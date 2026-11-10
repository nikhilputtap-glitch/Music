package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Pure AMOLED background
val AmoledBlack = Color(0xFF05020A)
val AmoledDarkSurface = Color(0xFF0C0717)
val AmoledCard = Color(0xFF130E24)
val AmoledCardHover = Color(0xFF1D1636)

// Neon & Accent Colors
val NeonPurple = Color(0xFF9D4EDD)
val NeonPurpleBright = Color(0xFFC77DFF)
val DeepPurple = Color(0xFF5A189A)
val ElectricBlue = Color(0xFF00F0FF)
val ElectricCyan = Color(0xFF00E5FF)
val NeonMagenta = Color(0xFFFF2A85)

// Functional Colors
val NeonGreen = Color(0xFF00F59B)
val NeonAmber = Color(0xFFFFB300)
val NeonRed = Color(0xFFFF3366)

// Glassmorphism & Borders
val GlassBorder = Color(0x3300F0FF)
val GlassBorderPurple = Color(0x339D4EDD)
val GlassSurface = Color(0x66130E24)
val GlassSurfaceHighlight = Color(0x22FFFFFF)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3A8D1)
val TextTertiary = Color(0xFF756A96)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, NeonPurple)
)

val GlowPurpleGradient = Brush.radialGradient(
    colors = listOf(Color(0x669D4EDD), Color(0x0005020A))
)

val WaveformGradient = Brush.verticalGradient(
    colors = listOf(ElectricBlue, NeonPurpleBright, DeepPurple)
)

val CardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF17102D), Color(0xFF0C0717))
)
