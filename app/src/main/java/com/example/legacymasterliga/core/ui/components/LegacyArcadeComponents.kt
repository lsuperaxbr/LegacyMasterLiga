package com.example.legacymasterliga.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.legacymasterliga.ui.theme.*

/**
 * Placa de Menu Estilo Arcade com bordas chanfradas e iluminação dinâmica.
 */
@Composable
fun LegacyArcadePlate(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlighted: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animação de escala ao tocar
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "plateScale")
    
    // Animação de "Sweep" (brilho rápido) ao tocar
    val sweepTransition = rememberInfiniteTransition(label = "sweep")
    val sweepOffset by sweepTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepOffset"
    )

    val neonCyan = if (highlighted) Color(0xFF00FF99) else Color(0xFF00D4FF)
    val neonHighlight = if (highlighted) Color(0xFF00FF99) else neonCyan
    val petroleumBlue = Color(0xFF001C3D)
    
    // Efeito Bevel (Relevo)
    val bevelGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.12f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.2f)
        )
    )

    Surface(
        modifier = modifier
            .height(90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .border(
                BorderStroke(
                    width = if (isPressed || highlighted) 2.dp else 1.dp,
                    brush = if (isPressed) {
                        Brush.linearGradient(listOf(neonHighlight, Color.White, neonHighlight))
                    } else if (highlighted) {
                        Brush.linearGradient(listOf(neonHighlight, neonHighlight.copy(alpha = 0.4f)))
                    } else {
                        Brush.verticalGradient(listOf(neonCyan.copy(alpha = 0.6f), neonCyan.copy(alpha = 0.2f)))
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .drawWithContent {
                drawContent()
                if (isPressed) {
                    // Simulação de reflexo atravessando a placa
                    drawRect(
                        brush = Brush.linearGradient(
                            0f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = 0.1f),
                            1f to Color.Transparent,
                            start = Offset(sweepOffset, 0f),
                            end = Offset(sweepOffset + 200f, 400f)
                        )
                    )
                }
            },
        color = petroleumBlue,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bevelGradient)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPressed) Color.White else neonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(6.dp))
                LegacyGlowText(
                    text = title.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LegacyArcadePlate(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlighted: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "plateScale")
    
    val sweepTransition = rememberInfiniteTransition(label = "sweep")
    val sweepOffset by sweepTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepOffset"
    )

    val neonCyan = if (highlighted) Color(0xFF00FF99) else Color(0xFF00D4FF)
    val petroleumBlue = Color(0xFF001C3D)
    
    val bevelGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.12f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.2f)
        )
    )

    Surface(
        modifier = modifier
            .height(90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .border(
                BorderStroke(
                    width = if (isPressed || highlighted) 2.dp else 1.dp,
                    brush = if (isPressed) {
                        Brush.linearGradient(listOf(neonCyan, Color.White, neonCyan))
                    } else if (highlighted) {
                        Brush.linearGradient(listOf(neonCyan, neonCyan.copy(alpha = 0.4f)))
                    } else {
                        Brush.verticalGradient(listOf(neonCyan.copy(alpha = 0.6f), neonCyan.copy(alpha = 0.2f)))
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .drawWithContent {
                drawContent()
                if (isPressed) {
                    drawRect(
                        brush = Brush.linearGradient(
                            0f to Color.Transparent,
                            0.5f to Color.White.copy(alpha = 0.1f),
                            1f to Color.Transparent,
                            start = Offset(sweepOffset, 0f),
                            end = Offset(sweepOffset + 200f, 400f)
                        )
                    )
                }
            },
        color = petroleumBlue,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bevelGradient)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    icon()
                }
                Spacer(Modifier.height(6.dp))
                LegacyGlowText(
                    text = title.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    textAlign = TextAlign.Center
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Painel HUD Superior compacto para informações de status.
 */
@Composable
fun LegacyHudPanel(
    leagueName: String,
    userName: String,
    roleName: String,
    isOnline: Boolean
) {
    val neonCyan = Color(0xFF00D4FF)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(4.dp)),
        color = Color(0xFF000C1A),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIGA",
                    style = MaterialTheme.typography.labelSmall,
                    color = neonCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = leagueName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            VerticalDivider(
                modifier = Modifier.height(30.dp).padding(horizontal = 12.dp),
                color = Color.White.copy(alpha = 0.1f)
            )

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOnline) {
                        Surface(
                            color = neonCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(2.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                "ONLINE", 
                                style = MaterialTheme.typography.labelSmall, 
                                modifier = Modifier.padding(horizontal = 4.dp),
                                color = neonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = userName.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = roleName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Texto com efeito de brilho suave (Glow).
 */
@Composable
fun LegacyGlowText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        style = style.copy(
            shadow = Shadow(
                color = Color(0xFF00A2FF).copy(alpha = 0.5f),
                offset = Offset(0f, 0f),
                blurRadius = 8f
            )
        )
    )
}
