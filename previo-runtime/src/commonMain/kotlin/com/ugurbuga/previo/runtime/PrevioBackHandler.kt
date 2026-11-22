package com.ugurbuga.previo.runtime

import androidx.compose.runtime.Composable

@Composable
expect fun PrevioBackHandler(enabled: Boolean, onBack: () -> Unit)
