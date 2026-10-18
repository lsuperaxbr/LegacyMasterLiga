package com.example.legacymasterliga.core.ui.components.badge

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path

object BadgeShapeDrawer {

    fun createPath(shape: String, width: Float, height: Float): Path = Path().apply {
        val w = width
        val h = height

        when (shape.lowercase().trim()) {
            "circle" -> {
                addOval(Rect(0f, 0f, w, h))
            }
            "diamond" -> {
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.5f)
                lineTo(w * 0.5f, h)
                lineTo(0f, h * 0.5f)
                close()
            }
            "hexagon" -> {
                moveTo(w * 0.5f, 0f)
                lineTo(w * 0.95f, h * 0.25f)
                lineTo(w * 0.95f, h * 0.75f)
                lineTo(w * 0.5f, h)
                lineTo(w * 0.05f, h * 0.75f)
                lineTo(w * 0.05f, h * 0.25f)
                close()
            }
            "crest" -> {
                moveTo(w * 0.05f, 0f)
                lineTo(w * 0.95f, 0f)
                lineTo(w * 0.95f, h * 0.60f)
                cubicTo(w * 0.95f, h * 0.88f, w * 0.65f, h, w * 0.5f, h)
                cubicTo(w * 0.35f, h, w * 0.05f, h * 0.88f, w * 0.05f, h * 0.60f)
                close()
            }
            else -> { // "shield" (padrão)
                moveTo(0f, h * 0.15f)
                lineTo(w * 0.5f, 0f)
                lineTo(w, h * 0.15f)
                lineTo(w, h * 0.55f)
                cubicTo(w, h * 0.85f, w * 0.7f, h, w * 0.5f, h)
                cubicTo(w * 0.3f, h, 0f, h * 0.85f, 0f, h * 0.55f)
                close()
            }
        }
    }
}
