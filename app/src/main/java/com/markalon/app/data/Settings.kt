package com.markalon.app.data

import kotlinx.serialization.Serializable

enum class ThemeMode(val id: String) {
    SYSTEM("system"), LIGHT("light"), DARK("dark");

    companion object {
        fun fromId(id: String?): ThemeMode = entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

enum class EditorFont(val id: String, val label: String, val fontName: String) {
    MONO("mono", "Monospace", "JetBrains Mono"),
    SANS("sans", "Sans-serif", "IBM Plex Sans"),
    SERIF("serif", "Serif", "Newsreader"),
    SYSTEM("system", "System default", "Device sans");

    companion object {
        fun fromId(id: String?): EditorFont = entries.firstOrNull { it.id == id } ?: MONO
    }
}

enum class ViewMode(val id: String, val label: String) {
    RAW("raw", "Raw"), MIXED("mixed", "Mixed"), RENDERED("rendered", "Rendered");

    companion object {
        fun fromId(id: String?): ViewMode = entries.firstOrNull { it.id == id } ?: MIXED
    }
}

/** All persisted user settings. */
@Serializable
data class Settings(
    val theme: String = ThemeMode.LIGHT.id,
    val font: String = EditorFont.MONO.id,
    val defaultView: String = ViewMode.MIXED.id,
    val accent: String = Accents.DEFAULT.hex,
    val toolbar: List<ToolItem> = defaultToolbar
) {
    val themeMode: ThemeMode get() = ThemeMode.fromId(theme)
    val editorFont: EditorFont get() = EditorFont.fromId(font)
    val defaultViewMode: ViewMode get() = ViewMode.fromId(defaultView)
}

data class Accent(val name: String, val hex: String)

object Accents {
    val ALL = listOf(
        Accent("Clay", "#D8623C"),
        Accent("Rose", "#E0407A"),
        Accent("Amber", "#E0922F"),
        Accent("Green", "#0E9F6E"),
        Accent("Teal", "#119DA4"),
        Accent("Blue", "#3B6FE0"),
        Accent("Violet", "#7C5CFF"),
        Accent("Graphite", "#5A5550"),
    )
    val DEFAULT = ALL.first()
}
