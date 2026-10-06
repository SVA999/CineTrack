package com.cinetrack.app.share

import android.content.Context
import android.content.Intent
import com.cinetrack.app.BuildConfig
import com.cinetrack.app.data.model.MediaTitle

object ShareLinks {
    private val baseUrl: String get() = BuildConfig.CINETRACK_WEB_BASE_URL.removeSuffix("/")

    fun profile(uid: String): String = "$baseUrl/profile/$uid"

    fun media(media: MediaTitle): String {
        val parsed = parseMediaId(media.id) ?: return baseUrl
        return "$baseUrl/media/${parsed.first}/${parsed.second}"
    }

    fun review(reviewId: String): String = "$baseUrl/review/$reviewId"

    fun reviewId(uid: String, mediaId: String): String =
        "${uid}_${mediaId.replace(':', '_').replace('/', '_')}"

    fun share(context: Context, subject: String, text: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "$text\n$url")
        }
        context.startActivity(Intent.createChooser(intent, "Compartir con"))
    }

    private fun parseMediaId(mediaId: String): Pair<String, Int>? {
        val parts = mediaId.split(':', limit = 2)
        if (parts.size != 2) return null
        val type = parts[0].lowercase()
        if (type !in setOf("movie", "tv")) return null
        val numericId = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return null
        return type to numericId
    }
}
