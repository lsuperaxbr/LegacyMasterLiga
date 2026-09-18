package com.example.legacymasterliga.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

private val LegacyFont = FontFamily.Default
private val RetroGlow = Shadow(
    color = Color(0xFF00A2FF).copy(alpha = 0.5f),
    offset = Offset(0f, 0f),
    blurRadius = 12f
)

val Typography = Typography(
    displaySmall = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Black, fontSize = 26.sp, lineHeight = 34.sp, shadow = RetroGlow, letterSpacing = 1.2.sp),
    headlineLarge = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp, shadow = RetroGlow, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp, shadow = RetroGlow),
    headlineSmall = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    titleLarge = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF00D4FF), letterSpacing = 0.8.sp),
    titleMedium = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = LegacyFont, fontWeight = FontWeight.Black, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 1.5.sp),
)
