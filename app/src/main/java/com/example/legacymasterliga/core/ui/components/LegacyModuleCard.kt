package com.example.legacymasterliga.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.legacymasterliga.ui.theme.LegacyBlue
import com.example.legacymasterliga.ui.theme.LegacyBevelLight
import com.example.legacymasterliga.ui.theme.LegacyBevelDark

@Composable
fun LegacyModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    
    // Animação de pulsação suave para a borda quando em foco ou pressionado
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "moduleScale")
    val borderIntensity = if (pressed) 1f else pulseAlpha
    val borderThickness = if (pressed) 2.dp else 1.dp
    
    val bevelGradient = Brush.verticalGradient(
        colors = listOf(LegacyBevelLight, Color.Transparent, LegacyBevelDark),
        startY = 0f,
        endY = 500f
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                BorderStroke(
                    width = borderThickness,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LegacyBlue.copy(alpha = borderIntensity),
                            LegacyBlue.copy(alpha = borderIntensity * 0.3f)
                        )
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .semantics {
                role = Role.Button
                contentDescription = "$title. $subtitle"
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF001C3D)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
    ) {
        Box(modifier = Modifier.background(bevelGradient)) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Ícone com fundo Arcade
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = LegacyBlue,
                        modifier = Modifier.size(22.dp).scale(if (pressed) 1.15f else 1f)
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title.uppercase(), 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = subtitle, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Brilho interno se estiver pressionado
            if (pressed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(LegacyBlue.copy(alpha = 0.05f))
                )
            }
        }
    }
}
