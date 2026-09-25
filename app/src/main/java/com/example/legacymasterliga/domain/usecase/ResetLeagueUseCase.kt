package com.example.legacymasterliga.domain.usecase

import android.util.Log
import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.OnlineSyncDao
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.feature.audit.domain.AuditLogger
import com.example.legacymasterliga.feature.backup.domain.BackupRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface ResetLeagueResult {
    data object Success : ResetLeagueResult
    data class Error(val message: String) : ResetLeagueResult
}

@Singleton
class ResetLeagueUseCase @Inject constructor(
    private val database: AppDatabase,
    private val leagueDao: LeagueDao,
    private val clubDao: ClubDao,
    private val financialDao: FinancialDao,
    private val syncDao: OnlineSyncDao,
    private val backupRepository: BackupRepository,
    private val firestore: FirebaseFirestore,
    private val auditLogger: AuditLogger,
) {
    suspend operator fun invoke(leagueId: Long): ResetLeagueResult = withContext(Dispatchers.IO) {
        try {
            // 1. GUARDA DE FILA: Se houver operações de sync pendentes, aborta.
            val pendingCount = syncDao.observePendingCount().first()
            if (pendingCount > 0) {
                return@withContext ResetLeagueResult.Error(
                    "Existem $pendingCount operações de sincronização pendentes. Sincronize antes de resetar a liga."
                )
            }

            // 2. BACKUP AUTOMÁTICO: Gera backup de segurança do estado atual do app.
            backupRepository.createManualBackup().onFailure { error ->
                Log.w(TAG, "Falha ao criar backup automático de pré-reset", error)
            }

            val league = leagueDao.findById(leagueId)
                ?: return@withContext ResetLeagueResult.Error("Liga não encontrada.")

            val bankClub = clubDao.findBankByLeague(leagueId)
                ?: return@withContext ResetLeagueResult.Error("Banco da Liga não encontrado.")

            val now = System.currentTimeMillis()

            // 3. EXECUÇÃO ATÔMICA NO BANCO LOCAL (Room)
            database.withTransaction {
                val db = database.openHelper.writableDatabase

                // A. JOGADORES: Apagar todos os jogadores da liga (localmente)
                db.execSQL("DELETE FROM players WHERE leagueId = $leagueId")

                // B. FINANCEIRO: Calcular saldo atual de cada clube e lançar ajuste compensatório RESET_ADJUSTMENT para 500 CR
                database.query(
                    "SELECT id, name FROM clubs WHERE leagueId = ? AND isBank = 0 AND isActive = 1",
                    arrayOf(leagueId.toString())
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val clubId = cursor.getLong(0)
                        val clubName = cursor.getString(1)
                        val currentBalance = financialDao.balanceOf(clubId)
                        val targetBalance = DEFAULT_INITIAL_BALANCE_CR
                        val diff = targetBalance - currentBalance

                        if (diff != 0L) {
                            val idempotencyKey = "reset_${leagueId}_${clubId}_$now"
                            financialDao.insert(
                                FinancialTransactionEntity(
                                    clubId = clubId,
                                    amountCr = diff,
                                    description = "Ajuste de Reset de Temporada",
                                    type = "RESET_ADJUSTMENT",
                                    counterpartyClubId = bankClub.id,
                                    createdAt = now,
                                    idempotencyKey = idempotencyKey
                                )
                            )
                            financialDao.insert(
                                FinancialTransactionEntity(
                                    clubId = bankClub.id,
                                    amountCr = -diff,
                                    description = "Ajuste de Reset: $clubName",
                                    type = "RESET_ADJUSTMENT",
                                    counterpartyClubId = clubId,
                                    createdAt = now,
                                    idempotencyKey = "bank_$idempotencyKey"
                                )
                            )
                        }
                    }
                }

                // C. HISTÓRICO LIMPO LOCAL
                val tablesToClear = listOf(
                    "competitions", "seasons", "rounds", "matches", "standings",
                    "competition_participants", "competition_settings", "competition_prizes",
                    "season_closures", "final_standings", "prize_history",
                    "arena_duels", "auction_bids", "auction_items", "auction_lots",
                    "news", "notifications", "notification_reads", "transfers", "goal_events"
                )
                tablesToClear.forEach { table ->
                    db.execSQL("DELETE FROM $table")
                }

                // D. LIMPEZA DA FILA DE SYNC LOCAL PARA ENTIDADES LIMPAS
                val deadEntityTypes = listOf(
                    "COMPETITION", "SEASON", "ROUND", "MATCH", "STANDING",
                    "PARTICIPANT", "TRANSFER", "NEWS", "ARENA", "GOAL_EVENT",
                    "AUCTION_LOT", "AUCTION_ITEM", "AUCTION_BID", "PLAYER"
                ).joinToString(",") { "'$it'" }

                db.execSQL("DELETE FROM online_sync_queue WHERE entityType IN ($deadEntityTypes)")
                db.execSQL("DELETE FROM online_sync_records WHERE entityType IN ($deadEntityTypes)")

                auditLogger.log(
                    category = "LEAGUE",
                    action = "LEAGUE_RESET",
                    entityType = "LEAGUE",
                    entityId = leagueId,
                    leagueId = leagueId,
                    summary = "Reset de Liga executado",
                    details = "Jogadores e histórico apagados; saldos ajustados para $DEFAULT_INITIAL_BALANCE_CR CR."
                )
            }

            // 4. LIMPEZA NA NUVEM (FIRESTORE) BATCHED WRITE
            val cloudId = league.cloudLeagueId
            if (league.isOnline && !cloudId.isNullOrBlank()) {
                val subcollections = listOf(
                    "matches", "standings", "transfers", "competitions",
                    "seasons", "rounds", "participants", "arena_duels",
                    "auction_lots", "auction_items", "auction_bids", "news", "goal_events", "players"
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
                            Log.d(TAG, "Limpeza de subcoleção remota '$sub': $deletedInSub documentos removidos via batch.")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Erro ao limpar subcoleção remota '$sub'", e)
                    }
                }
            }

            ResetLeagueResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Erro crítico ao executar Reset da Liga", e)
            ResetLeagueResult.Error("Falha ao executar o Reset da Liga: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "ResetLeagueUseCase"
        const val DEFAULT_INITIAL_BALANCE_CR = 500L
    }
}
