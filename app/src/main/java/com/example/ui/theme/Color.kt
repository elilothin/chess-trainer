package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Reference Japanese-Bauhaus Editorial Palette (from concept board)
val PaperIvory = Color(0xFFF6F4EE)
val PaperSand = Color(0xFFE6E1D7)
val PaperTaupe = Color(0xFFBEB8AD)
val InkMuted = Color(0xFF777777)
val InkCharcoal = Color(0xFF333333)
val InkBlack = Color(0xFF111111)

// Warm Lion & Editorial Accents
val LionOchre = Color(0xFFD98A2B)
val LionAmber = Color(0xFFF0B44C)
val LionCream = Color(0xFFFAF6EC)
val SageSuccess = Color(0xFF3B7A57)
val AmberNotice = Color(0xFFC97A18)
val TerracottaAlert = Color(0xFFC24B3B)

// Dark Ink & Warm Paper Edition
val DarkPaperTop = Color(0xFF141311)
val DarkPaperMid = Color(0xFF1B1916)
val DarkPaperBottom = Color(0xFF100F0D)
val DarkLionGold = Color(0xFFE8A849)
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
    bgBottom = Color(0xFFEEEAE0),
    orbCyan = Color(0x18D98A2B),
    orbMagenta = Color(0x14BEB8AD),
    glassSurface = Color(0xFFFBF9F5),
    glassSurfaceElevated = PaperSand,
    glassBorderStart = InkBlack,
    glassBorderEnd = PaperTaupe,
    textPrimary = InkBlack,
    textSecondary = Color(0xFF59564E),
    accentCyan = LionOchre,
    accentMagenta = InkCharcoal,
    success = SageSuccess,
    warning = AmberNotice,
    error = TerracottaAlert,
    boardLightSquare = PaperIvory,
    boardDarkSquare = PaperTaupe,
    boardBorder = InkBlack,
    highlightSelected = Color(0x88E8A849),
    highlightLastMove = Color(0x55D98A2B),
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
    orbCyan = Color(0x1EE8A849),
    orbMagenta = Color(0x1A777777),
    glassSurface = Color(0xFF1E1C18),
    glassSurfaceElevated = Color(0xFF28251F),
    glassBorderStart = PaperTaupe,
    glassBorderEnd = Color(0xFF47433C),
    textPrimary = PaperIvory,
    textSecondary = PaperTaupe,
    accentCyan = DarkLionGold,
    accentMagenta = Color(0xFFD2CBC0),
    success = Color(0xFF56A879),
    warning = Color(0xFFE5A446),
    error = Color(0xFFE06D5D),
    boardLightSquare = Color(0xFFD9D3C7),
    boardDarkSquare = Color(0xFF706A60),
    boardBorder = PaperSand,
    highlightSelected = Color(0x88E8A849),
    highlightLastMove = Color(0x4DE8A849),
    highlightHint = Color(0x9956A879),
    inkShadow = Color(0x55000000),
    paperSand = Color(0xFF2A2722),
    paperTaupe = Color(0xFF5C574E),
    lionMane = DarkLionGold,
    lionGold = LionAmber
)
