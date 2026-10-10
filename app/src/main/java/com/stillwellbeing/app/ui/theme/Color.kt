package com.stillwellbeing.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app's full palette, ported 1:1 from the design mockup's CSS tokens.
 * Material3's [androidx.compose.material3.ColorScheme] only has slots for
 * some of these (primary, background, surface, ...) — [Theme.kt] maps those,
 * and the rest (the tinted "hero"/"break"/"weekly" card surfaces, chart bars,
 * chips) are carried in [StillPalette] via [LocalStillPalette] instead of
 * being force-fit into slots that don't mean the same thing.
 */
data class StillPalette(
    val paper: Color,
    val card: Color,
    val cardAlt: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val lineSoft: Color,

    val accent: Color,
    val onAccentSurface: Color,
    val accentFill: Color,
    val blueFill: Color,

    val sage: Color,
    val pink: Color,
    val yellow: Color,

    val chipBg: Color,
    val chipFg: Color,

    val trackBg: Color,
    val bar: Color,
    val barSelected: Color,
    val barHour: Color,
    val barHourSelected: Color,
    val chartGrid: Color,

    val dateBg: Color,
    val dividerLine: Color
)

val LightStillPalette = StillPalette(
    paper = Color(0xFFFAF8F2),
    card = Color(0xFFFFFFFF),
    cardAlt = Color(0xFFF7F8F2),
    ink = Color(0xFF293C32),
    muted = Color(0xFF687268),
    line = Color(0xFFE7E7DC),
    lineSoft = Color(0xFFEFF0E9),

    accent = Color(0xFF456D55),
    onAccentSurface = Color(0xFF53674D),
    accentFill = Color(0xFF8BA27B),
    blueFill = Color(0xFF9BAFBD),

    sage = Color(0xFFE6EDDF),
    pink = Color(0xFFF3E2E4),
    yellow = Color(0xFFF8EDC9),

    chipBg = Color(0xB3FFFFFF),
    chipFg = Color(0xFF476141),

    trackBg = Color(0xFFEDF0E8),
    bar = Color(0xFFDBE5D3),
    barSelected = Color(0xFF829B71),
    barHour = Color(0xFFBCCBD3),
    barHourSelected = Color(0xFF768F9E),
    chartGrid = Color(0xFFE9ECE4),

    dateBg = Color(0xFFEEEEE5),
    dividerLine = Color(0x1C566441)
)

val DarkStillPalette = StillPalette(
    paper = Color(0xFF171B13),
    card = Color(0xFF1F251A),
    cardAlt = Color(0xFF1B2016),
    ink = Color(0xFFE9ECE2),
    muted = Color(0xFF95A18D),
    line = Color(0xFF2B3226),
    lineSoft = Color(0xFF262C20),

    accent = Color(0xFF93B681),
    onAccentSurface = Color(0xFFC9D9BD),
    accentFill = Color(0xFF7FA06A),
    blueFill = Color(0xFF6F93A8),

    sage = Color(0xFF263120),
    pink = Color(0xFF33242A),
    yellow = Color(0xFF362F1E),

    chipBg = Color(0x38000000),
    chipFg = Color(0xFFD3E4C6),

    trackBg = Color(0xFF262C1F),
    bar = Color(0xFF2B3325),
    barSelected = Color(0xFF7FA06A),
    barHour = Color(0xFF232E37),
    barHourSelected = Color(0xFF6F93A8),
    chartGrid = Color(0xFF232A1E),

    dateBg = Color(0xFF1B2016),
    dividerLine = Color(0x22D8E0C8)
)

val LocalStillPalette = staticCompositionLocalOf { LightStillPalette }
