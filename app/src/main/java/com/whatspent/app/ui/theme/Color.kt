package com.whatspent.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// ============================================================================
// WHATSPENT DESIGN SYSTEM TOKENS
// Obsidian + Acid-Lime + Periwinkle Violet Theme (Dark Mode First-Class)
// ============================================================================

// Primary Canvas & Dark Bento Surfaces
val ObsidianBackground = Color(0xFF0B0B0D)        // True pitch-black app canvas
val CharcoalBentoSurface = Color(0xFF17171A)      // Deep charcoal elevated cards & bento containers
val ElevatedCharcoalSurface = Color(0xFF1E1E22)   // Secondary pill tracks, controls & elevated container
val DarkCircleButtonSurface = Color(0xFF1C1C20)   // Circular dark icon button background
val SubtleDarkBorder = Color(0xFF28282D)          // Subtle hairline border & dashed gridlines

// Primary Neon Acid-Lime Accent & Inks
val AcidLimeAccent = Color(0xFFE5FA6B)            // Vibrant neon acid-lime primary accent
val OnAcidLimeInk = Color(0xFF0E0F0A)             // Deep dark ink for high-contrast on acid-lime
val ButterGoldAccent = AcidLimeAccent             // Backward-compatible alias for existing references
val OnButterGoldInk = OnAcidLimeInk               // Backward-compatible alias for existing references

// Primary Chart Highlight & Inactive Surfaces
val PeriwinkleBarHighlight = Color(0xFF9F8FEF)    // Signature Periwinkle Violet chart highlight
val InactiveBarCharcoal = Color(0xFF222227)       // Deep matte charcoal for unselected/inactive chart bars
val FloatingTooltipSurface = Color(0xFF232329)    // Floating dark glass pill tooltip background
val FloatingTooltipBorder = Color(0xFF34343C)     // Floating dark glass pill border

// Typography Tokens
val PrimaryWhiteText = Color(0xFFF6F6F9)          // Crisp white primary text
val MutedFractionText = Color(0xFF6E6E77)         // Two-tone secondary time units (e.g. 14m, 32s)
val SubtleAxisLabelText = Color(0xFF8A8A93)       // Clean muted labels for axes, subtitles & hints

// Curated 8-Color Harmonious Dark-Mode Chart Palette
val ChatChartPalette: List<Color> = listOf(
    Color(0xFF9F8FEF), // 0: Periwinkle Violet - Primary chart highlight
    Color(0xFFE5FA6B), // 1: Neon Acid-Lime - Primary active accent
    Color(0xFF63E6BE), // 2: Cyber Mint
    Color(0xFF74C0FC), // 3: Electric Sky
    Color(0xFFFF8787), // 4: Soft Coral
    Color(0xFFFCC419), // 5: Warm Amber
    Color(0xFFE599F7), // 6: Orchid
    Color(0xFF96F2D7)  // 7: Seafoam
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

val DarkCircleButtonIcon = SubtleAxisLabelText
val MutedChampagneIcon = SubtleAxisLabelText

// Bento Card Backward-Compatibility Mappings (Replaced pastels with sleek dark surfaces)
val SageMintHeroCard = CharcoalBentoSurface
val SageMintInnerCallout = FloatingTooltipSurface
val WarmSandChartCard = CharcoalBentoSurface
val WarmSandCalloutPill = FloatingTooltipSurface
val SoftOliveChartCard = CharcoalBentoSurface
val SoftSageListCard = CharcoalBentoSurface
val PastelCardInk = PrimaryWhiteText
val PastelCardMutedInk = SubtleAxisLabelText
val PastelCardCircleBorder = SubtleDarkBorder

// Summary List Pill Rows
val PastelPillLavender = ElevatedCharcoalSurface
val PastelPillSky = CharcoalBentoSurface
val PastelPillMint = ElevatedCharcoalSurface
val PastelPillCream = CharcoalBentoSurface
val PastelRowPills = listOf(
    PastelPillLavender,
    PastelPillSky,
    PastelPillMint,
    PastelPillCream
)

// Semantic Badges & Accents
val MintPositiveBadge = AcidLimeAccent
val OnMintPositiveInk = OnAcidLimeInk
val CoralHighlightText = Color(0xFFFF8787)
val CapsulePeachBar = PeriwinkleBarHighlight
val CapsuleGoldBar = AcidLimeAccent

val LivePulseGreen = AcidLimeAccent
val AlertWarningAmber = Color(0xFFFCC419)

// Emerald / Teal Primary (mapped to acid-lime / obsidian)
val EmeraldPrimaryLight = Color(0xFF0D6E4C)
val OnEmeraldPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFA6F5D0)
val OnEmeraldPrimaryContainerLight = Color(0xFF002114)

val EmeraldPrimaryDark = AcidLimeAccent
val OnEmeraldPrimaryDark = OnAcidLimeInk
val EmeraldPrimaryContainerDark = CharcoalBentoSurface
val OnEmeraldPrimaryContainerDark = AcidLimeAccent

// Indigo Secondary (mapped to Periwinkle)
val IndigoSecondaryLight = Color(0xFF4C58A9)
val OnIndigoSecondaryLight = Color(0xFFFFFFFF)
val IndigoSecondaryContainerLight = Color(0xFFDFE0FF)
val OnIndigoSecondaryContainerLight = Color(0xFF00105C)

val IndigoSecondaryDark = PeriwinkleBarHighlight
val OnIndigoSecondaryDark = OnAcidLimeInk
val IndigoSecondaryContainerDark = ElevatedCharcoalSurface
val OnIndigoSecondaryContainerDark = PeriwinkleBarHighlight

// Cyan Tertiary
val CyanTertiaryLight = Color(0xFF006877)
val OnCyanTertiaryLight = Color(0xFFFFFFFF)
val CyanTertiaryContainerLight = Color(0xFFA1EFFF)
val OnCyanTertiaryContainerLight = Color(0xFF001F25)

val CyanTertiaryDark = PeriwinkleBarHighlight
val OnCyanTertiaryDark = OnAcidLimeInk
val CyanTertiaryContainerDark = ElevatedCharcoalSurface
val OnCyanTertiaryContainerDark = PeriwinkleBarHighlight

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
val OnSurfaceDark = PrimaryWhiteText
val SurfaceVariantDark = CharcoalBentoSurface
val OnSurfaceVariantDark = SubtleAxisLabelText
val BackgroundDark = ObsidianBackground
val OnBackgroundDark = PrimaryWhiteText
val OutlineDark = SubtleDarkBorder
val OutlineVariantDark = SubtleDarkBorder

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

val ErrorDark = Color(0xFFFF8787)
val OnErrorDark = Color(0xFF380004)
val ErrorContainerDark = Color(0xFF5A000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)
