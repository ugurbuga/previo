package com.ugurbuga.previo.runtime

import androidx.compose.runtime.mutableStateListOf

object PrevioRegistry {
    private val _previews = mutableStateListOf<PreviewEntry>()
    val previews: List<PreviewEntry> get() = _previews

    fun register(entries: List<PreviewEntry>) {
        for (entry in entries) {
            if (_previews.none { it.id == entry.id }) {
                _previews.add(entry)
            }
        }
    }
}
