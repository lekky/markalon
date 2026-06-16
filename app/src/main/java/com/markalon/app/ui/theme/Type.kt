package com.markalon.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.markalon.app.data.EditorFont

/**
 * The UI/chrome font (Spline Sans in the design). The handoff accepts the
 * system sans as a native substitute; brand fonts can be added later via
 * downloadable or bundled fonts without touching call sites.
 */
val UiFontFamily = FontFamily.SansSerif

/** Inline code / code blocks always render monospace, regardless of editor font. */
val CodeFontFamily = FontFamily.Monospace

/** Maps the user's editor-font choice to a native family. */
fun EditorFont.toFontFamily(): FontFamily = when (this) {
    EditorFont.MONO -> FontFamily.Monospace
    EditorFont.SANS -> FontFamily.SansSerif
    EditorFont.SERIF -> FontFamily.Serif
    EditorFont.SYSTEM -> FontFamily.Default
}

val MarkalonTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
)
