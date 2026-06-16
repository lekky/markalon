package com.markalon.app.data

import kotlinx.serialization.Serializable

/** A single markdown note. Mirrors the prototype's `{ id, title, body, created, updated }`. */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val created: Long,
    val updated: Long
)
