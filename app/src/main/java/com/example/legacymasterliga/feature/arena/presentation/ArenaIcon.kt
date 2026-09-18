package com.example.legacymasterliga.feature.arena.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ArenaIcon(modifier: Modifier = Modifier.size(24.dp)) {
    val cyan = MaterialTheme.colorScheme.primary
    val gold = Color(0xFFFFD23C)
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Espadas cruzadas atrás (X)
        rotate(45f) {
            // Espada 1 (Dourada)
            drawRect(
                color = gold,
                topLeft = Offset(w * 0.45f, h * 0.1f),
                size = androidx.compose.ui.geometry.Size(w * 0.1f, h * 0.8f)
            )
        }
        rotate(-45f) {
            // Espada 2 (Dourada)
            drawRect(
                color = gold,
                topLeft = Offset(w * 0.45f, h * 0.1f),
                size = androidx.compose.ui.geometry.Size(w * 0.1f, h * 0.8f)
            )
        }

        // Bola de Futebol (Círculo)
        drawCircle(
            color = cyan,
            radius = w * 0.35f,
            style = Stroke(width = w * 0.08f)
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.8f),
            radius = w * 0.32f
        )
        
        // Pentágono Central (Branco)
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.4f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.58f, h * 0.62f)
            lineTo(w * 0.42f, h * 0.62f)
            lineTo(w * 0.38f, h * 0.48f)
            close()
        }
        drawPath(path = path, color = Color.White)
    }
}
