package com.example.eyeguard.presentation.theme

import androidx.compose.ui.graphics.Color

// ── Warm Dark Gradients ──────────────────────────
val WarmDarkBgStart = Color(0xFF0F1117)
val WarmDarkBgMid = Color(0xFF141720)
val WarmDarkBgEnd = Color(0xFF191B24)

// Backward compatibility
val GradientDarkStart = WarmDarkBgStart
val GradientDarkMid = WarmDarkBgMid
val GradientDarkEnd = WarmDarkBgEnd

// ── Warm Light Gradients ─────────────────────────
val WarmLightBgStart = Color(0xFFFAF7F2)
val WarmLightBgMid = Color(0xFFF4EFE6)
val WarmLightBgEnd = Color(0xFFEBE4D8)

// ── Primary Accents (Emerald & Amber Glow) ────────
val EmeraldPrimary = Color(0xFF10B981)
val EmeraldGlow = Color(0xFF34D399)
val EmeraldDark = Color(0xFF065F46)
val BluePrimary = Color(0xFF38BDF8)
val BlueOnPrimary = Color(0xFF002A45)
val BluePrimaryContainer = Color(0xFF0C4A6E)
val BlueOnPrimaryContainer = Color(0xFFBAE6FD)

// ── Secondary Accents (Warm Amber & Honey) ─────────
val AmberWarm = Color(0xFFF59E0B)
val AmberGlow = Color(0xFFFCD34D)
val GreenSecondary = EmeraldPrimary
val GreenOnSecondary = Color(0xFF003822)
val GreenSecondaryContainer = Color(0xFF064E3B)
val GreenOnSecondaryContainer = Color(0xFFA7F3D0)

// ── Tertiary (Soft Violet / Lavender) ─────────────
val PurpleTertiary = Color(0xFFA78BFA)
val PurpleOnTertiary = Color(0xFF2E1065)
val PurpleTertiaryContainer = Color(0xFF4C1D95)
val PurpleOnTertiaryContainer = Color(0xFFDDD6FE)

// ── Error & Warning ───────────────────────────────
val ErrorRed = Color(0xFFF87171)
val ErrorOnRed = Color(0xFF450A0A)
val ErrorRedContainer = Color(0xFF7F1D1D)
val ErrorOnRedContainer = Color(0xFFFECACA)

// ── Dark Theme Background & Surface ───────────────
val DarkBg = WarmDarkBgStart
val DarkOnBg = Color(0xFFF1F5F9)
val DarkSurface = Color(0xFF13151D)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkSurfaceVariant = Color(0xFF1E212B)
val DarkOnSurfaceVariant = Color(0xFFCBD5E1)
val DarkOutline = Color(0xFF64748B)
val DarkOutlineVariant = Color(0xFF334155)

// ── Light Theme Background & Surface ──────────────
val LightBg = WarmLightBgStart
val LightOnBg = Color(0xFF1E293B)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1E293B)
val LightSurfaceVariant = Color(0xFFF1EDE4)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFF94A3B8)
val LightOutlineVariant = Color(0xFFE2E8F0)
val LightPrimary = Color(0xFF0D9488)
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFCCFBF1)
val LightOnPrimaryContainer = Color(0xFF115E59)
val LightSecondary = Color(0xFFD97706)
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFFEF3C7)
val LightOnSecondaryContainer = Color(0xFF78350F)

// ── Glassmorphism Specification ───────────────────
val GlassDarkSurface = Color(0x1FFFFFFF)
val GlassDarkBorder = Color(0x33FFFFFF)
val GlassDarkHighlight = Color(0x4DFFFFFF)

val GlassLightSurface = Color(0xE6FFFFFF)
val GlassLightBorder = Color(0x66FFFFFF)
val GlassLightHighlight = Color(0x99FFFFFF)

// Legacy aliases
val GlassSurface = GlassDarkSurface
val GlassSurfaceStrong = Color(0x3D141720)
val GlassBorder = GlassDarkBorder
val GlassHighlight = GlassDarkHighlight

// ── Semantic Status ───────────────────────────────
val StatusActive = EmeraldGlow
val StatusInactive = Color(0xFF94A3B8)
val StatusWarning = AmberWarm

// ── Text Hierarchy ────────────────────────────────
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)
