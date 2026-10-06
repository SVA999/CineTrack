package com.cinetrack.app.util

import java.text.Normalizer

fun String.normalizedForSearch(): String = Normalizer.normalize(trim(), Normalizer.Form.NFD)
    .replace(Regex("\\p{Mn}+"), "")
    .lowercase()
