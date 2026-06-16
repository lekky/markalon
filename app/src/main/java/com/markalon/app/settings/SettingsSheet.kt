package com.markalon.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markalon.app.data.Accent
import com.markalon.app.data.Accents
import com.markalon.app.data.EditorFont
import com.markalon.app.data.Settings
import com.markalon.app.data.ThemeMode
import com.markalon.app.data.Tool
import com.markalon.app.data.ToolItem
import com.markalon.app.data.ViewMode
import com.markalon.app.editor.ToolIcon
import com.markalon.app.ui.components.SegmentLabel
import com.markalon.app.ui.components.SegmentedControl
import com.markalon.app.ui.theme.LocalAccent
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.UiFontFamily
import com.markalon.app.ui.theme.hexColor
import com.markalon.app.ui.theme.hexToHsv
import com.markalon.app.ui.theme.toFontFamily
import com.markalon.app.ui.theme.toHexString

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    onDismiss: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onAccent: (String) -> Unit,
    onFont: (EditorFont) -> Unit,
    onDefaultView: (ViewMode) -> Unit,
    onToolbar: (List<ToolItem>) -> Unit,
) {
    val colors = LocalMarkalonColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.fg1,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 38.dp, height = 4.dp).clip(RoundedCornerShape(3.dp)).background(colors.border))
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxHeight(0.93f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                Text("Settings", color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(colors.surface2).clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Close, "Close", tint = colors.fg2, modifier = Modifier.size(18.dp))
                }
            }

            SectionHeader("Appearance")
            ThemeControl(settings.themeMode, onTheme)

            SectionHeader("Accent color")
            AccentSwatches(settings.accent, onAccent)

            SectionHeader("Editor font")
            FontList(settings.editorFont, onFont)

            SectionHeader("Default view", helper = "How notes open by default.")
            SegmentedControl(
                options = listOf(ViewMode.RAW, ViewMode.MIXED, ViewMode.RENDERED),
                selected = settings.defaultViewMode,
                onSelect = onDefaultView,
                modifier = Modifier.fillMaxWidth(),
            ) { mode -> SegmentLabel(mode.label, active = mode == settings.defaultViewMode) }

            SectionHeader("Insert toolbar", helper = "Toggle buttons on or off, and reorder them with the arrows.")
            ToolbarEditor(settings.toolbar, onToolbar)
        }
    }
}

@Composable
private fun SectionHeader(title: String, helper: String? = null) {
    val colors = LocalMarkalonColors.current
    Column(Modifier.padding(top = 22.dp, bottom = 10.dp)) {
        Text(
            title.uppercase(),
            color = colors.fg3,
            fontFamily = UiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
        if (helper != null) {
            Text(helper, color = colors.fg3, fontFamily = UiFontFamily, fontSize = 12.5f.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ThemeControl(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val colors = LocalMarkalonColors.current
    val items = listOf(
        Triple(ThemeMode.SYSTEM, "Auto", Icons.Filled.Computer),
        Triple(ThemeMode.LIGHT, "Light", Icons.Filled.LightMode),
        Triple(ThemeMode.DARK, "Dark", Icons.Filled.DarkMode),
    )
    SegmentedControl(
        options = items,
        selected = items.first { it.first == selected },
        onSelect = { onSelect(it.first) },
        modifier = Modifier.fillMaxWidth(),
    ) { item ->
        val active = item.first == selected
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(item.third, null, tint = if (active) colors.fg1 else colors.fg2, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            SegmentLabel(item.second, active)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentSwatches(selectedHex: String, onSelect: (String) -> Unit) {
    val colors = LocalMarkalonColors.current
    val isPreset = Accents.ALL.any { it.hex.equals(selectedHex, ignoreCase = true) }
    var showCustom by remember { mutableStateOf(false) }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Accents.ALL.forEach { accent: Accent ->
            val selected = accent.hex.equals(selectedHex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(hexColor(accent.hex))
                    .border(
                        width = 2.dp,
                        color = if (selected) colors.fg1 else Color.Transparent,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(accent.hex) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(Icons.Filled.Check, accent.name, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Custom color swatch: shows the current custom color (when not a preset),
        // otherwise a palette affordance. Tapping opens the picker.
        val customSelected = !isPreset
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (customSelected) hexColor(selectedHex) else colors.surface2)
                .border(
                    width = 2.dp,
                    color = if (customSelected) colors.fg1 else colors.border,
                    shape = CircleShape,
                )
                .clickable { showCustom = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (customSelected) Icons.Filled.Check else Icons.Filled.Palette,
                contentDescription = "Custom color",
                tint = if (customSelected) Color.White else colors.fg2,
                modifier = Modifier.size(20.dp),
            )
        }
    }

    if (showCustom) {
        CustomColorDialog(
            initialHex = selectedHex,
            onConfirm = { onSelect(it); showCustom = false },
            onDismiss = { showCustom = false },
        )
    }
}

@Composable
private fun CustomColorDialog(
    initialHex: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalMarkalonColors.current
    val initialHsv = remember(initialHex) { hexToHsv(initialHex) }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val current = Color.hsv(hue, sat, value)
    val hex = current.toHexString()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Custom color", color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(current)
                            .border(1.dp, colors.border, CircleShape)
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(hex, color = colors.fg2, fontFamily = UiFontFamily, fontSize = 15.sp)
                }
                ColorSlider("Hue", hue, 0f, 360f) { hue = it }
                ColorSlider("Saturation", sat, 0f, 1f) { sat = it }
                ColorSlider("Brightness", value, 0f, 1f) { value = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hex); onDismiss() }) {
                Text("Use color", color = current, fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fg2, fontFamily = UiFontFamily) }
        },
    )
}

@Composable
private fun ColorSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current
    Column {
        Text(label, color = colors.fg3, fontFamily = UiFontFamily, fontSize = 12.5f.sp)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = colors.surface2,
            ),
        )
    }
}

@Composable
private fun FontList(selected: EditorFont, onSelect: (EditorFont) -> Unit) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        EditorFont.entries.forEach { font ->
            val isSelected = font == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(colors.surface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) accent else colors.border,
                        shape = RoundedCornerShape(13.dp),
                    )
                    .clickable { onSelect(font) }
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "M",
                    color = colors.fg1,
                    fontFamily = font.toFontFamily(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.size(width = 28.dp, height = 28.dp).padding(end = 4.dp),
                )
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(font.label, color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.5f.sp)
                    Text(font.fontName, color = colors.fg3, fontFamily = font.toFontFamily(), fontSize = 12.5f.sp)
                }
                if (isSelected) {
                    Icon(Icons.Filled.Check, null, tint = accent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ToolbarEditor(toolbar: List<ToolItem>, onChange: (List<ToolItem>) -> Unit) {
    val colors = LocalMarkalonColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp)),
    ) {
        toolbar.forEachIndexed { index, item ->
            val tool = Tool.fromId(item.id) ?: return@forEachIndexed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(colors.surface2),
                    contentAlignment = Alignment.Center,
                ) {
                    ToolIcon(tool, tint = colors.fg2, glyphSize = 11)
                }
                Spacer(Modifier.size(12.dp))
                Text(tool.label, color = colors.fg1, fontFamily = UiFontFamily, fontSize = 14.5f.sp, modifier = Modifier.weight(1f))

                ReorderArrow(Icons.Filled.KeyboardArrowUp, enabled = index > 0) {
                    onChange(toolbar.moved(index, index - 1))
                }
                ReorderArrow(Icons.Filled.KeyboardArrowDown, enabled = index < toolbar.lastIndex) {
                    onChange(toolbar.moved(index, index + 1))
                }
                Spacer(Modifier.size(6.dp))
                ToggleSwitch(item.enabled) {
                    onChange(toolbar.mapIndexed { i, t -> if (i == index) t.copy(enabled = !t.enabled) else t })
                }
            }
            if (index < toolbar.lastIndex) {
                HorizontalDivider(color = colors.border, modifier = Modifier.padding(start = 13.dp))
            }
        }
    }
}

@Composable
private fun ReorderArrow(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalMarkalonColors.current
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) colors.fg2 else colors.fg3.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ToggleSwitch(on: Boolean, onToggle: () -> Unit) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 24.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (on) accent else colors.surface2)
            .border(
                width = if (on) 0.dp else 1.dp,
                color = if (on) Color.Transparent else colors.border,
                shape = RoundedCornerShape(13.dp),
            )
            .clickable { onToggle() }
            .padding(horizontal = 3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (on) Color.White else colors.fg3)
        )
    }
}

private fun List<ToolItem>.moved(from: Int, to: Int): List<ToolItem> {
    if (from == to || to < 0 || to > lastIndex) return this
    val list = toMutableList()
    val element = list.removeAt(from)
    list.add(to, element)
    return list
}
