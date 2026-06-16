package com.markalon.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "markalon")

/** Persists notes and settings locally via DataStore (JSON-encoded). Local-first, no cloud. */
class MarkalonRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val docsKey = stringPreferencesKey("docs")
    private val settingsKey = stringPreferencesKey("settings")

    val notes: Flow<List<Note>> = context.dataStore.data.map { prefs ->
        val raw = prefs[docsKey]
        val list = if (raw.isNullOrBlank()) null else runCatching {
            json.decodeFromString<List<Note>>(raw)
        }.getOrNull()
        (if (list.isNullOrEmpty()) seedDocs() else list).sortedByDescending { it.updated }
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        val raw = prefs[settingsKey]
        if (raw.isNullOrBlank()) Settings()
        else runCatching { json.decodeFromString<Settings>(raw) }.getOrDefault(Settings())
    }

    suspend fun saveNotes(notes: List<Note>) {
        context.dataStore.edit { it[docsKey] = json.encodeToString(notes) }
    }

    suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { it[settingsKey] = json.encodeToString(settings) }
    }

    companion object {
        private const val MIN = 60_000L
        private const val HOUR = 3_600_000L

        fun seedDocs(): List<Note> {
            val now = System.currentTimeMillis()
            val welcome = listOf(
                "# Welcome to Markalon",
                "",
                "_Markdown, made beautiful._",
                "",
                "A fast, **local-first** markdown editor. Everything you write saves automatically — no account, no cloud.",
                "",
                "## Three ways to view",
                "- **Raw** — write with a customisable insert toolbar",
                "- **Mixed** — editor on top, live preview below",
                "- **Rendered** — a clean reading mode",
                "",
                "> Tip: open the ⋯ menu to export a note as a `.md` file, or import one from your device.",
                "",
                "### Cheat sheet",
                "",
                "| Element | Syntax |",
                "| --- | --- |",
                "| Bold | `**text**` |",
                "| Italic | `*text*` |",
                "| Link | `[label](url)` |",
                "",
                "- [x] Auto-save to local storage",
                "- [x] Light & dark themes",
                "- [ ] Cloud sync",
                "",
                "```",
                "const note = \"happy writing\";",
                "console.log(note);",
                "```",
                "",
                "---",
                "",
                "Made for thinking out loud."
            ).joinToString("\n")

            val meeting = listOf(
                "# Q3 Planning",
                "_Thursday standup · Mei, Sam, Priya_",
                "",
                "## Decisions",
                "1. Ship the editor beta by **August 9th**",
                "2. Freeze scope on sync until Q4",
                "",
                "## Action items",
                "- [x] Draft the launch checklist",
                "- [ ] Wire up export to `.md`",
                "- [ ] Usability test the toolbar",
                "",
                "> Parking lot: revisit folder support next sprint."
            ).joinToString("\n")

            val reading = listOf(
                "# Reading list",
                "",
                "Things to get through this month.",
                "",
                "## Articles",
                "- [Designing for thumbs](https://example.com)",
                "- [Markdown, a retrospective](https://example.com)",
                "",
                "## Books",
                "- *The Craft of Writing* — ch. 3–5",
                "- **Refactoring UI** — skim",
                "",
                "~~Watch that 2h talk~~ done."
            ).joinToString("\n")

            val parser = listOf(
                "# Parser notes",
                "",
                "How the renderer turns text into a tree.",
                "",
                "## Pipeline",
                "1. Tokenise the source",
                "2. Build a block AST",
                "3. Walk inlines per block",
                "",
                "```",
                "Document",
                "└── Paragraph",
                "    └── Text",
                "```",
                "",
                "> Keep blocks and inlines separate."
            ).joinToString("\n")

            return listOf(
                Note("d-welcome", "Welcome to Markalon", welcome, now, now - 5 * MIN),
                Note("d-meeting", "Q3 Planning — notes", meeting, now - 3 * HOUR, now - 3 * HOUR),
                Note("d-reading", "Reading list", reading, now - 26 * HOUR, now - 26 * HOUR),
                Note("d-parser", "Parser README", parser, now - 50 * HOUR, now - 50 * HOUR),
            )
        }
    }
}
