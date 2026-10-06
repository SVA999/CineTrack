package com.cinetrack.app.data.remote.tmdb

import com.cinetrack.app.data.model.MediaDetails
import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.PersonCredit
import com.cinetrack.app.data.model.RelatedMedia
import com.cinetrack.app.data.model.RelationType
import com.cinetrack.app.data.repository.MediaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class TmdbMediaRepository(
    private val api: TmdbApiService,
    private val fallback: MediaRepository
) : MediaRepository {

    private val cache = ConcurrentHashMap<String, MediaTitle>()
    private val _items = MutableStateFlow(fallback.getAll())
    override val items: StateFlow<List<MediaTitle>> = _items.asStateFlow()
    override val remoteEnabled: Boolean = true

    private val _lastError = MutableStateFlow<String?>(null)
    override val lastError: StateFlow<String?> = _lastError.asStateFlow()

    init {
        fallback.getAll().forEach { cache[it.id] = it }
    }

    override fun getAll(): List<MediaTitle> = _items.value

    override fun getById(id: String): MediaTitle? = cache[id] ?: fallback.getById(id)

    override suspend fun refresh() {
        runCatching {
            api.trending().results
                .mapNotNull { it.toMediaTitle() }
                .distinctBy { it.id }
                .take(24)
        }.onSuccess { remote ->
            if (remote.isNotEmpty()) {
                remote.forEach { cache[it.id] = it }
                _items.value = remote
                _lastError.value = null
            }
        }.onFailure {
            _lastError.value = "No se pudo actualizar TMDB. Mostrando contenido local."
        }
    }

    override suspend fun search(query: String): List<MediaTitle> {
        if (query.isBlank()) return getAll()
        return runCatching {
            api.searchMulti(query.trim()).results
                .mapNotNull { it.toMediaTitle() }
                .distinctBy { it.id }
                .take(30)
                .also { results -> results.forEach { cache[it.id] = it } }
        }.onSuccess {
            _lastError.value = null
        }.onFailure {
            _lastError.value = "No se pudo buscar en TMDB."
        }.getOrElse {
            fallback.search(query)
        }
    }

    override suspend fun getDetails(id: String): MediaDetails? {
        val ref = parseRemoteId(id)
        if (ref == null) return fallback.getDetails(id)

        return runCatching {
            when (ref.type) {
                MediaType.MOVIE -> movieDetails(ref.tmdbId)
                MediaType.SERIES -> tvDetails(ref.tmdbId)
            }
        }.onSuccess {
            _lastError.value = null
            cache[it.media.id] = it.media
        }.onFailure {
            _lastError.value = "No se pudo cargar el detalle desde TMDB."
        }.getOrElse {
            getById(id)?.let { MediaDetails(media = it, related = emptyList()) }
        }
    }

    override suspend fun getRelated(id: String): List<RelatedMedia> {
        val ref = parseRemoteId(id) ?: return fallback.getRelated(id)
        return runCatching {
            buildRelated(ref)
        }.onFailure {
            _lastError.value = "No se pudieron cargar los títulos relacionados."
        }.getOrElse { emptyList() }
    }

    private suspend fun movieDetails(id: Int): MediaDetails = coroutineScope {
        val detailsDeferred = async { api.movieDetails(id) }
        val creditsDeferred = async { api.movieCredits(id) }
        val details = detailsDeferred.await()
        val credits = creditsDeferred.await()
        val director = credits.crew.firstOrNull { it.job.equals("Director", ignoreCase = true) }
        val media = details.toMediaTitle(director?.name)
        val related = buildRelated(RemoteRef(MediaType.MOVIE, id), details.genres.firstOrNull()?.id, director, credits.cast.firstOrNull())
        MediaDetails(
            media = media,
            cast = credits.cast.sortedBy { it.order ?: Int.MAX_VALUE }.take(12).map { it.toPersonCredit() },
            crew = credits.crew.take(12).map { it.toPersonCredit() },
            related = related
        )
    }

    private suspend fun tvDetails(id: Int): MediaDetails = coroutineScope {
        val detailsDeferred = async { api.tvDetails(id) }
        val creditsDeferred = async { api.tvCredits(id) }
        val details = detailsDeferred.await()
        val credits = creditsDeferred.await()
        val creator = details.createdBy.firstOrNull()
        val media = details.toMediaTitle(creator?.name)
        val related = buildRelated(
            RemoteRef(MediaType.SERIES, id),
            details.genres.firstOrNull()?.id,
            creator?.let { TmdbCrewDto(it.id, it.name, job = "Creator", profilePath = it.profilePath) },
            credits.cast.firstOrNull()
        )
        MediaDetails(
            media = media,
            cast = credits.cast.sortedBy { it.order ?: Int.MAX_VALUE }.take(12).map { it.toPersonCredit() },
            crew = buildList {
                creator?.let { add(PersonCredit(it.id, it.name, "Creador", profileImage(it.profilePath))) }
                addAll(credits.crew.take(11).map { it.toPersonCredit() })
            },
            related = related
        )
    }

    private suspend fun buildRelated(ref: RemoteRef): List<RelatedMedia> = when (ref.type) {
        MediaType.MOVIE -> {
            val details = api.movieDetails(ref.tmdbId)
            val credits = api.movieCredits(ref.tmdbId)
            buildRelated(
                ref,
                details.genres.firstOrNull()?.id,
                credits.crew.firstOrNull { it.job.equals("Director", true) },
                credits.cast.firstOrNull()
            )
        }
        MediaType.SERIES -> {
            val details = api.tvDetails(ref.tmdbId)
            val credits = api.tvCredits(ref.tmdbId)
            buildRelated(
                ref,
                details.genres.firstOrNull()?.id,
                details.createdBy.firstOrNull()?.let { TmdbCrewDto(it.id, it.name, "Creator", profilePath = it.profilePath) },
                credits.cast.firstOrNull()
            )
        }
    }

    private suspend fun buildRelated(
        ref: RemoteRef,
        genreId: Int?,
        directorOrCreator: TmdbCrewDto?,
        leadCast: TmdbCastDto?
    ): List<RelatedMedia> = supervisorScope {
        val recommendationsDeferred = async {
            runCatching {
                if (ref.type == MediaType.MOVIE) api.movieRecommendations(ref.tmdbId).results
                else api.tvRecommendations(ref.tmdbId).results
            }.getOrDefault(emptyList())
        }
        val similarDeferred = async {
            runCatching {
                if (ref.type == MediaType.MOVIE) api.similarMovies(ref.tmdbId).results
                else api.similarTv(ref.tmdbId).results
            }.getOrDefault(emptyList())
        }
        val genreDeferred = async {
            runCatching {
                if (genreId == null) emptyList()
                else if (ref.type == MediaType.MOVIE) api.discoverMovies(withGenres = genreId.toString()).results
                else api.discoverTv(withGenres = genreId.toString()).results
            }.getOrDefault(emptyList())
        }
        val creatorDeferred = async {
            runCatching {
                val personId = directorOrCreator?.id ?: return@runCatching emptyList<TmdbMediaDto>()
                if (ref.type == MediaType.MOVIE) api.personMovieCredits(personId).crew
                else api.personTvCredits(personId).crew
            }.getOrDefault(emptyList())
        }
        val actorDeferred = async {
            runCatching {
                val personId = leadCast?.id ?: return@runCatching emptyList<TmdbMediaDto>()
                if (ref.type == MediaType.MOVIE) api.personMovieCredits(personId).cast
                else api.personTvCredits(personId).cast
            }.getOrDefault(emptyList())
        }

        val output = LinkedHashMap<String, RelatedMedia>()
        fun add(items: List<TmdbMediaDto>, type: RelationType, reason: String, hint: MediaType) {
            items.asSequence()
                .mapNotNull { it.toMediaTitle(hint) }
                .filter { it.id != "${ref.type.idPrefix}:${ref.tmdbId}" }
                .forEach { media ->
                    cache[media.id] = media
                    output.putIfAbsent(media.id, RelatedMedia(media, type, reason))
                }
        }

        add(recommendationsDeferred.await().take(5), RelationType.RECOMMENDED, "Recomendada para este título", ref.type)
        add(similarDeferred.await().take(5), RelationType.SIMILAR_GENRE_THEME, "Géneros y temas similares", ref.type)
        directorOrCreator?.let { person ->
            add(
                creatorDeferred.await().take(5),
                if (ref.type == MediaType.MOVIE) RelationType.SAME_DIRECTOR else RelationType.SAME_CREATOR,
                if (ref.type == MediaType.MOVIE) "Misma dirección: ${person.name}" else "Mismo creador: ${person.name}",
                ref.type
            )
        }
        leadCast?.let { actor ->
            add(actorDeferred.await().take(5), RelationType.SAME_CAST, "Con ${actor.name}", ref.type)
        }
        if (genreId != null) {
            add(genreDeferred.await().take(5), RelationType.SAME_GENRE, "Mismo género", ref.type)
        }
        output.values.take(18)
    }

    private fun TmdbMediaDto.toMediaTitle(typeHint: MediaType? = null): MediaTitle? {
        val type = typeHint ?: when (mediaType) {
            "movie" -> MediaType.MOVIE
            "tv" -> MediaType.SERIES
            else -> return null
        }
        val resolvedTitle = if (type == MediaType.MOVIE) title else name
        if (resolvedTitle.isNullOrBlank()) return null
        val date = if (type == MediaType.MOVIE) releaseDate else firstAirDate
        val genres = genreIds.mapNotNull { genreName(type, it) }
        return MediaTitle(
            id = "${type.idPrefix}:$id",
            title = resolvedTitle,
            type = type,
            year = date?.take(4)?.toIntOrNull() ?: 0,
            genres = genres,
            synopsis = overview.orEmpty().ifBlank { "Sin sinopsis disponible." },
            generalRating = voteAverage?.div(2.0),
            ratingCount = voteCount,
            posterUrl = posterImage(posterPath),
            backdropUrl = backdropImage(backdropPath),
            tmdbId = id
        )
    }

    private fun TmdbMovieDetailsDto.toMediaTitle(director: String?) = MediaTitle(
        id = "movie:$id",
        title = title,
        type = MediaType.MOVIE,
        year = releaseDate?.take(4)?.toIntOrNull() ?: 0,
        genres = genres.map { it.name },
        durationMinutes = runtime,
        synopsis = overview.orEmpty().ifBlank { "Sin sinopsis disponible." },
        generalRating = voteAverage?.div(2.0),
        ratingCount = voteCount,
        director = director,
        posterUrl = posterImage(posterPath),
        backdropUrl = backdropImage(backdropPath),
        tmdbId = id
    )

    private fun TmdbTvDetailsDto.toMediaTitle(creator: String?) = MediaTitle(
        id = "tv:$id",
        title = name,
        type = MediaType.SERIES,
        year = firstAirDate?.take(4)?.toIntOrNull() ?: 0,
        genres = genres.map { it.name },
        durationMinutes = episodeRunTime.firstOrNull(),
        synopsis = overview.orEmpty().ifBlank { "Sin sinopsis disponible." },
        generalRating = voteAverage?.div(2.0),
        ratingCount = voteCount,
        director = creator,
        posterUrl = posterImage(posterPath),
        backdropUrl = backdropImage(backdropPath),
        tmdbId = id
    )

    private fun TmdbCastDto.toPersonCredit() = PersonCredit(id, name, character, profileImage(profilePath))
    private fun TmdbCrewDto.toPersonCredit() = PersonCredit(id, name, job ?: department, profileImage(profilePath))

    private data class RemoteRef(val type: MediaType, val tmdbId: Int)

    private fun parseRemoteId(id: String): RemoteRef? {
        val parts = id.split(':', limit = 2)
        if (parts.size != 2) return null
        val tmdbId = parts[1].toIntOrNull() ?: return null
        val type = when (parts[0]) {
            "movie" -> MediaType.MOVIE
            "tv" -> MediaType.SERIES
            else -> return null
        }
        return RemoteRef(type, tmdbId)
    }

    private val MediaType.idPrefix: String
        get() = if (this == MediaType.MOVIE) "movie" else "tv"

    private fun posterImage(path: String?) = path?.let { "https://image.tmdb.org/t/p/w500$it" }
    private fun backdropImage(path: String?) = path?.let { "https://image.tmdb.org/t/p/w780$it" }
    private fun profileImage(path: String?) = path?.let { "https://image.tmdb.org/t/p/w185$it" }

    private fun genreName(type: MediaType, id: Int): String? = when (type) {
        MediaType.MOVIE -> movieGenres[id]
        MediaType.SERIES -> tvGenres[id]
    }

    private val movieGenres = mapOf(
        28 to "Acción", 12 to "Aventura", 16 to "Animación", 35 to "Comedia",
        80 to "Crimen", 99 to "Documental", 18 to "Drama", 10751 to "Familia",
        14 to "Fantasía", 36 to "Historia", 27 to "Terror", 10402 to "Música",
        9648 to "Misterio", 10749 to "Romance", 878 to "Ciencia ficción",
        53 to "Suspenso", 10752 to "Guerra", 37 to "Western"
    )

    private val tvGenres = mapOf(
        10759 to "Acción y aventura", 16 to "Animación", 35 to "Comedia", 80 to "Crimen",
        99 to "Documental", 18 to "Drama", 10751 to "Familia", 10762 to "Infantil",
        9648 to "Misterio", 10763 to "Noticias", 10764 to "Reality",
        10765 to "Ciencia ficción y fantasía", 10766 to "Telenovela", 10767 to "Talk show",
        10768 to "Guerra y política", 37 to "Western"
    )
}
