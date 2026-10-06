package com.cinetrack.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID

/** Private per-account images, deliberately excluded from Android cloud backup. */
class LocalAvatarStore(private val context: Context) {
    private fun directory(uid: String): File {
        val key = MessageDigest.getInstance("SHA-256").digest(uid.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(context.noBackupFilesDir, "local_avatars/$key")
    }

    suspend fun get(uid: String): String? = withContext(Dispatchers.IO) {
        directory(uid).listFiles()?.firstOrNull { it.extension == "jpg" }?.let { Uri.fromFile(it).toString() }
    }

    suspend fun save(uid: String, source: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(Uri.parse(source))?.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= 5 * 1024 * 1024) { "Elige una imagen de hasta 5 MB." }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            } ?: error("No se pudo abrir la imagen.")
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            require(options.outWidth > 0 && options.outHeight > 0) { "El archivo no es una imagen compatible." }
            var sample = 1
            while (maxOf(options.outWidth, options.outHeight) / sample > 1024) sample *= 2
            options.inJustDecodeBounds = false
            options.inSampleSize = sample
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                ?: error("No se pudo leer la imagen.")
            // Camera JPEGs often store rotation in metadata rather than in their pixels.
            val orientation = runCatching {
                bytes.inputStream().use { ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix().apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                    ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                    ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                    ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(-90f); postScale(-1f, 1f) }
                    ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
                }
            }
            if (!matrix.isIdentity) {
                val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (oriented !== bitmap) bitmap.recycle()
                bitmap = oriented
            }
            val dir = directory(uid).apply { mkdirs() }
            val temporary = File(dir, UUID.randomUUID().toString() + ".tmp")
            val destination = File(dir, UUID.randomUUID().toString() + ".jpg")
            try {
                temporary.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
                check(temporary.renameTo(destination)) { "No se pudo guardar la imagen." }
                dir.listFiles()?.filter { it != destination }?.forEach { it.delete() }
                Uri.fromFile(destination).toString()
            } finally {
                bitmap.recycle()
                temporary.delete()
            }
        }
    }

    suspend fun delete(uid: String) = withContext(Dispatchers.IO) {
        directory(uid).deleteRecursively()
        Unit
    }
}
