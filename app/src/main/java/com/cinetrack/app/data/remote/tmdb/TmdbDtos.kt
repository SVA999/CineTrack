package com.cinetrack.app.data.remote.tmdb

import com.google.gson.annotations.SerializedName

data class TmdbPagedResponse<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    @SerializedName("total_pages") val totalPages: Int = 1,
    @SerializedName("total_results") val totalResults: Int = 0
)

data class TmdbMediaDto(
    val id: Int,
    @SerializedName("media_type") val mediaType: String? = null,
    val title: String? = null,
    val name: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    val overview: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
    @SerializedName("vote_count") val voteCount: Int? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    val popularity: Double? = null
)

data class TmdbGenreDto(val id: Int, val name: String)

data class TmdbMovieDetailsDto(
    val id: Int,
    val title: String,
    @SerializedName("release_date") val releaseDate: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val runtime: Int? = null,
    val overview: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
    @SerializedName("vote_count") val voteCount: Int? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null
)

data class TmdbCreatorDto(
    val id: Int,
    val name: String,
    @SerializedName("profile_path") val profilePath: String? = null
)

data class TmdbTvDetailsDto(
    val id: Int,
    val name: String,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerializedName("episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    val overview: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
    @SerializedName("vote_count") val voteCount: Int? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("created_by") val createdBy: List<TmdbCreatorDto> = emptyList()
)

data class TmdbCastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    @SerializedName("profile_path") val profilePath: String? = null,
    val order: Int? = null
)

data class TmdbCrewDto(
    val id: Int,
    val name: String,
    val job: String? = null,
    val department: String? = null,
    @SerializedName("profile_path") val profilePath: String? = null
)

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbPersonCreditsDto(
    val cast: List<TmdbMediaDto> = emptyList(),
    val crew: List<TmdbMediaDto> = emptyList()
)
