package com.ugurbuga.previo.runtime

import androidx.compose.runtime.Composable

data class PreviewEntry(
    val id: String,
    val name: String,
    val group: String,
    val order: Int,
    val alphabeticalOrder: String,
    val customInfo: String,
    val isVerticallyScrollable: Boolean = false,
    val isHorizontallyScrollable: Boolean = false,
    val content: @Composable () -> Unit
)
