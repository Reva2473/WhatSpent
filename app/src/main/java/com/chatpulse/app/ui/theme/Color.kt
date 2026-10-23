package com.chatpulse.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// Emerald / Teal Primary
val EmeraldPrimaryLight = Color(0xFF0D6E4C)
val OnEmeraldPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFA6F5D0)
val OnEmeraldPrimaryContainerLight = Color(0xFF002114)

val EmeraldPrimaryDark = Color(0xFF4EE2A8)
val OnEmeraldPrimaryDark = Color(0xFF003822)
val EmeraldPrimaryContainerDark = Color(0xFF005234)
val OnEmeraldPrimaryContainerDark = Color(0xFFA6F5D0)

// Electric Indigo Secondary
val IndigoSecondaryLight = Color(0xFF4C58A9)
val OnIndigoSecondaryLight = Color(0xFFFFFFFF)
val IndigoSecondaryContainerLight = Color(0xFFDFE0FF)
val OnIndigoSecondaryContainerLight = Color(0xFF00105C)

val IndigoSecondaryDark = Color(0xFFBDC2FF)
val OnIndigoSecondaryDark = Color(0xFF1B2778)
val IndigoSecondaryContainerDark = Color(0xFF343F90)
val OnIndigoSecondaryContainerDark = Color(0xFFDFE0FF)

// Vibrant Cyan Tertiary
val CyanTertiaryLight = Color(0xFF006877)
val OnCyanTertiaryLight = Color(0xFFFFFFFF)
val CyanTertiaryContainerLight = Color(0xFFA1EFFF)
val OnCyanTertiaryContainerLight = Color(0xFF001F25)

val CyanTertiaryDark = Color(0xFF83D5E7)
val OnCyanTertiaryDark = Color(0xFF00363F)
val CyanTertiaryContainerDark = Color(0xFF004E5A)
val OnCyanTertiaryContainerDark = Color(0xFFA1EFFF)

// Neutral & Surfaces Light
val SurfaceLight = Color(0xFFF7FBF6)
val OnSurfaceLight = Color(0xFF181D1A)
val SurfaceVariantLight = Color(0xFFDCE5DD)
val OnSurfaceVariantLight = Color(0xFF404944)
val BackgroundLight = Color(0xFFF7FBF6)
val OnBackgroundLight = Color(0xFF181D1A)
val OutlineLight = Color(0xFF707973)
val OutlineVariantLight = Color(0xFFC0C9C2)

// Neutral & Surfaces Dark
val SurfaceDark = Color(0xFF101412)
val OnSurfaceDark = Color(0xFFE0E3DE)
val SurfaceVariantDark = Color(0xFF1E2622)
val OnSurfaceVariantDark = Color(0xFFBFC9C2)
val BackgroundDark = Color(0xFF0B0F0D)
val OnBackgroundDark = Color(0xFFE0E3DE)
val OutlineDark = Color(0xFF8A938C)
val OutlineVariantDark = Color(0xFF404944)

// Surface Container levels for M3 hierarchy
val SurfaceContainerLowestDark = Color(0xFF090D0B)
val SurfaceContainerLowDark = Color(0xFF141916)
val SurfaceContainerDark = Color(0xFF181D1A)
val SurfaceContainerHighDark = Color(0xFF222824)
val SurfaceContainerHighestDark = Color(0xFF2D332F)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF2F6F1)
val SurfaceContainerLight = Color(0xFFECF0EB)
val SurfaceContainerHighLight = Color(0xFFE6EAE5)
val SurfaceContainerHighestLight = Color(0xFFE0E4DF)

// Error Colors
val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// Live Pulse Green Indicator
val LivePulseGreen = Color(0xFF22C55E)
val AlertWarningAmber = Color(0xFFF59E0B)

/**
 * Curated list of 8 harmonious Material 3 chart colors for donut and bar charts.
 */
val ChatChartPalette: List<Color> = listOf(
    Color(0xFF10B981), // 0: Emerald Green
    Color(0xFF6366F1), // 1: Electric Indigo
    Color(0xFF0EA5E9), // 2: Sky Blue
    Color(0xFFF59E0B), // 3: Amber Gold
    Color(0xFFF43F5E), // 4: Rose Coral
    Color(0xFF8B5CF6), // 5: Violet Purple
    Color(0xFF14B8A6), // 6: Mint Teal
    Color(0xFFF97316)  // 7: Orange Tangerine
)

/**
 * Returns a consistent, vibrant color for a chat.
 * If customColorTag is specified, converts it to Color.
 * Otherwise, maps the chatName hash deterministically to ChatChartPalette.
 */
fun getChatColor(chatName: String, customColorTag: Int?): Color {
    if (customColorTag != null) {
        return Color(customColorTag)
    }
    val hash = abs(chatName.hashCode())
    return ChatChartPalette[hash % ChatChartPalette.size]
}
