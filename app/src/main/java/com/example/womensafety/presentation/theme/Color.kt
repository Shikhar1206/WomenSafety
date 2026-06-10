package com.example.womensafety.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary & Accent Colors
val SafetyRed = Color(0xFFFF3366)      // Vibrant Neon Coral
val SafetyRedDark = Color(0xFFC62828)
val SafetyRedLight = Color(0xFFFF6B8B)

val TrustPurple = Color(0xFF8E24AA)
val TrustPurpleDark = Color(0xFF4A0072)
val TrustPurpleLight = Color(0xFFD05CE3)

val SafeGreen = Color(0xFF00E676)       // Vibrant Neon Green
val SafeGreenLight = Color(0xFF66FFA6)

val WarmPink = Color(0xFFD81B60)
val WarmPinkLight = Color(0xFFFF6090)

val ElectricBlue = Color(0xFF00E5FF)
val ElectricBlueLight = Color(0xFF80F7FF)

// Neutrals for Obsidian & Glassmorphism
val Background = Color(0xFF07070C)      // Ultra-dark Space Black
val Surface = Color(0xFF111122)         // Translucent Nebula Dark
val SurfaceVariant = Color(0xFF1A1A35)
val CardBackground = Color(0x751E1E38)  // Semi-transparent for Glass effect
val Outline = Color(0x357F52FF)         // Translucent purple outline for borders
val GlassBorder = Color(0x1AFFFFFF)

// Light theme (fallback)
val BackgroundLight = Color(0xFFFAF9FF)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1EEFF)

// Text Colors
val OnDark = Color(0xFFF3F3FA)
val OnDarkSecondary = Color(0xFF9090B0)
val OnLight = Color(0xFF111122)
val OnLightSecondary = Color(0xFF6B6B8A)

// Status Indicators
val StatusActive = Color(0xFF00E676)
val StatusCancelled = Color(0xFFFFB300)
val StatusDanger = Color(0xFFFF3366)

// Gradient Palettes
val PrimaryGradient = listOf(TrustPurpleLight, WarmPinkLight)
val DangerGradient = listOf(SafetyRed, WarmPink)
val GlassGradient = listOf(Color(0x0AFFFFFF), Color(0x02FFFFFF))
val ActiveGradient = listOf(SafeGreen, ElectricBlue)
