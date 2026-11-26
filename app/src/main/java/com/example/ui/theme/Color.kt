package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Reference Japanese-Bauhaus Editorial Palette (from concept board)
val PaperIvory = Color(0xFFEBE4D5)
val PaperSand = Color(0xFFDDD4C0)
val PaperTaupe = Color(0xFFB5AB97)
val InkMuted = Color(0xFF777777)
val InkCharcoal = Color(0xFF333333)
val InkBlack = Color(0xFF0E0E0D)

// Warm Lion & Editorial Accents
val LionOchre = Color(0xFFB9822F)
val LionAmber = Color(0xFFCFA050)
val LionCream = Color(0xFFFAF6EC)
val SageSuccess = Color(0xFF3B7A57)
val AmberNotice = Color(0xFFC97A18)
val TerracottaAlert = Color(0xFFC24B3B)

// Dark Ink & Warm Paper Edition
val DarkPaperTop = Color(0xFF0C0C0B)
val DarkPaperMid = Color(0xFF111110)
val DarkPaperBottom = Color(0xFF0A0A09)
val DarkLionGold = Color(0xFFC9A361)
val DarkStoneCharcoal = Color(0xFF47433C)

data class GlassPalette(
    val isDark: Boolean,
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val orbCyan: Color,
    val orbMagenta: Color,
    val glassSurface: Color,
    val glassSurfaceElevated: Color,
    val glassBorderStart: Color,
    val glassBorderEnd: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentCyan: Color,      // Warm Lion Ochre / Gold primary accent
    val accentMagenta: Color,   // Deep Charcoal / Stone secondary accent
    val success: Color,
    val warning: Color,
    val error: Color,
    val boardLightSquare: Color,
    val boardDarkSquare: Color,
    val boardBorder: Color,
    val highlightSelected: Color,
    val highlightLastMove: Color,
    val highlightHint: Color,
    val inkShadow: Color,
    val paperSand: Color,
    val paperTaupe: Color,
    val lionMane: Color,
    val lionGold: Color
)

val LightGlassPalette = GlassPalette(
    isDark = false,
    bgTop = PaperIvory,
    bgMid = PaperIvory,
    bgBottom = Color(0xFFE3DBC9),
    orbCyan = Color(0x00000000),
    orbMagenta = Color(0x14BEB8AD),
    glassSurface = Color(0xFFF3EDDF),
    glassSurfaceElevated = PaperSand,
    glassBorderStart = InkBlack,
    glassBorderEnd = PaperTaupe,
    textPrimary = InkBlack,
    textSecondary = Color(0xFF55514A),
    accentCyan = LionOchre,
    accentMagenta = InkCharcoal,
    success = SageSuccess,
    warning = AmberNotice,
    error = TerracottaAlert,
    boardLightSquare = Color(0xFFE9DFC9),
    boardDarkSquare = Color(0xFFA38F70),
    boardBorder = Color(0xFF2A2824),
    highlightSelected = Color(0x99D9B25A),
    highlightLastMove = Color(0x66D9B25A),
    highlightHint = Color(0x993B7A57),
    inkShadow = Color(0x26111111),
    paperSand = PaperSand,
    paperTaupe = PaperTaupe,
    lionMane = LionOchre,
    lionGold = LionAmber
)

val DarkGlassPalette = GlassPalette(
    isDark = true,
    bgTop = DarkPaperTop,
    bgMid = DarkPaperMid,
    bgBottom = DarkPaperBottom,
    orbCyan = Color(0x00000000),
    orbMagenta = Color(0x1A777777),
    glassSurface = Color(0xFF151514),
    glassSurfaceElevated = Color(0xFF1D1C1A),
    glassBorderStart = Color(0xFF55524A),
    glassBorderEnd = Color(0xFF33312D),
    textPrimary = PaperIvory,
    textSecondary = Color(0xFF8E897D),
    accentCyan = DarkLionGold,
    accentMagenta = Color(0xFFD2CBC0),
    success = Color(0xFF6FA583),
    warning = Color(0xFFE5A446),
    error = Color(0xFFE06D5D),
    boardLightSquare = Color(0xFFB8B1A1),
    boardDarkSquare = Color(0xFF4A463F),
    boardBorder = Color(0xFF3A3834),
    highlightSelected = Color(0x99D9B25A),
    highlightLastMove = Color(0x59D9B25A),
    highlightHint = Color(0x9956A879),
    inkShadow = Color(0x55000000),
    paperSand = Color(0xFF1D1C1A),
    paperTaupe = Color(0xFF3E3B35),
    lionMane = DarkLionGold,
    lionGold = LionAmber
)
