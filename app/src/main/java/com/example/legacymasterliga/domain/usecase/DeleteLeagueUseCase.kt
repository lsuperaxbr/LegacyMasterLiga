package com.example.legacymasterliga.domain.usecase

import android.util.Log
import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.OnlineSyncDao
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface DeleteLeagueResult {
    data object Success : DeleteLeagueResult
    data class Error(val message: String) : DeleteLeagueResult
}

@Singleton
class DeleteLeagueUseCase @Inject constructor(
    private val database: AppDatabase,
    private val leagueDao: LeagueDao,
    private val syncDao: OnlineSyncDao,
    private val backupRepository: BackupRepository,
    private val firestore: FirebaseFirestore,
    private val auditLogger: AuditLogger,
) {
    suspend operator fun invoke(targetLeagueId: Long, activeLeagueId: Long?): DeleteLeagueResult = withContext(Dispatchers.IO) {
        try {
            // 1. GUARDA DE FILA: Se houver operações de sync pendentes, aborta.
            val pendingCount = syncDao.observePendingCount().first()
            if (pendingCount > 0) {
                return@withContext DeleteLeagueResult.Error(
                    "Existem $pendingCount operações de sincronização pendentes. Sincronize antes de excluir a liga."
                )
            }

            // 2. GUARDA DE LIGA ATIVA: Não permite excluir a liga atualmente selecionada/ativa.
            if (activeLeagueId != null && targetLeagueId == activeLeagueId) {
                return@withContext DeleteLeagueResult.Error(
                    "Não é possível excluir a liga atualmente ativa. Selecione outra liga no topo da tela primeiro."
                )
            }

            val targetLeague = leagueDao.findById(targetLeagueId)
                ?: return@withContext DeleteLeagueResult.Error("Liga não encontrada.")

            // 3. BACKUP AUTOMÁTICO: Gera backup de segurança do estado atual do app.
            backupRepository.createManualBackup().onFailure { error ->
                Log.w(TAG, "Falha ao criar backup automático de pré-exclusão", error)
            }

            val cloudId = targetLeague.cloudLeagueId

            // 4. EXECUÇÃO ATÔMICA NO BANCO LOCAL (Room)
            database.withTransaction {
                val db = database.openHelper.writableDatabase

                // A. Apagar dados dependentes associados aos clubes/competições da liga
                db.execSQL("DELETE FROM players WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM financial_transactions WHERE clubId IN (SELECT id FROM clubs WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM transfers WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM goal_events WHERE matchId IN (SELECT id FROM matches WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId)))")
                db.execSQL("DELETE FROM competition_participants WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM standings WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM matches WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM rounds WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM season_closures WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM final_standings WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM prize_history WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM competition_prizes WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM competition_settings WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM competitions WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM arena_duels WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM auction_bids WHERE itemId IN (SELECT id FROM auction_items WHERE lotId IN (SELECT id FROM auction_lots WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM auction_items WHERE lotId IN (SELECT id FROM auction_lots WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM auction_lots WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM news WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM notification_reads WHERE notificationId IN (SELECT id FROM notifications WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM notifications WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM club_presidencies WHERE clubId IN (SELECT id FROM clubs WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM audit_logs WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM clubs WHERE leagueId = $targetLeagueId")

                // B. Apagar o registro da própria liga
                db.execSQL("DELETE FROM leagues WHERE id = $targetLeagueId")

                // C. Apagar pendências e registros de sync locais associados ao cloudId
                if (!cloudId.isNullOrBlank()) {
                    db.execSQL("DELETE FROM online_sync_queue WHERE cloudLeagueId = '$cloudId'")
                    db.execSQL("DELETE FROM online_sync_records WHERE cloudLeagueId = '$cloudId'")
                }

                auditLogger.log(
                    category = "LEAGUE",
                    action = "LEAGUE_DELETED",
                    entityType = "LEAGUE",
                    entityId = targetLeagueId,
                    leagueId = activeLeagueId,
                    summary = "Liga '${targetLeague.name}' excluída com sucesso",
                    details = "Todos os clubes, jogadores, histórico e configurações locais foram totalmente removidos."
                )
            }

            // 5. REMOÇÃO REMOTA NA NUVEM (FIRESTORE) BATCHED WRITE
            if (targetLeague.isOnline && !cloudId.isNullOrBlank()) {
                val subcollections = listOf(
                    "members", "clubs", "players", "matches", "standings", "transfers",
                    "competitions", "seasons", "rounds", "participants", "arena_duels",
                    "auction_lots", "auction_items", "auction_bids", "news", "goal_events"
                )

                subcollections.forEach { sub ->
                    try {
                        val snapshot = firestore.collection("leagues").document(cloudId)
                            .collection(sub).get().await()

                        if (!snapshot.isEmpty) {
                            var deletedInSub = 0
                            val chunks = snapshot.documents.chunked(400)
                            for (chunk in chunks) {
                                val batch = firestore.batch()
                                for (doc in chunk) {
                                    batch.delete(doc.reference)
                                }
                                batch.commit().await()
                                deletedInSub += chunk.size
                            }
                            Log.d(TAG, "Exclusão remota da subcoleção '$sub': $deletedInSub documentos removidos.")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Erro ao excluir subcoleção remota '$sub'", e)
                    }
                }

                // Apagar o documento pai da liga
                try {
                    firestore.collection("leagues").document(cloudId).delete().await()
                    Log.d(TAG, "Documento da liga 'leagues/$cloudId' apagado do Firestore.")
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao apagar o documento pai da liga 'leagues/$cloudId'", e)
                }

                // Desvincular cloudLeagueId nos user_profiles
                try {
                    val profileQuery = firestore.collection("user_profiles")
                        .whereEqualTo("cloudLeagueId", cloudId)
                        .get().await()

                    for (doc in profileQuery.documents) {
                        doc.reference.update(
                            mapOf(
                                "cloudLeagueId" to null,
                                "memberships" to FieldValue.arrayRemove(cloudId)
                            )
                        ).await()
                    }
                    Log.d(TAG, "Vínculos da liga $cloudId removidos dos user_profiles (${profileQuery.size()} perfis atualizados).")
                } catch (e: Exception) {
                    Log.w(TAG, "Erro ao desvincular perfis em user_profiles", e)
                }
            }

            DeleteLeagueResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico ao executar Exclusão de Liga", e)
            DeleteLeagueResult.Error("Falha ao excluir a liga: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "DeleteLeagueUseCase"
    }
}
