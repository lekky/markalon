package com.markalon.app.data

import kotlinx.serialization.Serializable

/** A single markdown note. [category] is the optional category name (null = uncategorized). */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val created: Long,
    val updated: Long,
    val category: String? = null
)
