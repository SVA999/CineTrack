package com.cinetrack.app.data.model

enum class RelationType {
    RECOMMENDED,
    SIMILAR_GENRE_THEME,
    SAME_DIRECTOR,
    SAME_CREATOR,
    SAME_CAST,
    SAME_GENRE
}

data class RelatedMedia(
    val media: MediaTitle,
    val relationType: RelationType,
    val reason: String
)
