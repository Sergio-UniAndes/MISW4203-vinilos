package com.misw4203.vinilos.feature.home.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val EditorialPalettes = listOf(
    listOf(Color(0xFF1F2A37), Color(0xFF0F1115), Color(0xFF7A5CFF)),
    listOf(Color(0xFF30204B), Color(0xFF111117), Color(0xFFB792FF)),
    listOf(Color(0xFF15282D), Color(0xFF0E1215), Color(0xFF47E0C8)),
    listOf(Color(0xFF34261B), Color(0xFF101012), Color(0xFFFFB38A)),
)

private fun editorialPalette(seed: String): List<Color> {
    val index = kotlin.math.abs(seed.hashCode()) % EditorialPalettes.size
    return EditorialPalettes[index]
}

internal fun editorialBrush(seed: String): Brush =
    Brush.linearGradient(colors = editorialPalette(seed))

internal fun editorialRadialBrush(seed: String): Brush =
    Brush.radialGradient(
        colors = editorialPalette(seed),
        center = Offset(500f, 240f),
        radius = 1200f,
    )
