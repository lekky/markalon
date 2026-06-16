package com.markalon.app.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markalon.app.data.Note
import com.markalon.app.data.Settings
import com.markalon.app.data.Tool
import com.markalon.app.data.ViewMode
import com.markalon.app.markdown.MarkdownView
import com.markalon.app.ui.components.SegmentLabel
import com.markalon.app.ui.components.SegmentedControl
import com.markalon.app.ui.theme.LocalAccent
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.SavedGreen
import com.markalon.app.ui.theme.DeleteRed
import com.markalon.app.ui.theme.UiFontFamily
import com.markalon.app.ui.theme.toFontFamily
import com.markalon.app.util.wordCount

@Composable
fun EditorScreen(
    note: Note,
    settings: Settings,
    view: ViewMode,
    saved: Boolean,
    menuOpen: Boolean,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSetView: (ViewMode) -> Unit,
    onUndo: () -> String?,
    onRedo: () -> String?,
    onOpenMenu: () -> Unit,
    onCloseMenu: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit,
    onSetCategory: (String?) -> Unit,
) {
    val colors = LocalMarkalonColors.current
    val bodyFont = settings.editorFont.toFontFamily()
    var showCategoryPicker by remember { mutableStateOf(false) }

    // Local editor text state, reseeded when switching notes.
    // Local editor text state, reseeded when switching notes. Undo/redo replace
    // it explicitly via the callbacks below.
    var tfv by remember(note.id) {
        mutableStateOf(TextFieldValue(note.body, TextRange(note.body.length)))
    }

    fun applyTool(tool: Tool) {
        when (tool) {
            Tool.UNDO -> onUndo()?.let { tfv = TextFieldValue(it, TextRange(it.length)) }
            Tool.REDO -> onRedo()?.let { tfv = TextFieldValue(it, TextRange(it.length)) }
            else -> {
                val sel = tfv.selection
                val result = applyInsert(tool, tfv.text, sel.min, sel.max) ?: return
                tfv = TextFieldValue(result.text, TextRange(result.selStart, result.selEnd))
                onBodyChange(result.text)
            }
        }
    }

    Column(Modifier.fillMaxSize().background(colors.bg)) {
        EditorTopBar(
            noteId = note.id,
            initialTitle = note.title,
            saved = saved,
            view = view,
            wordCount = wordCount(tfv.text),
            onBack = onBack,
            onTitleChange = onTitleChange,
            onSetView = onSetView,
            onOpenMenu = onOpenMenu,
            menuOpen = menuOpen,
            onCloseMenu = onCloseMenu,
            onImport = onImport,
            onExport = onExport,
            onSettings = onSettings,
            onDelete = onDelete,
            onCategory = { showCategoryPicker = true },
        )

        when (view) {
            ViewMode.RAW -> Column(Modifier.fillMaxSize()) {
                EditorTextArea(
                    value = tfv,
                    bodyFont = bodyFont,
                    onValueChange = { tfv = it; onBodyChange(it.text) },
                    modifier = Modifier.weight(1f),
                )
                InsertToolbar(toolbar = settings.toolbar, onTool = ::applyTool)
            }

            ViewMode.MIXED -> Column(Modifier.fillMaxSize()) {
                EditorTextArea(
                    value = tfv,
                    bodyFont = bodyFont,
                    onValueChange = { tfv = it; onBodyChange(it.text) },
                    modifier = Modifier.weight(1f),
                )
                InsertToolbar(toolbar = settings.toolbar, onTool = ::applyTool)
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(colors.bg)
                ) {
                    PreviewPane(markdown = tfv.text, bodyFont = bodyFont, topBorder = true)
                }
            }

            ViewMode.RENDERED -> PreviewPane(
                markdown = tfv.text,
                bodyFont = bodyFont,
                topBorder = false,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = settings.categories,
            current = note.category,
            onPick = { onSetCategory(it); showCategoryPicker = false },
            onDismiss = { showCategoryPicker = false },
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<String>,
    current: String?,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalMarkalonColors.current
    val accent = LocalAccent.current

    @Composable
    fun Option(label: String, value: String?) {
        val selected = value == current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPick(value) }
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                color = if (selected) accent else colors.fg1,
                fontFamily = UiFontFamily,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Category", color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Option("None", null)
                categories.forEach { Option(it, it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = accent, fontFamily = UiFontFamily) }
        },
    )
}

@Composable
private fun EditorTextArea(
    value: TextFieldValue,
    bodyFont: androidx.compose.ui.text.font.FontFamily,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMarkalonColors.current
    Box(modifier.fillMaxWidth().background(colors.bg)) {
        if (value.text.isEmpty()) {
            Text(
                "Start writing in markdown…",
                color = colors.fg3,
                fontFamily = bodyFont,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 18.dp, top = 18.dp),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = colors.fg1,
                fontFamily = bodyFont,
                fontSize = 15.sp,
                lineHeight = (15 * 1.75f).sp,
            ),
            cursorBrush = SolidColor(LocalAccent.current),
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 10.dp),
        )
    }
}

@Composable
private fun PreviewPane(
    markdown: String,
    bodyFont: androidx.compose.ui.text.font.FontFamily,
    topBorder: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMarkalonColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg)
            .verticalScroll(rememberScrollState()),
    ) {
        if (topBorder) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        }
        MarkdownView(
            markdown = markdown,
            bodyFont = bodyFont,
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 30.dp),
        )
    }
}

@Composable
private fun EditorTopBar(
    noteId: String,
    initialTitle: String,
    saved: Boolean,
    view: ViewMode,
    wordCount: Int,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onSetView: (ViewMode) -> Unit,
    onOpenMenu: () -> Unit,
    menuOpen: Boolean,
    onCloseMenu: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit,
    onCategory: () -> Unit,
) {
    val colors = LocalMarkalonColors.current
    var titleValue by remember(noteId) {
        mutableStateOf(TextFieldValue(initialTitle, TextRange(initialTitle.length)))
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Icons.Filled.ArrowBack, "Back", onBack)
            Box(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                if (titleValue.text.isEmpty()) {
                    Text("Untitled", color = colors.fg3, fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                }
                BasicTextField(
                    value = titleValue,
                    onValueChange = { titleValue = it; onTitleChange(it.text) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.fg1, fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
                    cursorBrush = SolidColor(LocalAccent.current),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SavedIndicator(saved)
            Box {
                IconBtn(Icons.Filled.MoreVert, "More", onOpenMenu)
                OverflowMenu(
                    expanded = menuOpen,
                    onDismiss = onCloseMenu,
                    onImport = onImport,
                    onExport = onExport,
                    onSettings = onSettings,
                    onDelete = onDelete,
                    onCategory = onCategory,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentedControl(
                options = listOf(ViewMode.RAW, ViewMode.MIXED, ViewMode.RENDERED),
                selected = view,
                onSelect = onSetView,
                modifier = Modifier.weight(1f),
            ) { mode -> SegmentLabel(mode.label, active = mode == view) }
            Text(
                "$wordCount words",
                color = colors.fg3,
                fontFamily = UiFontFamily,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun SavedIndicator(saved: Boolean) {
    val colors = LocalMarkalonColors.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (saved) SavedGreen else colors.fg3)
        )
        Spacer(Modifier.size(5.dp))
        Text(
            if (saved) "Saved" else "Saving…",
            color = colors.fg2,
            fontFamily = UiFontFamily,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun OverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit,
    onCategory: () -> Unit,
) {
    val colors = LocalMarkalonColors.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(colors.surface),
    ) {
        MenuRow(Icons.Filled.Label, "Category…", colors.fg1) { onDismiss(); onCategory() }
        MenuRow(Icons.Filled.FileOpen, "Open from device", colors.fg1) { onDismiss(); onImport() }
        MenuRow(Icons.Filled.Download, "Export as .md", colors.fg1) { onDismiss(); onExport() }
        MenuRow(Icons.Filled.Settings, "Settings", colors.fg1) { onDismiss(); onSettings() }
        HorizontalDivider(color = colors.border)
        MenuRow(Icons.Filled.Delete, "Delete note", DeleteRed) { onDismiss(); onDelete() }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
        Spacer(Modifier.size(12.dp))
        Text(label, color = tint, fontFamily = UiFontFamily, fontSize = 14.5f.sp)
    }
}

@Composable
private fun IconBtn(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = LocalMarkalonColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = colors.fg2, modifier = Modifier.size(21.dp))
    }
}
