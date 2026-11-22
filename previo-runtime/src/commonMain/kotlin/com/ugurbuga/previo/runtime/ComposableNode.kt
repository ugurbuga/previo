package com.ugurbuga.previo.runtime

data class ComposableNode(
    val name: String,
    val isNative: Boolean,
    val children: List<ComposableNode> = emptyList()
)
