package com.example.legacymasterliga.feature.reports.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legacymasterliga.feature.reports.domain.ReportFormat

@Composable fun ReportsRoute(onBack:()->Unit, vm:ReportsViewModel= hiltViewModel()) { val s by vm.state.collectAsStateWithLifecycle(); ReportsScreen(s,onBack,vm::selectLeague,vm::selectCompetition,vm::selectSeason,vm::generate,vm::copyTo,vm::clearGenerated) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun ReportsScreen(s:ReportsUiState,onBack:()->Unit,onLeague:(Long)->Unit,onCompetition:(Long?)->Unit,onSeason:(Long?)->Unit,onGenerate:(ReportFormat)->Unit,onCopy:(android.net.Uri)->Unit,onClear:()->Unit) {
    val context= LocalContext.current
    val saver= rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(s.generated?.mimeType ?: "application/octet-stream")){ it?.let(onCopy) }
    Scaffold(topBar={ CenterAlignedTopAppBar(title={Text("Relatórios e Exportação")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Outlined.ArrowBack,"Voltar")}}) }){ p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("Filtros",style=MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){ s.leagues.forEach{ FilterChip(selected=s.leagueId==it.id,onClick={onLeague(it.id)},label={Text(it.name)}) } }
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){ FilterChip(selected=s.competitionId==null,onClick={onCompetition(null)},label={Text("Todas competições")}); s.competitions.forEach{FilterChip(selected=s.competitionId==it.id,onClick={onCompetition(it.id)},label={Text(it.name)})} }
            if(s.competitionId!=null) FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){ FilterChip(selected=s.seasonId==null,onClick={onSeason(null)},label={Text("Todas temporadas")}); s.seasons.forEach{FilterChip(selected=s.seasonId==it.id,onClick={onSeason(it.id)},label={Text(it.name)})} }
            Text("O relatório inclui classificação, financeiro em CR, mercado, resultados, histórico e estatísticas.",style=MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){ Button(onClick={onGenerate(ReportFormat.PDF)},enabled=!s.isLoading){Text("Gerar PDF")}; OutlinedButton(onClick={onGenerate(ReportFormat.CSV)},enabled=!s.isLoading){Text("Gerar CSV")} }
            if(s.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            s.generated?.let { r ->
                Card(Modifier.fillMaxWidth()){ Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){ Text(r.fileName,style=MaterialTheme.typography.titleMedium); Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){ Button(onClick={ saver.launch(r.fileName) }){Text("Salvar")}; OutlinedButton(onClick={ val intent=Intent(Intent.ACTION_SEND).apply{type=r.mimeType;putExtra(Intent.EXTRA_STREAM,r.uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)};context.startActivity(Intent.createChooser(intent,"Compartilhar relatório")) }){Text("Compartilhar")}; TextButton(onClick=onClear){Text("Fechar")} } } }
            }
            s.message?.let { Text(it,color=MaterialTheme.colorScheme.primary) }
        }
    }
}
