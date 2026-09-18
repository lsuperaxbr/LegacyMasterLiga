package com.example.legacymasterliga.feature.betaguide.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class BetaTestStep(
    val title: String,
    val instructions: String,
    val expected: String,
)

private val betaTestSteps = listOf(
    BetaTestStep("1. Entrar como Administrador", "Use admin / admin123.", "O Dashboard abre com acesso total."),
    BetaTestStep("2. Conferir dados da Liga", "Abra Clubes e Competições.", "Torino, Fluminense, Bournemouth e Girona aparecem na Liga M L Amigos; cada clube inicia com 500 CR."),
    BetaTestStep("3. Conferir participantes", "Abra Inscrições e selecione Liga Principal / Temporada 1.", "Os quatro clubes aparecem inscritos sem duplicidade."),
    BetaTestStep("4. Gerar calendário", "Abra Rodadas e gere as partidas.", "O formato ida e volta cria confrontos com mandos invertidos."),
    BetaTestStep("5. Lançar resultados", "Cadastre placares em algumas partidas.", "Classificação, estatísticas, Dashboard, notícias e notificações são atualizados."),
    BetaTestStep("6. Testar a Classificação", "Abra Classificação.", "GP, GC, SG, pontos e aproveitamento estão corretos; o líder fica amarelo."),
    BetaTestStep("7. Testar Mercado e Financeiro", "Registre uma transferência e consulte o extrato.", "O saldo em CR é validado, a movimentação aparece no extrato e uma notícia é criada."),
    BetaTestStep("8. Testar usuários", "Entre com tomascote, richemont, pipocacr7 ou lsuperax; senha demo123.", "O Presidente vê somente as funções permitidas e seu clube associado."),
    BetaTestStep("9. Testar Backup", "Crie um backup manual e valide o arquivo.", "O pacote .lmlbackup é criado e listado como válido."),
    BetaTestStep("10. Registrar problemas", "Anote a tela, ação e mensagem exibida.", "O erro pode ser reproduzido e corrigido na próxima revisão."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaTestGuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Roteiro de teste Beta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Liga M L Amigos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Dados de demonstração são criados somente nas variantes Debug e Beta. A versão Release não recebe dados fictícios.")
                    Text("Presidentes: senha demo123", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            items(betaTestSteps, key = { it.title }) { step ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(step.instructions)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Resultado esperado: ${step.expected}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
