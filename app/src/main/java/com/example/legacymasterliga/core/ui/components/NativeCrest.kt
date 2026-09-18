package com.example.legacymasterliga.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.legacymasterliga.domain.ClubCrestParser

@Composable
fun NativeCrest(clubName: String, crestRaw: String?, modifier: Modifier = Modifier) {
    val data = ClubCrestParser.decode(crestRaw, clubName)
    val fillColor = runCatching { Color(android.graphics.Color.parseColor(data.colorFill)) }
        .getOrDefault(Color(0xFF00E6C8))
    val borderColor = runCatching { Color(android.graphics.Color.parseColor(data.colorBorder)) }
        .getOrDefault(Color.White)
    val patternColor = runCatching { Color(android.graphics.Color.parseColor(data.colorPattern)) }
        .getOrDefault(Color.White)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val borderWidth = w * 0.06f
            
            val path = shapePath(data.shape, w, h)

            // 1. Preenchimento base
            drawPath(path, color = fillColor)

            // 2. Padrão decorativo, recortado dentro da forma
            if (data.pattern != ClubCrestParser.CrestPattern.NONE) {
                clipPath(path) {
                    when (data.pattern) {
                        ClubCrestParser.CrestPattern.STRIPES_VERTICAL -> {
                            val stripeWidth = w / 5f
                            for (i in 0 until 5 step 2) {
                                drawRect(
                                    color = patternColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(i * stripeWidth, 0f),
                                    size = androidx.compose.ui.geometry.Size(stripeWidth, h),
                                )
                            }
                        }
                        ClubCrestParser.CrestPattern.STRIPES_HORIZONTAL -> {
                            val stripeHeight = h / 5f
                            for (i in 0 until 5 step 2) {
                                drawRect(
                                    color = patternColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(0f, i * stripeHeight),
                                    size = androidx.compose.ui.geometry.Size(w, stripeHeight),
                                )
                            }
                        }
                        ClubCrestParser.CrestPattern.DIAGONAL -> {
                            val diagPath = Path().apply {
                                moveTo(0f, h * 0.65f)
                                lineTo(w * 0.4f, 0f)
                                lineTo(w * 0.7f, 0f)
                                lineTo(w * 0.3f, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(diagPath, color = patternColor)
                        }
                        else -> {}
                    }
                }
            }

            // 3. Borda por cima de tudo
            drawPath(path, color = borderColor, style = Stroke(width = borderWidth))
        }
        
        if (data.showInitials) {
            Text(
                text = data.initials,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = if (data.initials.length >= 3) 11.sp else 13.sp,
                style = TextStyle(
                    shadow = Shadow(Color.Black, blurRadius = 4f)
                ),
            )
        }
    }
}

private fun shapePath(shape: ClubCrestParser.CrestShape, w: Float, h: Float): Path = Path().apply {
    when (shape) {
        ClubCrestParser.CrestShape.CIRCLE -> addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
        ClubCrestParser.CrestShape.OVAL -> {
            // Oval genuinamente alongado: mais estreito na largura, cheio na altura
            val insetX = w * 0.15f
            addOval(androidx.compose.ui.geometry.Rect(insetX, 0f, w - insetX, h))
        }
        ClubCrestParser.CrestShape.DIAMOND -> {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.5f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.5f)
            close()
        }
        ClubCrestParser.CrestShape.SHIELD -> {
            moveTo(0f, h * 0.15f)
            lineTo(w * 0.5f, 0f)
            lineTo(w, h * 0.15f)
            lineTo(w, h * 0.55f)
            cubicTo(w, h * 0.85f, w * 0.7f, h, w * 0.5f, h)
            cubicTo(w * 0.3f, h, 0f, h * 0.85f, 0f, h * 0.55f)
            close()
        }
        ClubCrestParser.CrestShape.SHIELD_THIN -> {
            // Versão mais estreita e alongada do escudo — inset lateral maior
            val insetX = w * 0.18f
            val left = insetX
            val right = w - insetX
            moveTo(left, h * 0.12f)
            lineTo(w * 0.5f, 0f)
            lineTo(right, h * 0.12f)
            lineTo(right, h * 0.6f)
            cubicTo(right, h * 0.9f, w * 0.5f + (right - left) * 0.1f, h, w * 0.5f, h)
            cubicTo(w * 0.5f - (right - left) * 0.1f, h, left, h * 0.9f, left, h * 0.6f)
            close()
        }
    }
}
