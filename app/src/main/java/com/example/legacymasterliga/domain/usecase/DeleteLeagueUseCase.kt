package com.example.legacymasterliga.domain.usecase

import android.util.Log
import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.OnlineSyncDao
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.google.firebase.firestore.DocumentReference
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

data class PurgeGhostResult(
    val cloudLeagueId: String?,
    val deletedSubcollectionsLog: List<String>,
    val profilesUpdatedCount: Int,
    val localMirrorRemoved: Boolean
)

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

                db.execSQL("DELETE FROM players WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM financial_transactions WHERE clubId IN (SELECT id FROM clubs WHERE leagueId = $targetLeagueId)")
                db.execSQL("DELETE FROM transfers WHERE leagueId = $targetLeagueId")
                db.execSQL("DELETE FROM goal_events WHERE matchId IN (SELECT id FROM matches WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId)))")
                db.execSQL("DELETE FROM competition_participants WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM standings WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM matches WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM rounds WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId))")
                db.execSQL("DELETE FROM season_closures WHERE seasonId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = $targetLeagueId)))")
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
                db.execSQL("DELETE FROM leagues WHERE id = $targetLeagueId")

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

            // 5. REMOÇÃO REMOTA NA NUVEM (FIRESTORE) - Erros são propagados
            if (targetLeague.isOnline && !cloudId.isNullOrBlank()) {
                deleteCloudLeagueAndSubcollections(cloudId)
                unlinkLeagueFromUserProfiles(cloudId)
            }

            DeleteLeagueResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico ao executar Exclusão de Liga", e)
            DeleteLeagueResult.Error("Falha ao excluir a liga na nuvem/local: ${e.message}")
        }
    }

    /**
     * Purga resíduos de uma liga fantasma remanescente ("Liga M L Amigos") do Firestore e do Room local.
     */
    suspend fun purgeGhostLeague(
        targetLeagueName: String = "Liga M L Amigos"
    ): PurgeGhostResult = withContext(Dispatchers.IO) {
        val deletedLog = mutableListOf<String>()
        var updatedProfilesCount = 0
        var removedLocalMirror = false
        var cloudId: String? = null

        // 1. Verificar se a liga fantasma existe no Room local
        val localLeague = leagueDao.findByName(targetLeagueName)
        if (localLeague != null) {
            cloudId = localLeague.cloudLeagueId
            database.withTransaction {
                val db = database.openHelper.writableDatabase
                db.execSQL("DELETE FROM leagues WHERE id = ${localLeague.id}")
                if (!cloudId.isNullOrBlank()) {
                    db.execSQL("DELETE FROM online_sync_queue WHERE cloudLeagueId = '$cloudId'")
                    db.execSQL("DELETE FROM online_sync_records WHERE cloudLeagueId = '$cloudId'")
                }
            }
            removedLocalMirror = true
            Log.d(TAG, "Espelho local da liga fantasma '${localLeague.name}' removido do Room.")
        }

        // 2. Buscar documento da liga no Firestore se o cloudId não veio da base local
        if (cloudId.isNullOrBlank()) {
            try {
                val leaguesQuery = firestore.collection("leagues")
                    .whereEqualTo("name", targetLeagueName)
                    .get().await()
                cloudId = leaguesQuery.documents.firstOrNull()?.id
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao buscar liga '$targetLeagueName' por nome no Firestore", e)
            }
        }

        // 3. Se identificou o cloudId da liga fantasma, limpa subcoleções e desvincula perfis
        if (!cloudId.isNullOrBlank()) {
            val logs = deleteCloudLeagueAndSubcollections(cloudId)
            deletedLog.addAll(logs)
            updatedProfilesCount = unlinkLeagueFromUserProfiles(cloudId)
        }

        PurgeGhostResult(
            cloudLeagueId = cloudId,
            deletedSubcollectionsLog = deletedLog,
            profilesUpdatedCount = updatedProfilesCount,
            localMirrorRemoved = removedLocalMirror
        )
    }

    private suspend fun deleteCloudLeagueAndSubcollections(cloudId: String): List<String> {
        val logs = mutableListOf<String>()
        val subcollections = listOf(
            "members", "clubs", "players", "matches", "standings", "transfers",
            "competitions", "seasons", "rounds", "participants", "arena_duels",
            "auction_lots", "auction_items", "auction_bids", "news", "goal_events"
        )

        subcollections.forEach { sub ->
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
                val msg = "Subcoleção '$sub': $deletedInSub documentos removidos."
                logs.add(msg)
                Log.d(TAG, msg)
            }
        }

        // Apagar o documento pai da liga
        firestore.collection("leagues").document(cloudId).delete().await()
        Log.d(TAG, "Documento pai 'leagues/$cloudId' apagado do Firestore.")

        return logs
    }

    private suspend fun unlinkLeagueFromUserProfiles(cloudId: String): Int {
        val profileRefs = mutableSetOf<DocumentReference>()

        try {
            val queryDirect = firestore.collection("user_profiles")
                .whereEqualTo("cloudLeagueId", cloudId)
                .get().await()
            queryDirect.documents.forEach { profileRefs.add(it.reference) }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao buscar user_profiles por cloudLeagueId", e)
        }

        try {
            val queryMemberships = firestore.collection("user_profiles")
                .whereArrayContains("memberships", cloudId)
                .get().await()
            queryMemberships.documents.forEach { profileRefs.add(it.reference) }
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao buscar user_profiles por memberships arrayContains", e)
        }

        var profilesUpdatedCount = 0
        for (ref in profileRefs) {
            val snapshot = ref.get().await()
            val updates = mutableMapOf<String, Any?>()

            if (snapshot.getString("cloudLeagueId") == cloudId) {
                updates["cloudLeagueId"] = null
            }
            val membershipsList = snapshot.get("memberships") as? List<String> ?: emptyList()
            if (cloudId in membershipsList) {
                updates["memberships"] = FieldValue.arrayRemove(cloudId)
            }

            if (updates.isNotEmpty()) {
                ref.update(updates).await()
                profilesUpdatedCount++
            }
        }
        Log.d(TAG, "Vínculos da liga $cloudId removidos em $profilesUpdatedCount perfis no Firestore.")
        return profilesUpdatedCount
    }

    private companion object {
        const val TAG = "DeleteLeagueUseCase"
    }
}
