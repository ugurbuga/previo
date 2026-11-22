package com.ugurbuga.previo.runtime

import androidx.compose.ui.graphics.Color

object PrevioColorConstants {
    // Sleek Modern Light Palette
    val LightScreenBackground = Color(0xFFF8FAFC)
    val LightCardBackground = Color(0xFFFFFFFF)
    val LightPrimaryText = Color(0xFF0F172A)
    val LightSecondaryText = Color(0xFF64748B)
    val LightDividerOrBorder = Color(0xFFE2E8F0)
    val LightChipSelectedBackground = Color(0xFFF1F5F9)
    val LightChipSelectedText = Color(0xFF0F172A)
    val LightIconTint = Color(0xFF334155)
    val LightBadgeBackground = Color(0xFFF1F5F9)
    val LightBadgeText = Color(0xFF334155)

    // Sleek Modern Dark Palette
    val DarkScreenBackground = Color(0xFF0B0F19)
    val DarkCardBackground = Color(0xFF111827)
    val DarkPrimaryText = Color(0xFFF9FAFB)
    val DarkSecondaryText = Color(0xFF9CA3AF)
    val DarkDividerOrBorder = Color(0xFF1F2937)
    val DarkChipSelectedBackground = Color(0xFF1F2937)
    val DarkChipSelectedText = Color(0xFFF9FAFB)
    val DarkIconTint = Color(0xFFD1D5DB)
    val DarkBadgeBackground = Color(0xFF1F2937)
    val DarkBadgeText = Color(0xFFE5E7EB)

    val CustomDarkBg = Color(0xFF0B0F19)
    val CustomLightText = Color(0xFFF9FAFB)
    val CustomSubText = Color(0xFF9CA3AF)
    val CustomAccent = Color(0xFF1F2937)
}

data class PrevioColors(
    val screenBackground: Color,
    val cardBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val dividerOrBorder: Color,
    val chipSelectedBackground: Color,
    val chipSelectedText: Color,
    val iconTint: Color,
    val badgeBackground: Color,
    val badgeText: Color
)

object PrevioDefaults {
    val LightColors = PrevioColors(
        screenBackground = PrevioColorConstants.LightScreenBackground,
        cardBackground = PrevioColorConstants.LightCardBackground,
        primaryText = PrevioColorConstants.LightPrimaryText,
        secondaryText = PrevioColorConstants.LightSecondaryText,
        dividerOrBorder = PrevioColorConstants.LightDividerOrBorder,
        chipSelectedBackground = PrevioColorConstants.LightChipSelectedBackground,
        chipSelectedText = PrevioColorConstants.LightChipSelectedText,
        iconTint = PrevioColorConstants.LightIconTint,
        badgeBackground = PrevioColorConstants.LightBadgeBackground,
        badgeText = PrevioColorConstants.LightBadgeText
    )

    val DarkColors = PrevioColors(
        screenBackground = PrevioColorConstants.DarkScreenBackground,
        cardBackground = PrevioColorConstants.DarkCardBackground,
        primaryText = PrevioColorConstants.DarkPrimaryText,
        secondaryText = PrevioColorConstants.DarkSecondaryText,
        dividerOrBorder = PrevioColorConstants.DarkDividerOrBorder,
        chipSelectedBackground = PrevioColorConstants.DarkChipSelectedBackground,
        chipSelectedText = PrevioColorConstants.DarkChipSelectedText,
        iconTint = PrevioColorConstants.DarkIconTint,
        badgeBackground = PrevioColorConstants.DarkBadgeBackground,
        badgeText = PrevioColorConstants.DarkBadgeText
    )
}
