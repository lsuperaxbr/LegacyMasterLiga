package com.example.legacymasterliga.core.ui.components.badge

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

object BadgeSymbolDrawer {

    fun drawSymbol(
        drawScope: DrawScope,
        symbol: String,
        color: Color,
        w: Float,
        h: Float
    ) {
        val normSymbol = symbol.lowercase().trim()
        if (normSymbol == "none" || normSymbol.isBlank()) return

        when (normSymbol) {
            "star" -> {
                val path = createStarPath(w * 0.5f, h * 0.5f, w * 0.22f, w * 0.09f)
                drawScope.drawPath(path, color = color)
            }
            "crown" -> {
                val path = Path().apply {
                    val cx = w * 0.5f
                    val cy = h * 0.52f
                    val cw = w * 0.38f
                    val ch = h * 0.28f

                    moveTo(cx - cw * 0.5f, cy + ch * 0.5f)
                    lineTo(cx - cw * 0.5f, cy - ch * 0.3f)
                    lineTo(cx - cw * 0.2f, cy + ch * 0.1f)
                    lineTo(cx, cy - ch * 0.5f)
                    lineTo(cx + cw * 0.2f, cy + ch * 0.1f)
                    lineTo(cx + cw * 0.5f, cy - ch * 0.3f)
                    lineTo(cx + cw * 0.5f, cy + ch * 0.5f)
                    close()
                }
                drawScope.drawPath(path, color = color)
            }
            "lightning" -> {
                val path = Path().apply {
                    val cx = w * 0.5f
                    val cy = h * 0.5f
                    moveTo(cx + w * 0.08f, cy - h * 0.30f)
                    lineTo(cx - w * 0.18f, cy + h * 0.02f)
                    lineTo(cx + w * 0.02f, cy + h * 0.02f)
                    lineTo(cx - w * 0.08f, cy + h * 0.30f)
                    lineTo(cx + w * 0.18f, cy - h * 0.02f)
                    lineTo(cx - w * 0.02f, cy - h * 0.02f)
                    close()
                }
                drawScope.drawPath(path, color = color)
            }
            "ball" -> {
                val radius = w * 0.20f
                val center = Offset(w * 0.5f, h * 0.5f)
                drawScope.drawCircle(color = color, radius = radius, center = center)
                drawScope.drawCircle(color = Color.Black, radius = radius, center = center, style = Stroke(width = w * 0.04f))
            }
            "tree" -> {
                val path = Path().apply {
                    val cx = w * 0.5f
                    val cy = h * 0.5f
                    // Trunk
                    addRect(androidx.compose.ui.geometry.Rect(cx - w * 0.04f, cy + h * 0.10f, cx + w * 0.04f, cy + h * 0.25f))
                    // Layers
                    moveTo(cx, cy - h * 0.28f)
                    lineTo(cx + w * 0.18f, cy - h * 0.05f)
                    lineTo(cx + w * 0.10f, cy - h * 0.05f)
                    lineTo(cx + w * 0.24f, cy + h * 0.12f)
                    lineTo(cx - w * 0.24f, cy + h * 0.12f)
                    lineTo(cx - w * 0.10f, cy - h * 0.05f)
                    lineTo(cx - w * 0.18f, cy - h * 0.05f)
                    close()
                }
                drawScope.drawPath(path, color = color)
            }
        }
    }

    private fun createStarPath(cx: Float, cy: Float, outerR: Float, innerR: Float): Path = Path().apply {
        val points = 5
        val angle = Math.PI / points
        for (i in 0 until 2 * points) {
            val r = if (i % 2 == 0) outerR else innerR
            val currAngle = i * angle - Math.PI / 2
            val x = (cx + r * cos(currAngle)).toFloat()
            val y = (cy + r * sin(currAngle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
