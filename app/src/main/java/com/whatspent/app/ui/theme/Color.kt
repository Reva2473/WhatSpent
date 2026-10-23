package com.whatspent.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// ============================================================================
// WHATSPENT DESIGN SYSTEM TOKENS (Obsidian + Warm Butter-Gold + Pastel Bento)
// ============================================================================

// Canvas & Dark Bento Surfaces
val ObsidianBackground = Color(0xFF0B0B0D)        // Primary app canvas
val CharcoalBentoSurface = Color(0xFF1F1F21)      // Dark bento cards
val ElevatedCharcoalSurface = Color(0xFF28282C)   // Elevated dark surface / borders
val DarkCircleButtonSurface = Color(0xFF262522)   // Circular dark icon button background
val DarkCircleButtonIcon = Color(0xFFD9D2B0)      // Warm sand/gold tint on dark circle buttons

// Primary Accent & Navigation
val ButterGoldAccent = Color(0xFFEFE29B)          // Warm butter-gold: FAB, active bottom nav indicator, tab badge
val OnButterGoldInk = Color(0xFF141413)           // Dark charcoal ink on ButterGoldAccent
val MutedChampagneIcon = Color(0xFF9E9B8E)        // Unselected nav icons & secondary muted icons

// Signature Pastel Bento Cards (with Dark Ink #121413 inside)
val SageMintHeroCard = Color(0xFFBCE0D0)          // Hero card ("Top 5 chats by spend")
val SageMintInnerCallout = Color(0xFFE8FCE2)      // Floating pill inside the semi-circular gauge arch
val WarmSandChartCard = Color(0xFFC9BA9B)         // Bezier line chart card
val WarmSandCalloutPill = Color(0xFFDAF5E6)       // Callout pill on the Bezier chart
val SoftOliveChartCard = Color(0xFFCED2BE)        // Capsule bar chart card
val SoftSageListCard = Color(0xFFCBD1C4)          // Summary list container card
val PastelCardInk = Color(0xFF121413)             // Primary dark text & icon color on pastel cards
val PastelCardMutedInk = Color(0xFF56655E)        // Subtitle text on pastel cards ("Total time today")
val PastelCardCircleBorder = Color(0xFF6E7F77)    // Circular ↗ button outline on pastel cards

// Summary List Pastel Pill Rows
val PastelPillLavender = Color(0xFFDCE2F9)
val PastelPillSky = Color(0xFFCBEAF6)
val PastelPillMint = Color(0xFFC6F0DC)
val PastelPillCream = Color(0xFFF7F2CD)
val PastelRowPills = listOf(
    PastelPillLavender,
    PastelPillSky,
    PastelPillMint,
    PastelPillCream
)

// Semantic Badges & Accents
val MintPositiveBadge = Color(0xFF8BE8B9)         // Green comparison pill background on dark bento card
val OnMintPositiveInk = Color(0xFF0D2618)         // Dark green text inside MintPositiveBadge
val CoralHighlightText = Color(0xFFFF6464)        // Coral metric text on right dark bento card
val CapsulePeachBar = Color(0xFFF3B282)           // Peach capsule bar
val CapsuleGoldBar = Color(0xFFF7D486)            // Gold capsule bar

// Curated 8-Color Harmonious Chart Palette
val ChatChartPalette: List<Color> = listOf(
    Color(0xFF63C89A), // 0: Emerald Mint
    Color(0xFFAEF075), // 1: Soft Lime
    Color(0xFFFFB978), // 2: Warm Peach
    Color(0xFFFF8A7A), // 3: Soft Coral
    Color(0xFFA5B4FC), // 4: Periwinkle
    Color(0xFFF7D486), // 5: Butter Gold
    Color(0xFF7DD3FC), // 6: Sky Blue
    Color(0xFFD8B4FE)  // 7: Soft Purple
)

/**
 * Returns a consistent, vibrant color for a chat.
 * If customColorTag is specified, converts it to Color.
 * Otherwise, maps the chatName hash deterministically to ChatChartPalette.
 */
fun getChatColor(chatName: String, customColorTag: Int? = null): Color {
    if (customColorTag != null) {
        return Color(customColorTag)
    }
    val hash = abs(chatName.hashCode())
    return ChatChartPalette[hash % ChatChartPalette.size]
}

// ============================================================================
// BACKWARD-COMPATIBILITY ALIASES & MATERIAL 3 SUPPORT
// ============================================================================

val LivePulseGreen = Color(0xFF22C55E)
val AlertWarningAmber = Color(0xFFF59E0B)

// Emerald / Teal Primary (mapped to butter gold / emerald)
val EmeraldPrimaryLight = Color(0xFF0D6E4C)
val OnEmeraldPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFA6F5D0)
val OnEmeraldPrimaryContainerLight = Color(0xFF002114)

val EmeraldPrimaryDark = ButterGoldAccent
val OnEmeraldPrimaryDark = OnButterGoldInk
val EmeraldPrimaryContainerDark = CharcoalBentoSurface
val OnEmeraldPrimaryContainerDark = ButterGoldAccent

// Indigo Secondary
val IndigoSecondaryLight = Color(0xFF4C58A9)
val OnIndigoSecondaryLight = Color(0xFFFFFFFF)
val IndigoSecondaryContainerLight = Color(0xFFDFE0FF)
val OnIndigoSecondaryContainerLight = Color(0xFF00105C)

val IndigoSecondaryDark = SageMintHeroCard
val OnIndigoSecondaryDark = PastelCardInk
val IndigoSecondaryContainerDark = SageMintHeroCard
val OnIndigoSecondaryContainerDark = PastelCardInk

// Cyan Tertiary
val CyanTertiaryLight = Color(0xFF006877)
val OnCyanTertiaryLight = Color(0xFFFFFFFF)
val CyanTertiaryContainerLight = Color(0xFFA1EFFF)
val OnCyanTertiaryContainerLight = Color(0xFF001F25)

val CyanTertiaryDark = MintPositiveBadge
val OnCyanTertiaryDark = OnMintPositiveInk
val CyanTertiaryContainerDark = ElevatedCharcoalSurface
val OnCyanTertiaryContainerDark = MintPositiveBadge

// Neutral & Surfaces Light
val SurfaceLight = Color(0xFFF7FBF6)
val OnSurfaceLight = Color(0xFF181D1A)
val SurfaceVariantLight = Color(0xFFDCE5DD)
val OnSurfaceVariantLight = Color(0xFF404944)
val BackgroundLight = Color(0xFFF7FBF6)
val OnBackgroundLight = Color(0xFF181D1A)
val OutlineLight = Color(0xFF707973)
val OutlineVariantLight = Color(0xFFC0C9C2)

// Neutral & Surfaces Dark (mapped to Obsidian and Charcoal Bento surfaces)
val SurfaceDark = CharcoalBentoSurface
val OnSurfaceDark = Color(0xFFF5F5F5)
val SurfaceVariantDark = CharcoalBentoSurface
val OnSurfaceVariantDark = MutedChampagneIcon
val BackgroundDark = ObsidianBackground
val OnBackgroundDark = Color(0xFFF5F5F5)
val OutlineDark = ElevatedCharcoalSurface
val OutlineVariantDark = Color(0xFF38383E)

// Surface Container levels for M3 hierarchy
val SurfaceContainerLowestDark = ObsidianBackground
val SurfaceContainerLowDark = ObsidianBackground
val SurfaceContainerDark = CharcoalBentoSurface
val SurfaceContainerHighDark = ElevatedCharcoalSurface
val SurfaceContainerHighestDark = ElevatedCharcoalSurface

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
