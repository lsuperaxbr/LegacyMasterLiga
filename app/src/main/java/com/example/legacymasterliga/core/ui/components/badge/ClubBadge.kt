package com.example.legacymasterliga.core.ui.components.badge

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.legacymasterliga.domain.model.ClubBadgeConfig

@Composable
fun ClubBadge(
    config: ClubBadgeConfig,
    clubName: String,
    modifier: Modifier = Modifier,
    borderWidthDp: Dp = 1.5.dp
) {
    val primaryColor = parseColorSafely(config.primaryColor, Color(0xFF808080))
    val secondaryColor = parseColorSafely(config.secondaryColor, Color.White)
    val accentColor = parseColorSafely(config.accentColor, Color.Black)
    val initialText = ClubBadgeConfig.extractInitial(clubName)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            val shapePath = BadgeShapeDrawer.createPath(config.shape, w, h)

            // 1. Preenchimento base (primaryColor)
            drawPath(shapePath, color = primaryColor)

            // 2. Padrão recortado dentro da forma (secondaryColor)
            BadgePatternDrawer.drawPattern(
                drawScope = this,
                pattern = config.pattern,
                shapePath = shapePath,
                color = secondaryColor,
                w = w,
                h = h
            )

            // 3. Símbolo central vetorial (accentColor)
            if (config.symbol.lowercase().trim() != "initial") {
                BadgeSymbolDrawer.drawSymbol(
                    drawScope = this,
                    symbol = config.symbol,
                    color = accentColor,
                    w = w,
                    h = h
                )
            }

            // 4. Borda externa
            val strokeWidth = borderWidthDp.toPx().coerceAtLeast(1.5f)
            drawPath(shapePath, color = secondaryColor, style = Stroke(width = strokeWidth))
        }

        // Se o símbolo for "initial", exibe a primeira letra do nome do clube em maiúscula
        if (config.symbol.lowercase().trim() == "initial") {
            Text(
                text = initialText,
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                style = TextStyle(
                    shadow = Shadow(Color.Black.copy(alpha = 0.3f), blurRadius = 3f)
                )
            )
        }
    }
}

private fun parseColorSafely(hex: String, fallback: Color): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}
