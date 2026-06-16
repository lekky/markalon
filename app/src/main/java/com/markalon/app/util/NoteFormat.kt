package com.markalon.app.util

/** Word count: trimmed, split on whitespace. */
fun wordCount(body: String): Int {
    val t = body.trim()
    if (t.isEmpty()) return 0
    return t.split(Regex("\\s+")).size
}

/** First non-empty line with common markdown syntax stripped, for list snippets. */
fun snippet(body: String): String {
    if (body.isBlank()) return "Empty note"
    for (line in body.split("\n")) {
        var t = line.replace(Regex("^[#>\\s]*"), "")
        t = t.replace(Regex("^[-+*]\\s*\\[[ xX]\\]\\s*"), "") // task marker
        t = t.replace(Regex("^[-+*]\\s+"), "")                 // bullet
        t = t.replace(Regex("^\\d+\\.\\s+"), "")               // ordered
        t = t.replace(Regex("[*_`~]"), "")                      // emphasis/code marks
        t = t.replace(Regex("!?\\[([^\\]]*)\\]\\([^)]*\\)"), "$1") // links/images -> label
        t = t.trim()
        if (t.isNotEmpty()) return t
    }
    return "Empty note"
}

/** Compact relative time like "5m ago", "3h ago", "2d ago". */
fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - timestamp).coerceAtLeast(0)
    val sec = diff / 1000
    val min = sec / 60
    val hour = min / 60
    val day = hour / 24
    return when {
        sec < 45 -> "just now"
        min < 60 -> "${min}m ago"
        hour < 24 -> "${hour}h ago"
        day < 7 -> "${day}d ago"
        else -> "${day / 7}w ago"
    }
}
