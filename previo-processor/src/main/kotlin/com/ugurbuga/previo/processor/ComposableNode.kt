package com.ugurbuga.previo.processor

data class ComposableNode(
    val name: String,
    val isNative: Boolean,
    val children: List<ComposableNode> = emptyList(),
    /** Slot name in the parent's param list, e.g. "topBar", "navigationIcon", "title", "content" */
    val slotOrRole: String? = null,
    /** Short literal info e.g. the string from Text("Hello") */
    val infoOrText: String? = null,
)
