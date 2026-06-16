package com.markalon.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.markalon.app.data.MarkalonRepository
import com.markalon.app.data.Note
import com.markalon.app.data.Settings
import com.markalon.app.data.ToolItem
import com.markalon.app.data.ViewMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen { LIBRARY, EDITOR }

data class MarkalonUiState(
    val loaded: Boolean = false,
    val notes: List<Note> = emptyList(),
    val settings: Settings = Settings(),
    val screen: Screen = Screen.LIBRARY,
    val activeId: String? = null,
    val view: ViewMode = ViewMode.MIXED,
    val search: String = "",
    val settingsOpen: Boolean = false,
    val menuOpen: Boolean = false,
    val saved: Boolean = true,
    val toast: String? = null,
) {
    val activeNote: Note? get() = notes.firstOrNull { it.id == activeId }

    val filteredNotes: List<Note>
        get() {
            val q = search.trim().lowercase()
            val base = notes.sortedByDescending { it.updated }
            if (q.isEmpty()) return base
            return base.filter {
                it.title.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
        }
}

private class NoteHistory {
    val past = ArrayDeque<String>()
    val future = ArrayDeque<String>()
    var lastEdit = 0L
}

class MarkalonViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MarkalonRepository(app)
    private val _state = MutableStateFlow(MarkalonUiState())
    val state: StateFlow<MarkalonUiState> = _state.asStateFlow()

    private val history = mutableMapOf<String, NoteHistory>()
    private var saveJob: Job? = null
    private var toastJob: Job? = null

    init {
        viewModelScope.launch {
            val notes = repo.notes.first()
            val settings = repo.settings.first()
            _state.update {
                it.copy(loaded = true, notes = notes, settings = settings, view = settings.defaultViewMode)
            }
        }
    }

    // ---------- navigation ----------
    fun openNote(id: String) = _state.update {
        it.copy(screen = Screen.EDITOR, activeId = id, view = it.settings.defaultViewMode, menuOpen = false)
    }

    fun newNote() {
        val now = System.currentTimeMillis()
        val note = Note(id = "d-" + UUID.randomUUID(), title = "Untitled", body = "", created = now, updated = now)
        _state.update {
            it.copy(
                notes = listOf(note) + it.notes,
                screen = Screen.EDITOR,
                activeId = note.id,
                view = it.settings.defaultViewMode,
                menuOpen = false,
                search = "",
            )
        }
        persistNotes()
    }

    fun back() = _state.update { it.copy(screen = Screen.LIBRARY, menuOpen = false) }

    fun setView(view: ViewMode) = _state.update { it.copy(view = view) }
    fun setSearch(q: String) = _state.update { it.copy(search = q) }
    fun openMenu() = _state.update { it.copy(menuOpen = true) }
    fun closeMenu() = _state.update { it.copy(menuOpen = false) }
    fun openSettings() = _state.update { it.copy(settingsOpen = true, menuOpen = false) }
    fun closeSettings() = _state.update { it.copy(settingsOpen = false) }

    // ---------- editing ----------
    fun updateTitle(title: String) {
        val id = _state.value.activeId ?: return
        mutateNote(id) { it.copy(title = title, updated = System.currentTimeMillis()) }
        scheduleSave()
    }

    /** Updates the active note body, recording an undo entry (coalesced). */
    fun updateBody(newBody: String) {
        val id = _state.value.activeId ?: return
        val current = _state.value.activeNote?.body ?: ""
        if (newBody == current) return
        recordHistory(id, current)
        mutateNote(id) { it.copy(body = newBody, updated = System.currentTimeMillis()) }
        scheduleSave()
    }

    fun undo(): String? {
        val id = _state.value.activeId ?: return null
        val h = history[id] ?: return null
        if (h.past.isEmpty()) return null
        val current = _state.value.activeNote?.body ?: ""
        h.future.addLast(current)
        val prev = h.past.removeLast()
        mutateNote(id) { it.copy(body = prev, updated = System.currentTimeMillis()) }
        scheduleSave()
        return prev
    }

    fun redo(): String? {
        val id = _state.value.activeId ?: return null
        val h = history[id] ?: return null
        if (h.future.isEmpty()) return null
        val current = _state.value.activeNote?.body ?: ""
        h.past.addLast(current)
        val next = h.future.removeLast()
        mutateNote(id) { it.copy(body = next, updated = System.currentTimeMillis()) }
        scheduleSave()
        return next
    }

    private fun recordHistory(id: String, prev: String) {
        val h = history.getOrPut(id) { NoteHistory() }
        val now = System.currentTimeMillis()
        if (now - h.lastEdit > 500) {
            h.past.addLast(prev)
            if (h.past.size > 120) h.past.removeFirst()
            h.future.clear()
        }
        h.lastEdit = now
    }

    // ---------- notes io ----------
    fun deleteActiveNote() {
        val id = _state.value.activeId ?: return
        _state.update {
            it.copy(
                notes = it.notes.filterNot { n -> n.id == id },
                screen = Screen.LIBRARY,
                activeId = null,
                menuOpen = false,
                toast = "Note deleted",
            )
        }
        history.remove(id)
        persistNotes()
        scheduleToastClear()
    }

    fun importNote(name: String, body: String) {
        val now = System.currentTimeMillis()
        val title = name.replace(Regex("\\.(md|markdown|txt)$", RegexOption.IGNORE_CASE), "").ifBlank { "Imported note" }
        val note = Note(id = "d-" + UUID.randomUUID(), title = title, body = body, created = now, updated = now)
        _state.update {
            it.copy(
                notes = listOf(note) + it.notes,
                screen = Screen.EDITOR,
                activeId = note.id,
                view = it.settings.defaultViewMode,
                menuOpen = false,
                settingsOpen = false,
                toast = "Opened $name",
            )
        }
        persistNotes()
        scheduleToastClear()
    }

    fun showToast(message: String) {
        _state.update { it.copy(toast = message) }
        scheduleToastClear()
    }

    // ---------- settings ----------
    fun updateSettings(transform: (Settings) -> Settings) {
        _state.update {
            val newSettings = transform(it.settings)
            // keep current view in sync if default view changed while on it is handled by caller
            it.copy(settings = newSettings)
        }
        persistSettings()
    }

    fun setDefaultView(view: ViewMode) {
        _state.update { it.copy(settings = it.settings.copy(defaultView = view.id), view = view) }
        persistSettings()
    }

    fun setToolbar(toolbar: List<ToolItem>) = updateSettings { it.copy(toolbar = toolbar) }

    // ---------- helpers ----------
    private inline fun mutateNote(id: String, crossinline transform: (Note) -> Note) {
        _state.update { s -> s.copy(notes = s.notes.map { if (it.id == id) transform(it) else it }) }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        _state.update { it.copy(saved = false) }
        saveJob = viewModelScope.launch {
            delay(550)
            repo.saveNotes(_state.value.notes)
            _state.update { it.copy(saved = true) }
        }
    }

    private fun persistNotes() {
        viewModelScope.launch { repo.saveNotes(_state.value.notes) }
    }

    private fun persistSettings() {
        viewModelScope.launch { repo.saveSettings(_state.value.settings) }
    }

    private fun scheduleToastClear() {
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            delay(2000)
            _state.update { it.copy(toast = null) }
        }
    }
}
