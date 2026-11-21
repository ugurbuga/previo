package com.ugurbuga.previo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.ugurbuga.previo.runtime.GeneratedPreviewRegistry
import com.ugurbuga.previo.runtime.PreviewExplorer
import com.ugurbuga.previo.runtime.PrevioConfig
import com.ugurbuga.previo.theme.AppTypography
import com.ugurbuga.previo.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        var showPreviewExplorer by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showPreviewExplorer) {
                PreviewExplorerPlaceholder(
                    onBack = { showPreviewExplorer = false }
                )
            } else {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .safeContentPadding()
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showPreviewExplorer = true },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Open Preview Explorer")
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewExplorerPlaceholder(onBack: () -> Unit) {
    val customConfig = remember {
        PrevioConfig(
            title = "Previo Component Catalog",
            typography = AppTypography
        )
    }

    // Touch GeneratedPreviewRegistry directly in commonMain across iOS, Android, Desktop, and Web
    remember {
        GeneratedPreviewRegistry.touch()
    }

    PreviewExplorer(
        onBack = onBack,
        config = customConfig
    )
}
