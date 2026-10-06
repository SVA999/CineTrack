package com.cinetrack.app.data.model

data class MediaDetails(
    val media: MediaTitle,
    val cast: List<PersonCredit> = emptyList(),
    val crew: List<PersonCredit> = emptyList(),
    val related: List<RelatedMedia> = emptyList()
)
