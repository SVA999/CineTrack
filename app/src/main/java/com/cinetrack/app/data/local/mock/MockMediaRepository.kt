package com.cinetrack.app.data.local.mock

import com.cinetrack.app.data.model.MediaTitle
import com.cinetrack.app.data.model.MediaType
import com.cinetrack.app.data.model.MediaDetails
import com.cinetrack.app.data.model.RelatedMedia
import com.cinetrack.app.data.model.RelationType
import com.cinetrack.app.data.repository.MediaRepository
import com.cinetrack.app.util.normalizedForSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockMediaRepository : MediaRepository {
    private val seedItems = listOf(
        MediaTitle(
            id = "orbita-cero", title = "Órbita Cero", type = MediaType.MOVIE, year = 2026,
            genres = listOf("Ciencia ficción", "Drama"), durationMinutes = 118,
            synopsis = "Una misión científica queda aislada tras detectar una señal imposible en la órbita terrestre. Mientras la tripulación intenta volver a casa, cada decisión revela una versión distinta de lo que ocurrió antes del apagón.",
            generalRating = 4.6, ratingCount = 1240, ageRating = "13+", director = "Valeria Montes"
        ),
        MediaTitle(
            id = "bosque-cristal", title = "Bosque de Cristal", type = MediaType.MOVIE, year = 2025,
            genres = listOf("Drama", "Terror"), durationMinutes = 104,
            synopsis = "Un grupo de excursionistas descubre que un bosque aparentemente abandonado conserva recuerdos de quienes lo atraviesan.",
            generalRating = 4.2, ratingCount = 782, ageRating = "16+", director = "Marco Salcedo"
        ),
        MediaTitle(
            id = "horizonte", title = "Horizonte", type = MediaType.SERIES, year = 2026,
            genres = listOf("Drama", "Ciencia ficción"),
            synopsis = "Tras una tormenta solar, varias ciudades intentan reconstruir una red de comunicación global mientras aparecen transmisiones de un lugar que no figura en ningún mapa.",
            generalRating = 4.8, ratingCount = 3054, ageRating = "13+", director = "Andrea Vélez"
        ),
        MediaTitle(
            id = "ultimo-viaje", title = "Último Viaje", type = MediaType.MOVIE, year = 2024,
            genres = listOf("Drama", "Acción"), durationMinutes = 126,
            synopsis = "Un conductor retirado acepta una última ruta que termina conectándolo con un caso que nunca pudo resolver.",
            generalRating = 4.1, ratingCount = 621, ageRating = "16+", director = "Samuel Arango"
        ),
        MediaTitle(
            id = "neon", title = "Neón", type = MediaType.SERIES, year = 2025,
            genres = listOf("Acción", "Drama"),
            synopsis = "Dos detectives siguen una cadena de sabotajes tecnológicos en una ciudad que nunca apaga sus luces.",
            generalRating = 4.4, ratingCount = 1918, ageRating = "16+", director = "Camila Ríos"
        ),
        MediaTitle(
            id = "sombra", title = "Sombra", type = MediaType.MOVIE, year = 2026,
            genres = listOf("Terror", "Drama"), durationMinutes = 97,
            synopsis = "Una restauradora encuentra mensajes ocultos en antiguas fotografías de una casa a punto de ser demolida.",
            generalRating = 4.0, ratingCount = 488, ageRating = "18+", director = "Tomás Herrera"
        ),
        MediaTitle(
            id = "domingo-en-marte", title = "Domingo en Marte", type = MediaType.MOVIE, year = 2026,
            genres = listOf("Comedia", "Ciencia ficción"), durationMinutes = 101,
            synopsis = "La primera familia de turistas en Marte descubre que sus vacaciones de lujo se parecen mucho más a una mudanza improvisada.",
            generalRating = 4.3, ratingCount = 934, ageRating = "7+", director = "Juliana Paz"
        ),
        MediaTitle(
            id = "manual-para-fantasmas", title = "Manual para Fantasmas", type = MediaType.SERIES, year = 2025,
            genres = listOf("Comedia", "Terror"),
            synopsis = "Tres compañeros de apartamento descubren que su nuevo vecino lleva muerto más de cincuenta años y necesita ayuda para irse.",
            generalRating = 4.5, ratingCount = 1433, ageRating = "13+", director = "Nicolás Rojas"
        ),
        MediaTitle(
            id = "linea-roja", title = "Línea Roja", type = MediaType.SERIES, year = 2024,
            genres = listOf("Acción", "Ciencia ficción"),
            synopsis = "Una operadora del metro recibe llamadas desde trenes que todavía no han salido de la estación.",
            generalRating = 4.7, ratingCount = 2210, ageRating = "16+", director = "Paula Quintero"
        ),
        MediaTitle(
            id = "cafe-a-las-tres", title = "Café a las Tres", type = MediaType.MOVIE, year = 2023,
            genres = listOf("Comedia", "Drama"), durationMinutes = 94,
            synopsis = "Cinco desconocidos coinciden cada jueves en el mismo café y terminan formando una amistad que ninguno estaba buscando.",
            generalRating = 3.9, ratingCount = 517, ageRating = "7+", director = "Esteban Ruiz"
        )
    )

    private val _items = MutableStateFlow(seedItems)
    override val items: StateFlow<List<MediaTitle>> = _items
    override val remoteEnabled: Boolean = false
    private val _lastError = MutableStateFlow<String?>(null)
    override val lastError: StateFlow<String?> = _lastError

    override fun getAll(): List<MediaTitle> = _items.value
    override fun getById(id: String): MediaTitle? = _items.value.firstOrNull { it.id == id }

    override suspend fun refresh() = Unit

    override suspend fun search(query: String): List<MediaTitle> {
        val needle = query.normalizedForSearch()
        if (needle.isBlank()) return getAll()
        return getAll().filter { media ->
            listOf(media.title, media.director.orEmpty(), media.genres.joinToString(" "))
                .joinToString(" ")
                .normalizedForSearch()
                .contains(needle)
        }
    }

    override suspend fun getDetails(id: String): MediaDetails? {
        val media = getById(id) ?: return null
        return MediaDetails(media = media, related = getRelated(id))
    }

    override suspend fun getRelated(id: String): List<RelatedMedia> {
        val current = getById(id) ?: return emptyList()
        return getAll()
            .asSequence()
            .filter { it.id != id }
            .mapNotNull { candidate ->
                when {
                    current.director != null && current.director == candidate.director ->
                        RelatedMedia(candidate, RelationType.SAME_DIRECTOR, "Misma dirección: ${current.director}")
                    current.genres.any { genre -> candidate.genres.any { it.equals(genre, ignoreCase = true) } } ->
                        RelatedMedia(candidate, RelationType.SAME_GENRE, "Género relacionado")
                    else -> null
                }
            }
            .take(8)
            .toList()
    }
}
