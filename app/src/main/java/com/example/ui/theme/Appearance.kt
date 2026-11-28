package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** Thèmes de plateau. `light`/`dark` à null = couleurs du thème clair/sombre de l'application. */
enum class BoardTheme(val id: String, val fr: String, val en: String, val light: Color?, val dark: Color?) {
    CLASSIC("classic", "Classique", "Classic", null, null),
    WALNUT("walnut", "Noyer", "Walnut", Color(0xFFE6D3B3), Color(0xFF8A5E3B)),
    GRAPHITE("graphite", "Graphite", "Graphite", Color(0xFFC8CDD4), Color(0xFF5B6371)),
    SLATE("slate", "Ardoise", "Slate", Color(0xFFD3DBE6), Color(0xFF6A7E99)),
    MOSS("moss", "Mousse", "Moss", Color(0xFFE3E5D0), Color(0xFF6E8A68)),
    MARBLE("marble", "Marbre", "Marble", Color(0xFFEEECE7), Color(0xFF9B968D)),
    MIDNIGHT("midnight", "Minuit", "Midnight", Color(0xFF3A4252), Color(0xFF1B2029));

    companion object {
        fun fromId(id: String?) = values().firstOrNull { it.id == id } ?: CLASSIC
    }
}

/** Styles de pièces. */
enum class PieceStyle(val id: String, val fr: String, val en: String) {
    RELIEF("relief", "Relief", "Relief"),
    FLAT("flat", "Plat", "Flat"),
    METAL("metal", "Métal", "Metal"),
    GLASS("glass", "Cristal", "Crystal"),
    NEON("neon", "Néon", "Neon");

    companion object {
        fun fromId(id: String?) = values().firstOrNull { it.id == id } ?: RELIEF
    }
}

val LocalBoardTheme = compositionLocalOf { BoardTheme.CLASSIC }
val LocalPieceStyle = compositionLocalOf { PieceStyle.RELIEF }
