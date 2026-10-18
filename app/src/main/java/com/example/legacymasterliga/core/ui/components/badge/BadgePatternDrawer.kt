package com.example.legacymasterliga.core.ui.components.badge

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath

object BadgePatternDrawer {

    fun drawPattern(
        drawScope: DrawScope,
        pattern: String,
        shapePath: Path,
        color: Color,
        w: Float,
        h: Float
    ) {
        val normPattern = pattern.lowercase().trim()
        if (normPattern == "solid" || normPattern.isBlank()) return

        drawScope.clipPath(shapePath) {
            when (normPattern) {
                "vertical_stripes" -> {
                    val stripeWidth = w / 5f
                    for (i in 0 until 5 step 2) {
                        drawRect(
                            color = color,
                            topLeft = Offset(i * stripeWidth, 0f),
                            size = Size(stripeWidth, h)
                        )
                    }
                }
                "horizontal_stripes" -> {
                    val stripeHeight = h / 5f
                    for (i in 0 until 5 step 2) {
                        drawRect(
                            color = color,
                            topLeft = Offset(0f, i * stripeHeight),
                            size = Size(w, stripeHeight)
                        )
                    }
                }
                "diagonal_stripes" -> {
                    val diagPath = Path().apply {
                        moveTo(0f, h * 0.65f)
                        lineTo(w * 0.4f, 0f)
                        lineTo(w * 0.7f, 0f)
                        lineTo(w * 0.3f, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(diagPath, color = color)
                }
                "half" -> {
                    drawRect(
                        color = color,
                        topLeft = Offset(w * 0.5f, 0f),
                        size = Size(w * 0.5f, h)
                    )
                }
                "cross" -> {
                    val barThickness = w * 0.22f
                    // Vertical bar
                    drawRect(
                        color = color,
                        topLeft = Offset(w * 0.5f - barThickness / 2, 0f),
                        size = Size(barThickness, h)
                    )
                    // Horizontal bar
                    drawRect(
                        color = color,
                        topLeft = Offset(0f, h * 0.5f - barThickness / 2),
                        size = Size(w, barThickness)
                    )
                }
                "central_circle" -> {
                    drawCircle(
                        color = color,
                        radius = w * 0.28f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                }
            }
        }
    }
}
