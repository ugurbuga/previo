package com.ugurbuga.previo.processor

data class ComposableNode(
    val name: String,
    val isNative: Boolean,
    val children: List<ComposableNode> = emptyList()
)
