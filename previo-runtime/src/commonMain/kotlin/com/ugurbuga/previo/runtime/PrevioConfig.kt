package com.ugurbuga.previo.runtime

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class PrevioConfig(
    val title: String = "Previo Explorer",
    val enabled: Boolean = true,
    val collectPreviews: Boolean = true,
    val collectHierarchyTree: Boolean = true,
    val colors: PrevioColors? = null,
    val colorScheme: ColorScheme? = null,
    val typography: Typography? = null,
    val isDarkTheme: Boolean = false,
    val showExtraTreeInfo: Boolean = false
) {
    @Composable
    fun getColors(): PrevioColors {
        return remember(this) {
            colors ?: if (isDarkTheme) PrevioDefaults.DarkColors else PrevioDefaults.LightColors
        }
    }
}
