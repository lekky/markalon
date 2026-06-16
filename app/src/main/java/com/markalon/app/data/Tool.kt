package com.markalon.app.data

import kotlinx.serialization.Serializable

/** The 16 insert-toolbar tools, in their canonical id order. */
enum class Tool(val id: String, val label: String) {
    H1("h1", "Heading 1"),
    H2("h2", "Heading 2"),
    H3("h3", "Heading 3"),
    BOLD("bold", "Bold"),
    ITALIC("italic", "Italic"),
    STRIKE("strike", "Strikethrough"),
    UL("ul", "Bullet list"),
    OL("ol", "Numbered list"),
    TASK("task", "Task list"),
    CODE("code", "Inline code"),
    CODEBLOCK("codeblock", "Code block"),
    QUOTE("quote", "Quote"),
    TABLE("table", "Table"),
    HR("hr", "Divider"),
    UNDO("undo", "Undo"),
    REDO("redo", "Redo");

    companion object {
        fun fromId(id: String): Tool? = entries.firstOrNull { it.id == id }
    }
}

/** A toolbar entry: which tool, and whether it is enabled (shown in the editor). */
@Serializable
data class ToolItem(
    val id: String,
    val enabled: Boolean
)

/** Default toolbar order + enabled state, matching the handoff's `defaultToolbar`. */
val defaultToolbar: List<ToolItem> = listOf(
    ToolItem("h1", true), ToolItem("h2", true), ToolItem("h3", false),
    ToolItem("bold", true), ToolItem("italic", true), ToolItem("strike", false),
    ToolItem("ul", true), ToolItem("ol", true), ToolItem("task", true),
    ToolItem("code", true), ToolItem("codeblock", false), ToolItem("quote", true),
    ToolItem("table", true), ToolItem("hr", false), ToolItem("undo", true), ToolItem("redo", true)
)
