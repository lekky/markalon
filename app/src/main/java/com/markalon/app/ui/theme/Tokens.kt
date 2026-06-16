package com.markalon.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** Semantic color tokens, one set per theme (see handoff "Design Tokens"). */
data class MarkalonColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val fg1: Color,
    val fg2: Color,
    val fg3: Color,
    val codeBg: Color,
    val isDark: Boolean,
)

val LightColors = MarkalonColors(
    bg = Color(0xFFF4F1EA),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFECE9E1),
    border = Color(0xFFE4DFD4),
    fg1 = Color(0xFF26221B),
    fg2 = Color(0xFF6B6457),
    fg3 = Color(0xFF9D9686),
    codeBg = Color(0xFFF0ECE3),
    isDark = false,
)

val DarkColors = MarkalonColors(
    bg = Color(0xFF121317),
    surface = Color(0xFF1B1D22),
    surface2 = Color(0xFF242730),
    border = Color(0xFF30343D),
    fg1 = Color(0xFFECE9E2),
    fg2 = Color(0xFFA39E94),
    fg3 = Color(0xFF6F6C65),
    codeBg = Color(0xFF22252C),
    isDark = true,
)

/** Destructive / saved-status colors that don't vary by theme. */
val DeleteRed = Color(0xFFD6453C)
val SavedGreen = Color(0xFF3FA66A)

val LocalMarkalonColors = staticCompositionLocalOf { LightColors }
val LocalAccent = staticCompositionLocalOf { Color(0xFFD8623C) }

/** Parse a "#RRGGBB" hex string into a Compose Color. */
fun hexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val value = runCatching { clean.toLong(16) }.getOrNull() ?: return Color(0xFFD8623C)
    return when (clean.length) {
        6 -> Color(0xFF000000L or value)
        8 -> Color(value)
        else -> Color(0xFFD8623C)
    }
}

/** Render a Color as an upper-case "#RRGGBB" string (alpha dropped). */
fun Color.toHexString(): String = "#%06X".format(0xFFFFFF and toArgb())

/** Convert a hex color to HSV components [hue 0..360, saturation 0..1, value 0..1]. */
fun hexToHsv(hex: String): FloatArray {
    val argb = hexColor(hex).toArgb()
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv
}
