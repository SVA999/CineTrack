package com.cinetrack.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test
    fun removesAccentsAndNormalizesCase() {
        assertEquals("accion ciencia ficcion", "Acción Ciencia FICCIÓN".normalizedForSearch())
    }
}
