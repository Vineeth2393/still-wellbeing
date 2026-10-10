package com.stillwellbeing.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// FontFamily.Serif is a generic family (resolves to the platform's serif,
// e.g. Noto Serif) — a stand-in for the mockup's Georgia display face without
// bundling a font file. Swap for a downloaded Google Font later if desired.
private val DisplayFace = FontFamily.Serif
private val BodyFace = FontFamily.Default

val StillTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 1.5.em
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 1.5.em
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 1.5.em
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp
    )
)

/** The dramatic hero number (today's pickup count) — not a Material3 role. */
val BigNumberStyle = TextStyle(
    fontFamily = BodyFace,
    fontWeight = FontWeight.Medium,
    fontSize = 56.sp,
    letterSpacing = (-1.5).sp
)

/** A small caps-style section eyebrow, e.g. "A LITTLE SPACE TO BREATHE". */
val EyebrowStyle = TextStyle(
    fontFamily = BodyFace,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    letterSpacing = 1.4.sp
)
