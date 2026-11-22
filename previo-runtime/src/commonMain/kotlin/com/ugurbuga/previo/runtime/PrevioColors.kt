package com.ugurbuga.previo.runtime

import androidx.compose.ui.graphics.Color

object PrevioColorConstants {
    val White = Color.White
    val Black = Color.Black
    val DarkCanvas = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val LightGrayText = Color(0xFF6B7280)
    val DarkGrayText = Color(0xFF9CA3AF)
    val LightBorder = Color(0xFFE5E7EB)
    val DarkBorder = Color(0xFF374151)
    val LightAccent = Color(0xFFF3F4F6)
    val DarkAccent = Color(0xFF2E3545)
    val CustomDarkBg = Color(0xFF1E1B4B)
    val CustomLightText = Color(0xFFEEF2FF)
    val CustomSubText = Color(0xFFC7D2FE)
    val CustomAccent = Color(0xFF3730A3)
}

data class PrevioColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val accent: Color,
    val icon: Color
)

object PrevioDefaults {
    val LightColors = PrevioColors(
        background = PrevioColorConstants.White,
        surface = PrevioColorConstants.White,
        textPrimary = PrevioColorConstants.Black,
        textSecondary = PrevioColorConstants.LightGrayText,
        border = PrevioColorConstants.LightBorder,
        accent = PrevioColorConstants.LightAccent,
        icon = PrevioColorConstants.Black
    )

    val DarkColors = PrevioColors(
        background = PrevioColorConstants.DarkCanvas,
        surface = PrevioColorConstants.DarkSurface,
        textPrimary = PrevioColorConstants.White,
        textSecondary = PrevioColorConstants.DarkGrayText,
        border = PrevioColorConstants.DarkBorder,
        accent = PrevioColorConstants.DarkAccent,
        icon = PrevioColorConstants.White
    )
}
