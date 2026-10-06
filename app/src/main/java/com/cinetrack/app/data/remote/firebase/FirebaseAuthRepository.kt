package com.cinetrack.app.data.remote.firebase

import android.content.Context
import com.cinetrack.app.data.model.AppUser
import com.cinetrack.app.data.repository.AuthActionResult
import com.cinetrack.app.data.repository.AuthRepository
import com.cinetrack.app.data.repository.AuthResult
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthRepository(
    private val context: Context
) : AuthRepository {

    override val isFirebaseConfigured: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    private val authOrNull: FirebaseAuth?
        get() = if (isFirebaseConfigured) FirebaseAuth.getInstance() else null

    override val currentUser: AppUser?
        get() = authOrNull?.currentUser?.let { user ->
            AppUser(user.uid, user.displayName, user.email)
        }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val auth = authOrNull ?: return configurationError()
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener
                    val user = auth.currentUser
                    if (task.isSuccessful && user != null) {
                        continuation.resume(AuthResult.Success(AppUser(user.uid, user.displayName, user.email)))
                    } else {
                        continuation.resume(AuthResult.Error(mapFirebaseError(task.exception)))
                    }
                }
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        val auth = authOrNull ?: return configurationError()
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { createTask ->
                    if (!continuation.isActive) return@addOnCompleteListener
                    val user = auth.currentUser
                    if (!createTask.isSuccessful || user == null) {
                        continuation.resume(AuthResult.Error(mapFirebaseError(createTask.exception)))
                        return@addOnCompleteListener
                    }

                    val request = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()

                    user.updateProfile(request).addOnCompleteListener {
                        if (!continuation.isActive) return@addOnCompleteListener
                        continuation.resume(
                            AuthResult.Success(
                                AppUser(user.uid, name.trim(), user.email)
                            )
                        )
                    }
                }
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthActionResult {
        val auth = authOrNull ?: return AuthActionResult.Error(configurationError().message)
        return suspendCancellableCoroutine { continuation ->
            auth.sendPasswordResetEmail(email.trim())
                .addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener
                    continuation.resume(
                        if (task.isSuccessful) AuthActionResult.Success
                        else AuthActionResult.Error(mapFirebaseError(task.exception))
                    )
                }
        }
    }


    override suspend fun reauthenticateCurrentUser(password: String): AuthActionResult {
        val auth = authOrNull ?: return AuthActionResult.Error(configurationError().message)
        val user = auth.currentUser ?: return AuthActionResult.Error("No hay una sesión activa para verificar.")
        val email = user.email ?: return AuthActionResult.Error("La cuenta no tiene un correo disponible para verificar la identidad.")
        if (password.isBlank()) return AuthActionResult.Error("Ingresa tu contraseña actual para continuar.")

        val credential = EmailAuthProvider.getCredential(email, password)
        return suspendCancellableCoroutine { continuation ->
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener
                continuation.resume(
                    if (task.isSuccessful) AuthActionResult.Success
                    else AuthActionResult.Error(
                        when ((task.exception as? FirebaseAuthException)?.errorCode.orEmpty()) {
                            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "La contraseña actual no es correcta."
                            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento y vuelve a intentarlo."
                            "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo verificar tu identidad. Revisa la conexión."
                            else -> "No se pudo verificar tu identidad. Vuelve a iniciar sesión e inténtalo de nuevo."
                        }
                    )
                )
            }
        }
    }

    override suspend fun deleteCurrentAccount(): AuthActionResult {
        val auth = authOrNull ?: return AuthActionResult.Error(configurationError().message)
        val user = auth.currentUser ?: return AuthActionResult.Error("No hay una sesión activa para eliminar.")
        return suspendCancellableCoroutine { continuation ->
            user.delete().addOnCompleteListener { task ->
                if (!continuation.isActive) return@addOnCompleteListener
                continuation.resume(
                    if (task.isSuccessful) AuthActionResult.Success
                    else AuthActionResult.Error(
                        if ((task.exception as? FirebaseAuthException)?.errorCode == "ERROR_REQUIRES_RECENT_LOGIN")
                            "Por seguridad, vuelve a iniciar sesión y repite la eliminación de la cuenta."
                        else mapFirebaseError(task.exception)
                    )
                )
            }
        }
    }

    override fun signOut() {
        authOrNull?.signOut()
    }

    private fun configurationError() = AuthResult.Error(
        "Firebase todavía no está configurado. Agrega app/google-services.json y activa Email/Password en Firebase Console."
    )

    private fun mapFirebaseError(throwable: Throwable?): String {
        val code = (throwable as? FirebaseAuthException)?.errorCode.orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL" -> "Ingresa un correo válido."
            "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Ya existe una cuenta con este correo."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil."
            "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL" ->
                "Correo o contraseña incorrectos."
            "ERROR_NETWORK_REQUEST_FAILED" ->
                "No se pudo conectar. Revisa tu conexión e inténtalo nuevamente."
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Inténtalo nuevamente más tarde."
            else -> "No fue posible completar la autenticación. Inténtalo nuevamente."
        }
    }
}
