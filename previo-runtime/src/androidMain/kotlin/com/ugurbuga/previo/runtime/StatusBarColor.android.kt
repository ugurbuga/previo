package com.ugurbuga.previo.runtime

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SetStatusBarColor(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val darkIcons = !isDarkTheme
            LaunchedEffect(darkIcons) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
            }
        }
    }
}
