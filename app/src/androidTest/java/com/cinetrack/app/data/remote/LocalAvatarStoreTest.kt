package com.cinetrack.app.data.remote

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.cinetrack.app.data.local.LocalAvatarStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class LocalAvatarStoreTest {
    @Test fun appliesCameraOrientationBeforeSaving() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uid = "rotation-${UUID.randomUUID()}"
        val source = File(context.cacheDir, "$uid.jpg")
        val store = LocalAvatarStore(context)
        try {
            val bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.RED)
            source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            bitmap.recycle()
            android.media.ExifInterface(source.path).apply {
                setAttribute(android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_ROTATE_90.toString())
                saveAttributes()
            }
            val saved = store.save(uid, Uri.fromFile(source).toString()).getOrThrow()
            val decoded = android.graphics.BitmapFactory.decodeFile(Uri.parse(saved).path)
            assertEquals(20, decoded.width)
            assertEquals(40, decoded.height)
            decoded.recycle()
        } finally { store.delete(uid); source.delete() }
    }

    @Test fun persistsIsolatesAccountsAndPreservesPhotoOnInvalidReplacement() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uid = "local-avatar-test-${UUID.randomUUID()}"
        val otherUid = "$uid-other"
        val store = LocalAvatarStore(context)
        val source = File(context.cacheDir, "$uid.png")
        val invalid = File(context.cacheDir, "$uid.txt")
        try {
            val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.MAGENTA)
            source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val first = store.save(uid, Uri.fromFile(source).toString()).getOrThrow()
            assertTrue(File(Uri.parse(first).path!!).canonicalPath.startsWith(context.noBackupFilesDir.canonicalPath))
            assertEquals(first, LocalAvatarStore(context).get(uid))
            assertNull(store.get(otherUid))
            invalid.writeText("not an image")
            assertTrue(store.save(uid, Uri.fromFile(invalid).toString()).isFailure)
            assertEquals(first, store.get(uid))
            val second = store.save(uid, Uri.fromFile(source).toString()).getOrThrow()
            assertNotEquals(first, second)
            assertFalse(File(Uri.parse(first).path!!).exists())
            store.delete(uid)
            assertNull(store.get(uid))
        } finally {
            store.delete(uid)
            source.delete()
            invalid.delete()
        }
    }
}
