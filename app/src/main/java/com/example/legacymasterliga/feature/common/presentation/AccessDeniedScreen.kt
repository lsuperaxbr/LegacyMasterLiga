package com.example.legacymasterliga.feature.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.legacymasterliga.core.ui.components.LegacyEmptyState
import com.example.legacymasterliga.ui.theme.LegacySpacing

@Composable
fun AccessDeniedScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(LegacySpacing.lg).background(Color(0xFF08152B)), contentAlignment = Alignment.Center) {
        LegacyEmptyState(
            title = "Acesso restrito",
            message = "Seu perfil não possui permissão para esta função ou a liga ainda não foi carregada da nuvem.",
            icon = Icons.Outlined.Lock,
            actionLabel = "Voltar para o Início",
            onAction = onBack,
        )
    }
}
