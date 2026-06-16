package com.markalon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markalon.app.ui.theme.LocalAccent
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.UiFontFamily

/** Generic segmented control matching the handoff (track radius 12, inner 9). */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> Unit,
) {
    val colors = LocalMarkalonColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        options.forEach { option ->
            val active = option == selected
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) colors.surface else Color.Transparent)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                    ) { onSelect(option) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                label(option)
            }
        }
    }
}

@Composable
fun SegmentLabel(text: String, active: Boolean) {
    val colors = LocalMarkalonColors.current
    Text(
        text = text,
        color = if (active) colors.fg1 else colors.fg2,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        fontFamily = UiFontFamily,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
    )
}

/** Accent logo tile with an "M↓" style glyph. */
@Composable
fun MarkalonLogo(size: Int = 32) {
    val accent = LocalAccent.current
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.28f).dp))
            .background(accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "M↓",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = UiFontFamily,
            fontSize = (size * 0.42f).sp,
        )
    }
}
