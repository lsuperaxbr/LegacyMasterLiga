package com.example.legacymasterliga.feature.crashreport.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrashReportScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("crash_reports", Context.MODE_PRIVATE) }
    val crashText = remember { prefs.getString("last_crash", "Nenhum erro registrado.") ?: "" }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1428))
            .padding(16.dp)
    ) {
        Text("⚠️ O app encontrou um erro", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Copie o texto abaixo e envie para o suporte:", color = Color.White.copy(alpha = 0.7f))
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0D1F3C), RoundedCornerShape(8.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionContainer {
                Text(crashText, color = Color(0xFF00E6C8), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(crashText))
            }) { Text("Copiar Erro") }
            OutlinedButton(onClick = {
                prefs.edit().remove("last_crash").apply()
                onDismiss()
            }) { Text("Continuar") }
        }
    }
}
