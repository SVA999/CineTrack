package com.cinetrack.app.data.remote.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("trending/all/week")
    suspend fun trending(
        @Query("language") language: String = "es-CO"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("language") language: String = "es-CO",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/{id}")
    suspend fun movieDetails(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbMovieDetailsDto

    @GET("tv/{id}")
    suspend fun tvDetails(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbTvDetailsDto

    @GET("movie/{id}/credits")
    suspend fun movieCredits(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbCreditsDto

    @GET("tv/{id}/credits")
    suspend fun tvCredits(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbCreditsDto

    @GET("movie/{id}/recommendations")
    suspend fun movieRecommendations(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/{id}/recommendations")
    suspend fun tvRecommendations(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/{id}/similar")
    suspend fun similarMovies(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/{id}/similar")
    suspend fun similarTv(
        @Path("id") id: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") withGenres: String? = null,
        @Query("with_crew") withCrew: String? = null,
        @Query("with_people") withPeople: String? = null,
        @Query("language") language: String = "es-CO",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("with_genres") withGenres: String? = null,
        @Query("language") language: String = "es-CO",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("person/{personId}/movie_credits")
    suspend fun personMovieCredits(
        @Path("personId") personId: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbPersonCreditsDto

    @GET("person/{personId}/tv_credits")
    suspend fun personTvCredits(
        @Path("personId") personId: Int,
        @Query("language") language: String = "es-CO"
    ): TmdbPersonCreditsDto
}
