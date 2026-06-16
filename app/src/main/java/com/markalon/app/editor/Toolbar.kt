package com.markalon.app.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markalon.app.data.Tool
import com.markalon.app.data.ToolItem
import com.markalon.app.ui.theme.CodeFontFamily
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.UiFontFamily

/** Renders a tool's icon (text glyph or Material vector) in [tint]. */
@Composable
fun ToolIcon(tool: Tool, tint: Color, glyphSize: Int = 14) {
    @Composable
    fun glyph(
        text: String,
        weight: FontWeight = FontWeight.SemiBold,
        style: FontStyle = FontStyle.Normal,
        family: FontFamily = UiFontFamily,
        sizeSp: Int = glyphSize,
        strike: Boolean = false,
    ) {
        Text(
            text = text,
            color = tint,
            fontWeight = weight,
            fontStyle = style,
            fontFamily = family,
            fontSize = sizeSp.sp,
            textDecoration = if (strike) TextDecoration.LineThrough else TextDecoration.None,
        )
    }

    when (tool) {
        Tool.H1 -> glyph("H1")
        Tool.H2 -> glyph("H2")
        Tool.H3 -> glyph("H3")
        Tool.BOLD -> glyph("B", weight = FontWeight.ExtraBold, sizeSp = 15)
        Tool.ITALIC -> glyph("I", style = FontStyle.Italic, family = FontFamily.Serif, sizeSp = 16)
        Tool.STRIKE -> glyph("S", strike = true, sizeSp = 15)
        Tool.CODE -> glyph("</>", family = CodeFontFamily, sizeSp = 11)
        Tool.CODEBLOCK -> glyph("{ }", family = CodeFontFamily, sizeSp = 13)
        Tool.QUOTE -> glyph("“", family = FontFamily.Serif, sizeSp = 20)
        Tool.UL -> Icon(Icons.Filled.FormatListBulleted, null, tint = tint, modifier = Modifier.size(20.dp))
        Tool.OL -> Icon(Icons.Filled.FormatListNumbered, null, tint = tint, modifier = Modifier.size(20.dp))
        Tool.TASK -> Icon(Icons.Filled.Checklist, null, tint = tint, modifier = Modifier.size(20.dp))
        Tool.TABLE -> Icon(Icons.Filled.TableChart, null, tint = tint, modifier = Modifier.size(19.dp))
        Tool.HR -> Icon(Icons.Filled.HorizontalRule, null, tint = tint, modifier = Modifier.size(20.dp))
        Tool.UNDO -> Icon(Icons.Filled.Undo, null, tint = tint, modifier = Modifier.size(20.dp))
        Tool.REDO -> Icon(Icons.Filled.Redo, null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** The editor insert toolbar: a horizontally-scrolling row of enabled tools. */
@Composable
fun InsertToolbar(
    toolbar: List<ToolItem>,
    onTool: (Tool) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMarkalonColors.current
    val enabled = toolbar.filter { it.enabled }.mapNotNull { Tool.fromId(it.id) }
    Column(modifier.fillMaxWidth().background(colors.surface)) {
        HorizontalDivider(thickness = 1.dp, color = colors.border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            enabled.forEach { tool ->
                val interaction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(39.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(interactionSource = interaction, indication = null) { onTool(tool) },
                    contentAlignment = Alignment.Center,
                ) {
                    ToolIcon(tool, tint = colors.fg2)
                }
            }
        }
    }
}
