package com.markalon.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markalon.app.data.EditorFont
import com.markalon.app.data.ThemeMode
import com.markalon.app.data.ViewMode
import com.markalon.app.editor.EditorScreen
import com.markalon.app.library.LibraryScreen
import com.markalon.app.settings.SettingsSheet
import com.markalon.app.ui.theme.LocalMarkalonColors
import com.markalon.app.ui.theme.MarkalonTheme
import com.markalon.app.ui.theme.UiFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MarkalonApp(vm: MarkalonViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    MarkalonTheme(settings = state.settings) {
        val colors = LocalMarkalonColors.current
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val activeNote by rememberUpdatedState(state.activeNote)

        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    val (name, text) = withContext(Dispatchers.IO) { readDocument(context, uri) }
                    vm.importNote(name, text)
                }
            }
        }

        val exportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/markdown")
        ) { uri: Uri? ->
            val note = activeNote
            if (uri != null && note != null) {
                scope.launch {
                    withContext(Dispatchers.IO) { writeDocument(context, uri, note.body) }
                    vm.showToast("Exported ${sanitizeFileName(note.title)}.md")
                }
            }
        }

        // System back: on the Editor, return to the Library instead of exiting the app.
        // (The Settings sheet and overflow menu handle their own back-dismiss.)
        BackHandler(enabled = state.screen == Screen.EDITOR && !state.settingsOpen) {
            vm.back()
        }

        Box(Modifier.fillMaxSize().background(colors.bg)) {
            Box(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
            if (state.loaded) {
                when (state.screen) {
                    Screen.LIBRARY -> LibraryScreen(
                        notes = state.filteredNotes,
                        totalCount = state.notes.size,
                        search = state.search,
                        onSearch = vm::setSearch,
                        onOpenNote = vm::openNote,
                        onNewNote = vm::newNote,
                        onImport = { importLauncher.launch(arrayOf("text/*")) },
                        onSettings = vm::openSettings,
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onSelectCategory = vm::selectCategory,
                        onAddCategory = vm::addCategory,
                        onRenameCategory = vm::renameCategory,
                        onDeleteCategory = vm::deleteCategory,
                    )

                    Screen.EDITOR -> {
                        val note = state.activeNote
                        if (note != null) {
                            EditorScreen(
                                note = note,
                                settings = state.settings,
                                view = state.view,
                                saved = state.saved,
                                menuOpen = state.menuOpen,
                                onBack = vm::back,
                                onTitleChange = vm::updateTitle,
                                onBodyChange = vm::updateBody,
                                onSetView = vm::setView,
                                onUndo = vm::undo,
                                onRedo = vm::redo,
                                onOpenMenu = vm::openMenu,
                                onCloseMenu = vm::closeMenu,
                                onImport = { importLauncher.launch(arrayOf("text/*")) },
                                onExport = { exportLauncher.launch("${sanitizeFileName(note.title)}.md") },
                                onSettings = vm::openSettings,
                                onDelete = vm::deleteActiveNote,
                                onSetCategory = vm::setActiveNoteCategory,
                            )
                        }
                    }
                }
            }
            }

            if (state.settingsOpen) {
                SettingsSheet(
                    settings = state.settings,
                    onDismiss = vm::closeSettings,
                    onTheme = { mode: ThemeMode -> vm.updateSettings { it.copy(theme = mode.id) } },
                    onAccent = { hex: String -> vm.updateSettings { it.copy(accent = hex) } },
                    onFont = { font: EditorFont -> vm.updateSettings { it.copy(font = font.id) } },
                    onDefaultView = { view: ViewMode -> vm.setDefaultView(view) },
                    onToolbar = vm::setToolbar,
                )
            }

            state.toast?.let { message ->
                Toast(message, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun Toast(message: String, modifier: Modifier = Modifier) {
    val colors = LocalMarkalonColors.current
    Box(modifier.padding(bottom = 40.dp)) {
        Text(
            text = message,
            color = colors.bg,
            fontFamily = UiFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5f.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(11.dp))
                .background(colors.fg1)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

private fun readDocument(context: Context, uri: Uri): Pair<String, String> {
    val text = context.contentResolver.openInputStream(uri)?.use {
        it.readBytes().toString(Charsets.UTF_8)
    } ?: ""
    var name = "Imported note"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) {
            cursor.getString(idx)?.let { name = it }
        }
    }
    return name to text
}

private fun writeDocument(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use {
        it.write(text.toByteArray(Charsets.UTF_8))
    }
}

private fun sanitizeFileName(title: String): String {
    val cleaned = title.trim().ifBlank { "note" }
        .replace(Regex("[^a-zA-Z0-9 _-]"), "")
        .replace(Regex("\\s+"), "-")
    return cleaned.ifBlank { "note" }
}
