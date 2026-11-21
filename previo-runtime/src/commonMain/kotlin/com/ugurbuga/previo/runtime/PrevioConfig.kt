package com.ugurbuga.previo.runtime

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class PrevioConfig(
    val title: String = "Previo Explorer",
    val primaryColor: Color? = null,
    val colorScheme: ColorScheme? = null,
    val typography: Typography? = null,
    val isDarkTheme: Boolean? = null,
    val customHeader: (@Composable () -> Unit)? = null
)
