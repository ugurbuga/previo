package com.ugurbuga.previo.runtime

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun PrevioBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
