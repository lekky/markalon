package com.markalon.app.editor

import com.markalon.app.data.Tool

/** Result of an insert: the new body text and where the selection should land. */
data class InsertResult(val text: String, val selStart: Int, val selEnd: Int)

/**
 * Applies a toolbar insert to [text] given the current selection [start, end).
 * Ported from the prototype's `applyEdit`. Undo/Redo return null (handled by
 * the per-note history controller, not by text transformation).
 */
fun applyInsert(tool: Tool, text: String, start: Int, end: Int): InsertResult? {
    val s = start.coerceIn(0, text.length)
    val e = end.coerceIn(s, text.length)
    val sel = text.substring(s, e)
    val before = text.substring(0, s)
    val after = text.substring(e)

    fun wrap(pre: String, post: String, placeholder: String): InsertResult {
        val inner = sel.ifEmpty { placeholder }
        val nt = before + pre + inner + post + after
        val ns = before.length + pre.length
        return InsertResult(nt, ns, ns + inner.length)
    }

    fun linePre(pre: String): InsertResult {
        val ls = before.lastIndexOf('\n') + 1
        val nt = text.substring(0, ls) + pre + text.substring(ls)
        val ns = s + pre.length
        return InsertResult(nt, ns, ns)
    }

    fun atLineStartPad(): String = if (before.isNotEmpty() && !before.endsWith("\n")) "\n" else ""

    return when (tool) {
        Tool.H1 -> linePre("# ")
        Tool.H2 -> linePre("## ")
        Tool.H3 -> linePre("### ")
        Tool.BOLD -> wrap("**", "**", "bold text")
        Tool.ITALIC -> wrap("*", "*", "italic")
        Tool.STRIKE -> wrap("~~", "~~", "strikethrough")
        Tool.UL -> linePre("- ")
        Tool.OL -> linePre("1. ")
        Tool.TASK -> linePre("- [ ] ")
        Tool.CODE -> wrap("`", "`", "code")
        Tool.CODEBLOCK -> {
            val inner = sel.ifEmpty { "code" }
            val pad = atLineStartPad()
            val nt = before + pad + "```\n" + inner + "\n```\n" + after
            val ns = before.length + pad.length + 4 // "```\n".length
            InsertResult(nt, ns, ns + inner.length)
        }
        Tool.QUOTE -> linePre("> ")
        Tool.TABLE -> {
            val tbl = atLineStartPad() + "| Column | Column |\n| --- | --- |\n| Cell | Cell |\n"
            val nt = before + tbl + after
            val pos = before.length + tbl.length
            InsertResult(nt, pos, pos)
        }
        Tool.HR -> {
            val r = atLineStartPad() + "---\n"
            val nt = before + r + after
            val pos = before.length + r.length
            InsertResult(nt, pos, pos)
        }
        Tool.UNDO, Tool.REDO -> null
    }
}
