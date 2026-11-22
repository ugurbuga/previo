package com.ugurbuga.previo.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun PrevioOverlay(
    config: PrevioConfig = PrevioConfig(),
    content: @Composable () -> Unit
) {
    val isPrevioOpen by Previo.isPrevioOpen.collectAsState()
    val colors = config.getColors()

    PrevioBackHandler(enabled = isPrevioOpen) {
        Previo.dismiss()
    }

    if (isPrevioOpen) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            PreviewExplorer(
                onBack = { Previo.dismiss() },
                config = config
            )
        }
    } else {
        content()
    }
}
