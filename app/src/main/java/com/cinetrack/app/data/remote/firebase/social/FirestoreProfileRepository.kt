package com.cinetrack.app.data.remote.firebase.social

import android.content.Context
import com.cinetrack.app.BuildConfig
import android.net.Uri
import com.cinetrack.app.data.model.social.UserProfile
import com.cinetrack.app.data.repository.social.ProfileRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirestoreProfileRepository(
    private val context: Context
) : ProfileRepository {
    override val isConfigured: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    private val firestore: FirebaseFirestore?
        get() = if (isConfigured) FirebaseFirestore.getInstance() else null
    private val storage: FirebaseStorage?
        get() = if (isConfigured && BuildConfig.FIREBASE_STORAGE_ENABLED) FirebaseStorage.getInstance() else null

    override suspend fun getProfile(uid: String): Result<UserProfile?> {
        val db = firestore ?: return Result.failure(configurationError())
        return suspendCancellableCoroutine { continuation ->
            db.collection("profiles").document(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(Result.success(snapshot.toObject(UserProfile::class.java)))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val db = firestore ?: return Result.failure(configurationError())
        val safeProfile = profile.copy(updatedAtEpochMillis = System.currentTimeMillis())
        return suspendCancellableCoroutine { continuation ->
            db.collection("profiles").document(profile.uid).set(safeProfile)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }

    override suspend fun uploadAvatar(uid: String, sourceUri: String): Result<String> {
        if (!BuildConfig.FIREBASE_STORAGE_ENABLED) {
            return Result.failure(IllegalStateException("Las fotos se guardan solo en este dispositivo."))
        }
        val storage = storage ?: return Result.failure(configurationError())
        val uri = Uri.parse(sourceUri)
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri).orEmpty()
        if (!mimeType.startsWith("image/")) {
            return Result.failure(IllegalArgumentException("El archivo seleccionado no es una imagen válida."))
        }
        val size = runCatching { resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } }.getOrNull() ?: -1L
        if (size >= 5L * 1024L * 1024L) {
            return Result.failure(IllegalArgumentException("La imagen supera el límite de 5 MB."))
        }
        val reference = storage.reference.child("profile_avatars/$uid/avatar")
        return suspendCancellableCoroutine { continuation ->
            reference.putFile(uri, StorageMetadata.Builder().setContentType(mimeType).build())
                .continueWithTask { uploadTask ->
                    if (!uploadTask.isSuccessful) throw uploadTask.exception ?: IllegalStateException("No se pudo subir la imagen")
                    reference.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    if (continuation.isActive) continuation.resume(Result.success(downloadUri.toString()))
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }


    override suspend fun deleteProfile(uid: String): Result<Unit> {
        val db = firestore ?: return Result.failure(configurationError())
        val profileRef = db.collection("profiles").document(uid)
        return suspendCancellableCoroutine { continuation ->
            profileRef.get()
                .addOnSuccessListener { snapshot ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    val hadAvatar = snapshot.toObject(UserProfile::class.java)?.avatarUrl?.isNotBlank() == true
                    fun deleteDocument() {
                        profileRef.delete()
                            .addOnSuccessListener {
                                if (continuation.isActive) continuation.resume(Result.success(Unit))
                            }
                            .addOnFailureListener { error ->
                                if (continuation.isActive) continuation.resume(Result.failure(error))
                            }
                    }
                    // Preserve the avatar reference if Storage fails so a retry can clean it up.
                    val storageInstance = storage
                    if (!BuildConfig.FIREBASE_STORAGE_ENABLED || !hadAvatar) {
                        deleteDocument()
                    } else if (storageInstance == null) {
                        continuation.resume(Result.failure(configurationError()))
                    } else {
                        storageInstance.reference.child("profile_avatars/$uid/avatar").delete()
                            .addOnSuccessListener { deleteDocument() }
                            .addOnFailureListener { error ->
                                if ((error as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                    deleteDocument()
                                } else if (continuation.isActive) {
                                    continuation.resume(Result.failure(error))
                                }
                            }
                    }
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(error))
                }
        }
    }

    private fun configurationError() = IllegalStateException(
        "Firebase no está conectado. Agrega google-services.json y habilita Firestore/Storage."
    )
}

