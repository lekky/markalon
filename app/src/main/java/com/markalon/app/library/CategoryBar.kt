package com.markalon.app.library

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markalon.app.ui.theme.LocalAccent
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.UiFontFamily

/** The Library category filter row: All + categories + add (+) and manage (pencil). */
@Composable
fun CategoryBar(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    onAdd: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryChip("All", selected = selected == null) { onSelect(null) }
        categories.forEach { category ->
            CategoryChip(category, selected = selected == category) { onSelect(category) }
        }
        CircleIconButton(Icons.Filled.Add, "Add category", onAdd)
        if (categories.isNotEmpty()) {
            CircleIconButton(Icons.Filled.Edit, "Manage categories", onManage)
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) accent else colors.surface2)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else colors.fg1,
            fontFamily = UiFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = LocalMarkalonColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surface2)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = colors.fg2, modifier = Modifier.size(19.dp))
    }
}

/** Dialog to create a new category. */
@Composable
fun AddCategoryDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = LocalMarkalonColors.current
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("New category", color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.Bold) },
        text = { CategoryNameField(text, onChange = { text = it }, placeholder = "Category name") },
        confirmButton = {
            TextButton(onClick = { onConfirm(text); onDismiss() }, enabled = text.isNotBlank()) {
                Text("Add", color = if (text.isNotBlank()) LocalAccent.current else colors.fg3, fontFamily = UiFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fg2, fontFamily = UiFontFamily) }
        },
    )
}

/** Dialog to rename / delete existing categories. */
@Composable
fun ManageCategoriesDialog(
    categories: List<String>,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalMarkalonColors.current
    var renaming by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Categories", color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                categories.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            category,
                            color = colors.fg1,
                            fontFamily = UiFontFamily,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f),
                        )
                        CircleIconButton(Icons.Filled.Edit, "Rename $category") { renaming = category }
                        Box(Modifier.size(8.dp))
                        CircleIconButton(Icons.Filled.Delete, "Delete $category") { onDelete(category) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = LocalAccent.current, fontFamily = UiFontFamily) }
        },
    )

    renaming?.let { old ->
        RenameCategoryDialog(
            current = old,
            onConfirm = { newName -> onRename(old, newName); renaming = null },
            onDismiss = { renaming = null },
        )
    }
}

@Composable
private fun RenameCategoryDialog(current: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = LocalMarkalonColors.current
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Rename category", color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.Bold) },
        text = { CategoryNameField(text, onChange = { text = it }, placeholder = "Category name") },
        confirmButton = {
            TextButton(onClick = { onConfirm(text); onDismiss() }, enabled = text.isNotBlank()) {
                Text("Save", color = if (text.isNotBlank()) LocalAccent.current else colors.fg3, fontFamily = UiFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fg2, fontFamily = UiFontFamily) }
        },
    )
}

@Composable
private fun CategoryNameField(value: String, onChange: (String) -> Unit, placeholder: String) {
    val colors = LocalMarkalonColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(colors.surface2)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = colors.fg3, fontFamily = UiFontFamily, fontSize = 15.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = colors.fg1, fontFamily = UiFontFamily, fontSize = 15.sp),
            cursorBrush = SolidColor(LocalAccent.current),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
