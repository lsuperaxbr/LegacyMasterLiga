package com.example.legacymasterliga.feature.online.sync

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.CompetitionDao
import com.example.legacymasterliga.core.database.dao.CompetitionParticipantDao
import com.example.legacymasterliga.core.database.dao.NewsDao
import com.example.legacymasterliga.core.database.dao.FinancialDao
import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.MatchDao
import com.example.legacymasterliga.core.database.dao.OnlineSyncDao
import com.example.legacymasterliga.core.database.dao.RoundDao
import com.example.legacymasterliga.core.database.dao.SeasonDao
import com.example.legacymasterliga.core.database.dao.StandingDao
import com.example.legacymasterliga.core.database.dao.TransferDao
import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.CompetitionEntity
import com.example.legacymasterliga.core.database.entity.CompetitionParticipantEntity
import com.example.legacymasterliga.core.database.entity.NewsEntity
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.entity.MatchEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncQueueEntity
import com.example.legacymasterliga.core.database.entity.OnlineSyncRecordEntity
import com.example.legacymasterliga.core.database.entity.RoundEntity
import com.example.legacymasterliga.core.database.entity.SeasonEntity
import com.example.legacymasterliga.core.database.entity.StandingEntity
import com.example.legacymasterliga.core.database.entity.TransferEntity
import com.example.legacymasterliga.core.database.model.OnlineSyncCandidate
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.MatchStatus
import com.example.legacymasterliga.core.model.RoundStatus
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/** ONLINE-006: Room remains the local source used by the UI; this component mirrors online leagues. */
@Singleton
class OnlineSportsSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val leagueDao: LeagueDao,
    private val clubDao: ClubDao,
    private val userDao: UserDao,
    private val competitionDao: CompetitionDao,
    private val participantDao: CompetitionParticipantDao,
    private val seasonDao: SeasonDao,
    private val roundDao: RoundDao,
    private val matchDao: MatchDao,
    private val standingDao: StandingDao,
    private val financialDao: FinancialDao,
    private val transferDao: TransferDao,
    private val playerDao: com.example.legacymasterliga.core.database.dao.PlayerDao,
    private val newsDao: NewsDao,
    private val syncDao: OnlineSyncDao,
    private val goalEventDao: com.example.legacymasterliga.core.database.dao.GoalEventDao,
    private val auctionDao: com.example.legacymasterliga.core.database.dao.AuctionDao,
) {
    private val prefs = context.getSharedPreferences("online_sync_preferences", Context.MODE_PRIVATE)
    val pendingCount: Flow<Int> = syncDao.observePendingCount()
    val conflictCount: Flow<Int> = syncDao.observeConflictCount()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private val mutationMutex = Mutex()
    private val listeners = mutableMapOf<String, List<ListenerRegistration>>()
    @Volatile private var latestCandidates: List<OnlineSyncCandidate> = emptyList()
    @Volatile private var authenticatedProfileUid: String? = null
    @Volatile private var activeMembershipRoles: Map<String, String> = emptyMap()
    private val logBuffer = java.util.Collections.synchronizedList(mutableListOf<String>())

    private fun syncLog(msg: String, error: Throwable? = null) {
        if (error != null) Log.d(TAG, msg, error) else Log.d(TAG, msg)
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val suffix = error?.let { " | Erro: ${it.message}" } ?: ""
        logBuffer.add(0, "[$timestamp] $msg$suffix")
        if (logBuffer.size > 20) logBuffer.removeAt(logBuffer.size - 1)
    }

    fun getDiagnosticInfo() = mapOf(
        "uid" to (authenticatedProfileUid ?: "Nulo"),
        "memberships" to activeMembershipRoles.map { "${it.key}|${it.value}" },
        "logs" to logBuffer.toList()
    )

    private val activeMembershipLeagueIds: Set<String>
        get() = activeMembershipRoles.keys

    /**
     * Sports sync is opt-in after Auth AND user_profile have both succeeded.
     * Merely opening the app, an offline league, or restoring a cached Auth user never enables it.
     */
    fun startAuthenticatedSession(profile: CloudUserProfile) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            syncLog( "Falha no bootstrap: Usuário Firebase é nulo.")
            disableSession()
            return
        }
        if (firebaseUser.uid != profile.firebaseUid) {
            syncLog( "Falha no bootstrap: UID mismatch (Auth=${firebaseUser.uid} != Profile=${profile.firebaseUid}).")
            disableSession()
            return
        }
        if (profile.status != ACTIVE) {
            syncLog( "Falha no bootstrap: Perfil inativo (${profile.status}).")
            disableSession()
            return
        }
        authenticatedProfileUid = profile.firebaseUid
        
        // Otimização #2: Carrega o cache IMEDIATAMENTE
        activeMembershipRoles = loadMembershipCache(profile.firebaseUid)

        if (started.compareAndSet(false, true)) {
            auth.addAuthStateListener { currentAuth ->
                if (currentAuth.currentUser?.uid != authenticatedProfileUid) {
                    syncLog( "Sessão invalidada por mudança de estado Auth.")
                    disableSession()
                }
            }
            firestore.addSnapshotsInSyncListener { scope.launch { flushQueue() } }

            // Otimização #2: Inicia listeners imediatamente com base no cache
            if (activeMembershipLeagueIds.isNotEmpty()) {
                syncLog( "Iniciando listeners via CACHE para: $activeMembershipLeagueIds")
                scope.launch { refreshListeners() }
            }

            // Listener em Tempo Real para o Perfil do Usuário
            authenticatedProfileUid?.let { uid ->
                val profileRef = firestore.collection("user_profiles").document(uid)
                listeners["PROFILE"] = listOf(
                    profileRef.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            syncLog( "Erro no listener de perfil", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            syncLog( "Perfil atualizado no Firestore. Agendando validação de memberships...")
                            scope.launch { refreshMemberships() }
                        }
                    }
                )
            }

            scope.launch {
                leagueDao.observeOnline().collectLatest {
                    syncLog( "Mudança observada em ligas locais. Atualizando memberships...")
                    refreshMemberships()
                }
            }
            scope.launch {
                syncDao.observeCandidates().collectLatest { candidates ->
                    latestCandidates = candidates
                    scanAndFlush()
                }
            }
        }
        
        // Correção de Bug: Dispara refresh IMEDIATAMENTE e incondicionalmente no bootstrap
        scope.launch {
            syncLog( "Bootstrap: Iniciando refreshMemberships incondicional.")
            // Carga Imediata: Se o perfil já diz qual é a liga, espelha agora mesmo
            profile.cloudLeagueId?.let { leagueId ->
                ensureLocalLeagueMirror(leagueId)
            }
            refreshMemberships()
        }
    }

    private fun disableSession() {
        authenticatedProfileUid = null
        activeMembershipRoles = emptyMap()
        listeners.values.flatten().forEach(ListenerRegistration::remove)
        listeners.clear()
        started.set(false)
    }

    private fun syncEnabled(leagueId: String): Boolean {
        val uid = authenticatedProfileUid
        if (uid == null) {
            syncLog( "Sync ignorado: authenticatedProfileUid é nulo.")
            return false
        }
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != uid) {
            syncLog( "Sync ignorado: UID mismatch (Auth=$currentUserUid != Session=$uid).")
            return false
        }
        if (leagueId.isBlank()) {
            syncLog( "Sync ignorado: leagueId está em branco.")
            return false
        }
        if (leagueId !in activeMembershipLeagueIds) {
            syncLog( "Sync ignorado: Liga $leagueId não encontrada nas memberships ativas ($activeMembershipLeagueIds).")
            return false
        }
        return true
    }

    private suspend fun refreshMemberships() {
        val uid = authenticatedProfileUid ?: return
        if (auth.currentUser?.uid != uid) return
        
        syncLog( "Validando memberships com o servidor para $uid")

        val verifiedRoles = mutableMapOf<String, String>()
        val leaguesToPull = mutableSetOf<String>()
        
        try {
            // 1. Descoberta por Perfil (Vínculo Direto)
            val profileDoc = firestore.collection("user_profiles").document(uid).get().await()
            val savedLeagueId = profileDoc.getString("cloudLeagueId")
            val memberships = profileDoc.get("memberships") as? List<String> ?: emptyList()
            
            val candidates = (memberships + listOfNotNull(savedLeagueId)).toSet()
            
            candidates.forEach { leagueId ->
                try {
                    val member = firestore.collection("leagues").document(leagueId)
                        .collection("members").document(uid).get().await()
                    
                    if (member.exists() && member.getString("status") == ACTIVE) {
                        val role = member.getString("role") ?: "PRESIDENT"
                        verifiedRoles[leagueId] = role
                        ensureLocalLeagueMirror(leagueId)
                        if (leagueId !in activeMembershipLeagueIds) leaguesToPull += leagueId
                    }
                } catch (e: Exception) {
                    syncLog( "Falha ao verificar liga $leagueId do perfil", e)
                }
            }

            // 2. Scan Global (Vizinhança): Caso o usuário seja membro de outras ligas
            try {
                val membershipsQuery = firestore.collectionGroup("members")
                    .whereEqualTo("firebaseUid", uid)
                    .get().await()
                
                membershipsQuery.documents.forEach { memberDoc ->
                    val leagueId = memberDoc.reference.parent.parent?.id
                    if (leagueId != null && leagueId !in verifiedRoles && memberDoc.getString("status") == ACTIVE) {
                        val role = memberDoc.getString("role") ?: "PRESIDENT"
                        verifiedRoles[leagueId] = role
                        ensureLocalLeagueMirror(leagueId)
                        if (leagueId !in activeMembershipLeagueIds) leaguesToPull += leagueId
                    }
                }
            } catch (e: Exception) {
                syncLog("Falha no Scan Global de memberships. Prosseguindo com ligas do perfil.", e)
            }

            if (verifiedRoles != activeMembershipRoles) {
                syncLog( "Mudança de memberships detectada pelo servidor: $verifiedRoles")
                activeMembershipRoles = verifiedRoles
                saveMembershipCache(uid, verifiedRoles)
                refreshListeners()
            } else {
                syncLog( "Memberships confirmadas (sem alterações).")
            }

            // Carga inicial para novas ligas detectadas
            leaguesToPull.forEach { leagueId -> scope.launch { pullAll(leagueId) } }
            
            syncLog( "RefreshMemberships concluído. Disparando scanAndFlush.")
            scanAndFlush()
        } catch (e: Exception) {
            syncLog( "Falha no refreshMemberships (Rede). Mantendo listeners do cache.", e)
        }
    }

    private fun loadMembershipCache(uid: String): Map<String, String> {
        val cached = prefs.getStringSet("membership_roles_$uid", emptySet()) ?: emptySet()
        return cached.mapNotNull { 
            val parts = it.split("|")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    private fun saveMembershipCache(uid: String, roles: Map<String, String>) {
        val toSave = roles.map { "${it.key}|${it.value}" }.toSet()
        prefs.edit().putStringSet("membership_roles_$uid", toSave).apply()
    }

    private suspend fun ensureLocalLeagueMirror(cloudLeagueId: String) {
        if (leagueDao.findByCloudId(cloudLeagueId) != null) return
        
        try {
            val leagueDoc = firestore.collection("leagues").document(cloudLeagueId).get().await()
            if (leagueDoc.exists()) {
                val name = leagueDoc.getString("name") ?: "Liga Online"
                val existingLocal = leagueDao.findByName(name)
                
                if (existingLocal != null) {
                    leagueDao.linkCloudId(existingLocal.id, cloudLeagueId)
                } else {
                    leagueDao.insert(
                        com.example.legacymasterliga.core.database.entity.LeagueEntity(
                            name = name,
                            cloudLeagueId = cloudLeagueId,
                            isOnline = true
                        )
                    )
                }
                syncLog( "Liga $cloudLeagueId espelhada localmente com sucesso.")
            }
        } catch (e: Exception) {
            syncLog( "Falha ao espelhar liga $cloudLeagueId", e)
        }
    }

    fun triggerFullSync() {
        val leagueIds = activeMembershipLeagueIds
        leagueIds.forEach { leagueId ->
            scope.launch { pullAll(leagueId) }
        }
    }

    /** Força o re-escaneamento de ligas e cargas iniciais imediatamente. */
    fun refreshImmediately() {
        scope.launch { refreshMemberships() }
    }

    private suspend fun pullAll(leagueId: String) {
        if (!syncEnabled(leagueId)) return
        syncLog( "Iniciando pullAll para $leagueId")
        TYPES.forEach { type ->
            try {
                val snapshot = firestore.collection("leagues").document(leagueId)
                    .collection(collectionFor(type)).get().await()
                snapshot.documents.forEach { doc ->
                    val data = doc.data
                    if (data != null) applyRemote(leagueId, type, doc.id, data)
                }
                
                // PERFORMANCE-001: Recalcula a tabela apenas uma vez por categoria de dados baixada
                if (type == "PARTICIPANT" || type == "MATCH" || type == "STANDING") {
                    database.query("SELECT id FROM seasons WHERE competitionId IN (SELECT id FROM competitions WHERE leagueId = (SELECT id FROM leagues WHERE cloudLeagueId = ?))", arrayOf(leagueId)).use { cursor ->
                        while (cursor.moveToNext()) {
                            recalculateStandings(cursor.getLong(0))
                        }
                    }
                }
            } catch (e: Exception) {
                syncLog( "Falha no pullAll de $type para $leagueId", e)
            }
        }
    }

    /** Explicit safe resolution: discard local conflicting scores and accept the current cloud versions. */
    fun acceptRemoteConflicts() {
        scope.launch {
            syncDao.findConflicts().forEach { record ->
                if (!syncEnabled(record.cloudLeagueId)) return@forEach
                syncDao.deleteConflictQueue(record.entityType, record.localId)
                val snapshot = firestore.collection("leagues").document(record.cloudLeagueId)
                    .collection(collectionFor(record.entityType)).document(record.cloudId).get().await()
                if (snapshot.exists()) applyRemote(record.cloudLeagueId, record.entityType, record.cloudId, snapshot.data.orEmpty())
            }
        }
    }

    private suspend fun scanAndFlush() {
        if (authenticatedProfileUid == null) {
            syncLog( "scanAndFlush abortado: authenticatedProfileUid é nulo.")
            return
        }
        if (auth.currentUser?.uid != authenticatedProfileUid) {
            syncLog( "scanAndFlush abortado: UID mismatch.")
            return
        }
        mutationMutex.withLock {
            latestCandidates.filter { syncEnabled(it.cloudLeagueId) }.forEach { candidate ->
                val record = ensureRecord(candidate)
                if (candidate.updatedAt > record.lastLocalUpdatedAt && syncDao.findPending(candidate.entityType, candidate.localId) == null) {
                    syncDao.enqueue(
                        OnlineSyncQueueEntity(
                            operationId = UUID.randomUUID().toString(),
                            entityType = candidate.entityType,
                            localId = candidate.localId,
                            cloudLeagueId = candidate.cloudLeagueId,
                            baseRevision = record.revision,
                        ),
                    )
                }
            }
        }
        flushQueue()
    }

    private suspend fun ensureRecord(candidate: OnlineSyncCandidate): OnlineSyncRecordEntity {
        syncDao.findRecord(candidate.entityType, candidate.localId)?.let { return it }
        val created = OnlineSyncRecordEntity(
            entityType = candidate.entityType,
            localId = candidate.localId,
            cloudLeagueId = candidate.cloudLeagueId,
            cloudId = UUID.randomUUID().toString(),
            status = "PENDING",
        )
        syncDao.insertRecord(created)
        return syncDao.findRecord(candidate.entityType, candidate.localId) ?: created
    }

    private suspend fun ensureRecord(type: String, localId: Long, leagueId: String): OnlineSyncRecordEntity =
        syncDao.findRecord(type, localId) ?: OnlineSyncRecordEntity(
            entityType = type,
            localId = localId,
            cloudLeagueId = leagueId,
            cloudId = UUID.randomUUID().toString(),
            status = "PENDING",
        ).also { syncDao.insertRecord(it) }

    private suspend fun flushQueue() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            syncLog( "flushQueue abortado: Usuário Firebase é nulo.")
            return
        }
        if (uid != authenticatedProfileUid) {
            syncLog( "flushQueue abortado: UID mismatch.")
            return
        }
        if (!mutationMutex.tryLock()) {
            syncLog( "flushQueue ignorado: Mutex ocupado.")
            return
        }
        try {
            syncDao.findPendingBatch().filter { syncEnabled(it.cloudLeagueId) }.forEach { operation -> upload(operation, uid) }
        } finally {
            mutationMutex.unlock()
        }
    }

    private suspend fun upload(operation: OnlineSyncQueueEntity, uid: String) {
        val record = syncDao.findRecord(operation.entityType, operation.localId) ?: return
        val payload = buildPayload(operation, record) ?: run {
            syncDao.deleteQueue(operation.operationId)
            return
        }
        val clientUpdatedAt = payload["clientUpdatedAt"] as Long
        
        val ref = firestore.collection("leagues").document(operation.cloudLeagueId)
            .collection(collectionFor(operation.entityType)).document(record.cloudId)

        try {
            val outcome = firestore.runTransaction { transaction ->
                val remote = transaction.get(ref)
                val remoteRevision = remote.getLong("revision") ?: 0L
                if (remote.exists()) {
                    when (OnlineConflictPolicy.decide(operation.entityType, operation.baseRevision, remoteRevision, payload, remote.data.orEmpty())) {
                        OnlineConflictDecision.SCORE_CONFLICT -> return@runTransaction TransactionOutcome.ScoreConflict(remote.data.orEmpty().toString())
                        OnlineConflictDecision.REMOTE_WINS -> return@runTransaction TransactionOutcome.RemoteWins(remote.data.orEmpty())
                        OnlineConflictDecision.UPLOAD -> Unit
                    }
                }
                val revision = remoteRevision + 1
                transaction.set(
                    ref,
                    payload + mapOf(
                        "revision" to revision,
                        "authorUid" to uid,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                )
                TransactionOutcome.Uploaded(revision)
            }.await()
            when (outcome) {
                is TransactionOutcome.Uploaded -> {
                    syncDao.updateRecord(
                        record.copy(
                            revision = outcome.revision,
                            lastLocalUpdatedAt = clientUpdatedAt,
                            lastRemoteUpdatedAt = System.currentTimeMillis(),
                            status = "SYNCED",
                            conflictPayload = null,
                        ),
                    )
                    syncDao.deleteQueue(operation.operationId)
                }
                is TransactionOutcome.ScoreConflict -> {
                    syncDao.updateRecord(record.copy(status = "CONFLICT", conflictPayload = outcome.remotePayload))
                    syncDao.updateQueue(operation.copy(status = "CONFLICT", lastError = "Placar alterado em outro dispositivo", updatedAt = System.currentTimeMillis()))
                }
                is TransactionOutcome.RemoteWins -> {
                    syncDao.deleteQueue(operation.operationId)
                    // upload() runs under mutationMutex; defer the local merge until flushQueue releases it.
                    scope.launch { applyRemote(operation.cloudLeagueId, operation.entityType, record.cloudId, outcome.data) }
                }
            }
        } catch (error: Exception) {
            syncLog( "Falha no upload ${operation.entityType}/${operation.localId}", error)
            // Firestore transactions fail offline. Keep the operation durable for the next snapshot/auth/network event.
            syncDao.updateQueue(
                operation.copy(
                    status = "PENDING",
                    attempts = operation.attempts + 1,
                    lastError = error.message,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            syncLog( "Operação ${operation.operationId} mantida na fila", error)
        }
    }

    private suspend fun buildPayload(
        operation: OnlineSyncQueueEntity,
        record: OnlineSyncRecordEntity,
    ): Map<String, Any?>? {
        val common = mapOf(
            "cloudId" to record.cloudId,
            "schemaVersion" to SCHEMA_VERSION,
            "revision" to record.revision,
            "clientUpdatedAt" to entityUpdatedAt(operation.entityType, operation.localId),
        )
        return when (operation.entityType) {
            "COMPETITION" -> competitionDao.findById(operation.localId)?.let { c ->
                common + mapOf(
                    "name" to c.name, "type" to c.type.name, "format" to c.format.name, "status" to c.status.name,
                    "pointsForWin" to c.pointsForWin, "pointsForDraw" to c.pointsForDraw, "pointsForLoss" to c.pointsForLoss,
                    "groupCount" to c.groupCount, "qualifiedPerGroup" to c.qualifiedPerGroup, "knockoutLegs" to c.knockoutLegs,
                    "createdAt" to c.createdAt,
                )
            }
            "CLUB" -> clubDao.findById(operation.localId)?.let { cl ->
                val presidentUid = cl.presidentUserId?.let { userId ->
                    userDao.findById(userId)?.firebaseUid
                }
                
                common + mapOf(
                    "name" to cl.name, 
                    "crestUri" to cl.crestUri, 
                    "presidentFirebaseUid" to presidentUid,
                    "isBank" to cl.isBank, "isActive" to cl.isActive, "createdAt" to cl.createdAt,
                )
            }
            "SEASON" -> seasonDao.findById(operation.localId)?.let { s ->
                val parent = ensureRecord("COMPETITION", s.competitionId, operation.cloudLeagueId)
                common + mapOf(
                    "competitionCloudId" to parent.cloudId, "number" to s.number, "name" to s.name, "status" to s.status.name,
                    "startedAt" to s.startedAt, "finishedAt" to s.finishedAt, "createdAt" to s.createdAt,
                )
            }
            "PARTICIPANT" -> participantDao.findById(operation.localId)?.let { p ->
                val season = ensureRecord("SEASON", p.seasonId, operation.cloudLeagueId)
                val club = clubDao.findById(p.clubId) ?: return null
                common + mapOf(
                    "seasonCloudId" to season.cloudId, "clubKey" to clubKey(club.name), "clubName" to club.name,
                    "seed" to p.seed, "isActive" to p.isActive, "createdAt" to p.createdAt,
                )
            }
            "ROUND" -> roundDao.findById(operation.localId)?.let { r ->
                val parent = ensureRecord("SEASON", r.seasonId, operation.cloudLeagueId)
                common + mapOf(
                    "seasonCloudId" to parent.cloudId, "number" to r.number, "name" to r.name, "stage" to r.stage,
                    "stageLabel" to r.stageLabel, "groupIndex" to r.groupIndex, "status" to r.status.name, "createdAt" to r.createdAt,
                )
            }
            "MATCH" -> matchDao.findById(operation.localId)?.let { m ->
                val season = ensureRecord("SEASON", m.seasonId, operation.cloudLeagueId)
                val round = ensureRecord("ROUND", m.roundId, operation.cloudLeagueId)
                val home = clubDao.findById(m.homeClubId) ?: return null
                val away = clubDao.findById(m.awayClubId) ?: return null
                common + mapOf(
                    "seasonCloudId" to season.cloudId, "roundCloudId" to round.cloudId,
                    "homeClubKey" to clubKey(home.name), "homeClubName" to home.name,
                    "awayClubKey" to clubKey(away.name), "awayClubName" to away.name,
                    "pairingKey" to m.pairingKey, "leg" to m.leg, "stage" to m.stage, "groupIndex" to m.groupIndex,
                    "bracketPosition" to m.bracketPosition, "penaltiesHome" to m.penaltiesHome, "penaltiesAway" to m.penaltiesAway,
                    "winnerClubKey" to m.winnerClubId?.let { clubDao.findById(it)?.name?.let(::clubKey) },
                    "advanceReason" to m.advanceReason, "homeScore" to m.homeScore, "awayScore" to m.awayScore,
                    "homeYellowCards" to m.homeYellowCards, "homeRedCards" to m.homeRedCards,
                    "awayYellowCards" to m.awayYellowCards, "awayRedCards" to m.awayRedCards,
                    "status" to m.status.name, "scheduledAt" to m.scheduledAt, "createdAt" to m.createdAt,
                )
            }
            "STANDING" -> findStanding(operation.localId)?.let { st ->
                val season = ensureRecord("SEASON", st.seasonId, operation.cloudLeagueId)
                val club = clubDao.findById(st.clubId) ?: return null
                common + mapOf(
                    "seasonCloudId" to season.cloudId, "clubKey" to clubKey(club.name), "clubName" to club.name,
                    "groupIndex" to st.groupIndex, "played" to st.played, "wins" to st.wins, "draws" to st.draws,
                    "losses" to st.losses, "goalsFor" to st.goalsFor, "goalsAgainst" to st.goalsAgainst,
                    "goalDifference" to st.goalDifference, "points" to st.points,
                )
            }
            "FINANCE" -> financialDao.findById(operation.localId)?.let { ft ->
                val club = ensureRecord("CLUB", ft.clubId, operation.cloudLeagueId)
                val counterparty = ft.counterpartyClubId?.let { ensureRecord("CLUB", it, operation.cloudLeagueId) }
                val transfer = ft.transferId?.let { ensureRecord("TRANSFER", it, operation.cloudLeagueId) }
                common + mapOf(
                    "clubCloudId" to club.cloudId, "amountCr" to ft.amountCr, "description" to ft.description,
                    "type" to ft.type, "counterpartyClubCloudId" to counterparty?.cloudId,
                    "transferCloudId" to transfer?.cloudId, "createdAt" to ft.createdAt,
                )
            }
            "TRANSFER" -> transferDao.findById(operation.localId)?.let { t ->
                val origin = ensureRecord("CLUB", t.originClubId, operation.cloudLeagueId)
                val dest = ensureRecord("CLUB", t.destinationClubId, operation.cloudLeagueId)
                common + mapOf(
                    "playerName" to t.playerName, "originClubCloudId" to origin.cloudId,
                    "destinationClubCloudId" to dest.cloudId, "valueCr" to t.valueCr,
                    "type" to t.type, "swapId" to t.swapId, "seasonId" to t.seasonId,
                    "note" to t.note, "createdAt" to t.createdAt,
                )
            }
            "PLAYER" -> playerDao.findById(operation.localId)?.let { p ->
                val club = ensureRecord("CLUB", p.clubId, operation.cloudLeagueId)
                common + mapOf(
                    "clubCloudId" to club.cloudId, "name" to p.name, "position" to p.position,
                    "marketStatus" to p.marketStatus.name, "askingPriceCr" to p.askingPriceCr,
                    "skillImageUri" to p.skillImageUri, "notes" to p.notes,
                    "externalPlayerId" to p.externalPlayerId, "isActive" to p.isActive,
                    "attributesRaw" to p.attributesRaw, "overall" to p.overall,
                    "createdAt" to p.createdAt, "updatedAt" to p.updatedAt,
                )
            }
            "NEWS" -> newsDao.findById(operation.localId)?.let { n ->
                common + mapOf(
                    "title" to n.title, "body" to n.body, "category" to n.category,
                    "eventType" to n.eventType, "dedupKey" to n.dedupKey,
                    "imageUri" to n.imageUri, "authorUserId" to n.authorUserId,
                    "publishedAt" to n.publishedAt,
                )
            }
            "ARENA" -> database.arenaDao().findById(operation.localId)?.let { d ->
                common + mapOf(
                    "leagueId" to d.leagueId, "clubAId" to d.clubAId, "clubBId" to d.clubBId,
                    "stakeCr" to d.stakeCr, "status" to d.status, "resultType" to d.resultType,
                    "note" to d.note, "createdByUserId" to d.createdByUserId,
                    "createdAt" to d.createdAt, "resolvedAt" to d.resolvedAt,
                )
            }
            "GOAL_EVENT" -> goalEventDao.findById(operation.localId)?.let { g ->
                val match = ensureRecord("MATCH", g.matchId, operation.cloudLeagueId)
                val player = ensureRecord("PLAYER", g.playerId, operation.cloudLeagueId)
                val club = ensureRecord("CLUB", g.scoringClubId, operation.cloudLeagueId)
                common + mapOf(
                    "matchCloudId" to match.cloudId, "playerCloudId" to player.cloudId,
                    "scoringClubCloudId" to club.cloudId, "isOwnGoal" to g.isOwnGoal,
                    "createdAt" to g.createdAt
                )
            }
            "AUCTION_LOT" -> auctionDao.findLotById(operation.localId)?.let { l ->
                common + mapOf(
                    "name" to l.name, "startAt" to l.startAt, "endAt" to l.endAt,
                    "status" to l.status, "closedAt" to l.closedAt, "createdByUserId" to l.createdByUserId,
                    "createdAt" to l.createdAt
                )
            }
            "AUCTION_ITEM" -> auctionDao.findItemById(operation.localId)?.let { i ->
                val lot = ensureRecord("AUCTION_LOT", i.lotId, operation.cloudLeagueId)
                val player = ensureRecord("PLAYER", i.playerId, operation.cloudLeagueId)
                val club = i.leadingClubId?.let { ensureRecord("CLUB", it, operation.cloudLeagueId) }
                common + mapOf(
                    "lotCloudId" to lot.cloudId, "playerCloudId" to player.cloudId,
                    "startingPriceCr" to i.startingPriceCr, "currentBidCr" to i.currentBidCr,
                    "leadingClubCloudId" to club?.cloudId, "status" to i.status, "createdAt" to i.createdAt
                )
            }
            "AUCTION_BID" -> auctionDao.findBidById(operation.localId)?.let { b ->
                val item = ensureRecord("AUCTION_ITEM", b.itemId, operation.cloudLeagueId)
                val club = ensureRecord("CLUB", b.clubId, operation.cloudLeagueId)
                common + mapOf(
                    "itemCloudId" to item.cloudId, "clubCloudId" to club.cloudId,
                    "amountCr" to b.amountCr, "bidByUserId" to b.bidByUserId,
                    "status" to b.status, "createdAt" to b.createdAt
                )
            }
            else -> null
        }
    }

    private suspend fun entityUpdatedAt(type: String, id: Long): Long = when (type) {
        "COMPETITION" -> competitionDao.findById(id)?.updatedAt
        "CLUB" -> clubDao.findById(id)?.updatedAt
        "USER" -> userDao.findById(id)?.updatedAt
        "SEASON" -> seasonDao.findById(id)?.updatedAt
        "PARTICIPANT" -> participantDao.findById(id)?.createdAt
        "ROUND" -> roundDao.findById(id)?.updatedAt
        "MATCH" -> matchDao.findById(id)?.updatedAt
        "STANDING" -> findStanding(id)?.updatedAt
        "FINANCE" -> financialDao.findById(id)?.createdAt
        "TRANSFER" -> transferDao.findById(id)?.createdAt
        "PLAYER" -> playerDao.findById(id)?.updatedAt
        "NEWS" -> newsDao.findById(id)?.publishedAt
        "ARENA" -> database.arenaDao().findById(id)?.createdAt
        "GOAL_EVENT" -> goalEventDao.findById(id)?.createdAt
        "AUCTION_LOT" -> auctionDao.findLotById(id)?.createdAt
        "AUCTION_ITEM" -> auctionDao.findItemById(id)?.createdAt
        "AUCTION_BID" -> auctionDao.findBidById(id)?.createdAt
        else -> null
    } ?: 0L

    private suspend fun refreshListeners() {
        if (authenticatedProfileUid == null || auth.currentUser?.uid != authenticatedProfileUid) {
            listeners.values.flatten().forEach(ListenerRegistration::remove)
            listeners.clear()
            return
        }
        val onlineLeagues = latestOnlineLeagueIds().filterTo(mutableSetOf(), ::syncEnabled)
        listeners.keys.filterNot(onlineLeagues::contains).forEach { id -> listeners.remove(id)?.forEach(ListenerRegistration::remove) }
        onlineLeagues.filterNot(listeners::containsKey).forEach { leagueId ->
            listeners[leagueId] = TYPES.map { type ->
                firestore.collection("leagues").document(leagueId).collection(collectionFor(type))
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            syncLog( "ERRO NO LISTENER — tipo=$type liga=$leagueId erro=${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshot?.metadata?.isFromCache == true || !syncEnabled(leagueId)) return@addSnapshotListener
                        
                        syncLog( "SNAPSHOT recebido — tipo=$type liga=$leagueId totalDocumentos=${snapshot?.documentChanges?.size ?: 0} isFromCache=${snapshot?.metadata?.isFromCache}")
                        snapshot?.documentChanges?.forEach { change ->
                            if (!change.document.metadata.hasPendingWrites() && !change.document.metadata.isFromCache) {
                                scope.launch { applyRemote(leagueId, type, change.document.id, change.document.data) }
                            } else {
                                syncLog( "Documento IGNORADO (cache/pendingWrites) — tipo=$type id=${change.document.id}")
                            }
                        }
                    }
            }
        }
    }

    private suspend fun latestOnlineLeagueIds(): Set<String> =
        latestCandidates.mapTo(mutableSetOf()) { it.cloudLeagueId }.also { ids ->
            // A direct query makes listeners work even when a joined league has no local sports rows yet.
            database.query("SELECT cloudLeagueId FROM leagues WHERE isOnline = 1 AND cloudLeagueId IS NOT NULL", emptyArray<Any?>()).use { cursor ->
                while (cursor.moveToNext()) ids += cursor.getString(0)
            }
        }

    private suspend fun applyRemote(leagueId: String, type: String, cloudId: String, data: Map<String, Any>, retry: Int = 0) {
        if (!syncEnabled(leagueId)) return
        
        // Protocolo: Versão de Esquema (Se presente, deve bater; se ausente, aceitamos como inicial)
        val remoteSchema = (data["schemaVersion"] as? Number)?.toInt() ?: SCHEMA_VERSION
        if (remoteSchema != SCHEMA_VERSION) return
        
        // Protocolo: ID de Nuvem (Se presente, deve bater)
        val remoteCloudId = data["cloudId"] as? String
        if (remoteCloudId != null && remoteCloudId != cloudId) return

        // Protocolo: Revisão (Permite 0 para carga inicial/dados simples)
        val revision = (data["revision"] as? Number)?.toLong() ?: 0L
        
        if (type == "ROUND" && (data.int("number") < 1 || (data.string("stage") == "KNOCKOUT" && data.string("stageLabel").isBlank()))) return
        mutationMutex.withLock {
            try {
                database.withTransaction {
                    if (!syncEnabled(leagueId)) return@withTransaction
                    val localLeague = leagueDao.findByCloudId(leagueId) ?: throw DependencyPendingException()
                    val existingRecord = syncDao.findRecordByCloudId(leagueId, type, cloudId)
                    
                    // Se já temos o dado localmente, só atualiza se a revisão for maior ou se não tiver revisão (override)
                    if (existingRecord != null) {
                        if (revision > 0 && revision <= existingRecord.revision) return@withTransaction
                        if (existingRecord.status == "CONFLICT" || syncDao.findPending(type, existingRecord.localId) != null) {
                            return@withTransaction
                        }
                    }

                    if (type == "MATCH" && existingRecord != null) {
                        val localMatch = matchDao.findById(existingRecord.localId)
                        if (localMatch?.status == MatchStatus.FINISHED && dStatus(data) != MatchStatus.FINISHED) return@withTransaction
                    }
                    val localId = when (type) {
                        "COMPETITION" -> applyCompetition(localLeague.id, existingRecord?.localId, data)
                        "CLUB" -> applyClub(localLeague.id, existingRecord?.localId, data)
                        "USER" -> applyUser(data)
                        "MEMBER" -> applyMember(data)
                        "SEASON" -> applySeason(leagueId, existingRecord?.localId, data)
                        "PARTICIPANT" -> applyParticipant(localLeague.id, leagueId, existingRecord?.localId, data)
                        "ROUND" -> applyRound(leagueId, existingRecord?.localId, data)
                        "MATCH" -> applyMatch(localLeague.id, leagueId, existingRecord?.localId, data)
                        "STANDING" -> applyStanding(localLeague.id, leagueId, existingRecord?.localId, data)
                        "FINANCE" -> applyFinance(leagueId, existingRecord?.localId, data)
                        "TRANSFER" -> applyTransfer(localLeague.id, leagueId, existingRecord?.localId, data)
                        "PLAYER" -> applyPlayer(localLeague.id, leagueId, existingRecord?.localId, data)
                        "NEWS" -> applyNews(localLeague.id, existingRecord?.localId, data)
                        "ARENA" -> applyArena(localLeague.id, existingRecord?.localId, data)
                        "GOAL_EVENT" -> applyGoalEvent(localLeague.id, leagueId, existingRecord?.localId, data)
                        "AUCTION_LOT" -> applyAuctionLot(localLeague.id, leagueId, existingRecord?.localId, data)
                        "AUCTION_ITEM" -> applyAuctionItem(localLeague.id, leagueId, existingRecord?.localId, data)
                        "AUCTION_BID" -> applyAuctionBid(localLeague.id, leagueId, existingRecord?.localId, data)
                        else -> return@withTransaction
                    }
                    val revision = (data["revision"] as? Number)?.toLong() ?: 0
                    val localUpdatedAt = entityUpdatedAt(type, localId)
                    val updated = OnlineSyncRecordEntity(
                        entityType = type, localId = localId, cloudLeagueId = leagueId, cloudId = cloudId,
                        revision = revision, lastLocalUpdatedAt = localUpdatedAt,
                        lastRemoteUpdatedAt = System.currentTimeMillis(), status = "SYNCED",
                    )
                    if (syncDao.findRecord(type, localId) == null) syncDao.insertRecord(updated) else syncDao.updateRecord(updated)
                }
            } catch (_: DependencyPendingException) {
                // Firestore subcollection listeners are independent and can deliver a child before its parent.
                // Instalação limpa: muitos registros chegam juntos, a dependência pode demorar mais para resolver.
                if (retry < 40) {
                    val backoff = (200L * (retry + 1)).coerceAtMost(2000L)
                    syncLog( "Dependência pendente para $type/$cloudId. Tentando novamente em ${backoff}ms... (retry $retry)")
                    scope.launch {
                        delay(backoff)
                        refreshSingleDocument(leagueId, type, cloudId, retry + 1)
                    }
                } else {
                    syncLog( "Dependência definitivamente ausente para $type/$cloudId após $retry tentativas. Registro descartado.")
                }
            } catch (error: Exception) {
                syncLog( "FALHA CRÍTICA ao aplicar $type/$cloudId na liga $leagueId — erro=${error.message}", error)
            }
        }
    }

    private suspend fun refreshSingleDocument(leagueId: String, type: String, cloudId: String, retry: Int) {
        if (!syncEnabled(leagueId)) return
        val snapshot = firestore.collection("leagues").document(leagueId).collection(collectionFor(type)).document(cloudId).get().await()
        if (snapshot.exists()) applyRemote(leagueId, type, cloudId, snapshot.data.orEmpty(), retry)
    }

    private suspend fun applyCompetition(leagueId: Long, existingId: Long?, d: Map<String, Any>): Long {
        val current = existingId?.let { competitionDao.findById(it) }
            ?: competitionDao.findByLeagueAndName(leagueId, d.string("name"))
        val entity = CompetitionEntity(
            id = current?.id ?: 0, leagueId = leagueId, name = d.string("name"),
            type = enumValue(d.string("type"), CompetitionType.LEAGUE),
            format = enumValue(d.string("format"), CompetitionFormat.SINGLE_ROUND),
            status = enumValue(d.string("status"), CompetitionStatus.DRAFT),
            pointsForWin = d.int("pointsForWin", 3), pointsForDraw = d.int("pointsForDraw", 1),
            pointsForLoss = d.int("pointsForLoss", 0), groupCount = d.nullableInt("groupCount"),
            qualifiedPerGroup = d.nullableInt("qualifiedPerGroup"), knockoutLegs = d.int("knockoutLegs", 1),
            createdAt = d.long("createdAt"), updatedAt = d.long("clientUpdatedAt"),
        )
        return if (current == null) competitionDao.insert(entity) else current.id.also { competitionDao.update(entity) }
    }

    private suspend fun applyClub(leagueId: Long, existingId: Long?, d: Map<String, Any>): Long {
        val current = existingId?.let { clubDao.findById(it) }
            ?: clubDao.findByLeagueAndName(leagueId, d.string("name"))

        val presidentUid = d["presidentFirebaseUid"] as? String
        val presidentUserId = presidentUid?.let { uid ->
            // Prioridade: buscar pelo UID do Firebase
            userDao.findByFirebaseUid(uid)?.id ?: throw DependencyPendingException()
        }

        val entity = ClubEntity(
            id = current?.id ?: 0,
            leagueId = leagueId,
            name = d.string("name"),
            crestUri = d["crestUri"] as? String,
            presidentUserId = presidentUserId,
            isBank = d["isBank"] as? Boolean ?: false,
            isActive = d["isActive"] as? Boolean ?: true,
            createdAt = d.long("createdAt"),
            updatedAt = d.long("clientUpdatedAt"),
        )
        return if (current == null) clubDao.insert(entity) else current.id.also { clubDao.update(entity) }
    }

    private suspend fun applyUser(d: Map<String, Any>): Long {
        val uid = d.string("firebaseUid")
        if (uid.isBlank()) return -1L
        val current = userDao.findByFirebaseUid(uid)
        val entity = com.example.legacymasterliga.core.database.entity.UserEntity(
            id = current?.id ?: 0,
            firebaseUid = uid,
            username = d.string("username"),
            displayName = d.string("displayName"),
            role = enumValue(d.string("role"), com.example.legacymasterliga.core.model.UserRole.PRESIDENT),
            status = enumValue(d.string("status"), com.example.legacymasterliga.core.model.AccountStatus.ACTIVE),
            passwordHash = "FIREBASE_AUTH",
            passwordSalt = "FIREBASE_AUTH",
            updatedAt = d.long("clientUpdatedAt")
        )
        return if (current == null) userDao.insert(entity) else current.id.also { userDao.update(entity) }
    }

    private suspend fun applyMember(d: Map<String, Any>): Long {
        // No Firestore, 'members' é uma subcoleção da liga com dados espelhados do perfil
        return applyUser(d)
    }

    private suspend fun applySeason(leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val competitionId = syncDao.findRecordByCloudId(leagueId, "COMPETITION", d.string("competitionCloudId"))?.localId
            ?: throw DependencyPendingException()
        val current = existingId?.let { seasonDao.findById(it) }
            ?: seasonDao.findByCompetitionAndNumber(competitionId, d.int("number"))
        val entity = SeasonEntity(
            id = current?.id ?: 0, competitionId = competitionId, number = d.int("number"), name = d.string("name"),
            status = enumValue(d.string("status"), SeasonStatus.DRAFT), startedAt = d.nullableLong("startedAt"),
            finishedAt = d.nullableLong("finishedAt"), createdAt = d.long("createdAt"), updatedAt = d.long("clientUpdatedAt"),
        )
        return if (current == null) seasonDao.insert(entity) else current.id.also { seasonDao.update(entity) }
    }

    private suspend fun applyRound(leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val seasonId = syncDao.findRecordByCloudId(leagueId, "SEASON", d.string("seasonCloudId"))?.localId
            ?: throw DependencyPendingException()
        val current = existingId?.let { roundDao.findById(it) }
            ?: roundDao.findBySeasonAndNumber(seasonId, d.int("number"))
        val entity = RoundEntity(
            id = current?.id ?: 0, seasonId = seasonId, number = d.int("number"), name = d.string("name"),
            stage = d.string("stage", "REGULAR"), stageLabel = d["stageLabel"] as? String,
            groupIndex = d.nullableInt("groupIndex"), status = enumValue(d.string("status"), RoundStatus.SCHEDULED),
            createdAt = d.long("createdAt"), updatedAt = d.long("clientUpdatedAt"),
        )
        return if (current == null) roundDao.insertIfAbsent(entity).let { if (it == -1L) roundDao.findBySeasonAndNumber(seasonId, entity.number)!!.id else it }
        else current.id.also { roundDao.update(entity) }
    }

    private suspend fun applyParticipant(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val seasonId = syncDao.findRecordByCloudId(leagueId, "SEASON", d.string("seasonCloudId"))?.localId
            ?: throw DependencyPendingException()
        val club = findOrCreateClub(leagueLocalId, d.string("clubName"))
        val current = existingId?.let { participantDao.findById(it) } ?: participantDao.find(seasonId, club.id)
        val entity = CompetitionParticipantEntity(
            id = current?.id ?: 0, seasonId = seasonId, clubId = club.id, seed = d.nullableInt("seed"),
            isActive = d["isActive"] as? Boolean ?: true, createdAt = d.long("createdAt"),
        )
        return if (current == null) participantDao.insert(entity) else current.id.also { participantDao.update(entity) }
    }

    private suspend fun applyMatch(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val seasonId = syncDao.findRecordByCloudId(leagueId, "SEASON", d.string("seasonCloudId"))?.localId ?: throw DependencyPendingException()
        val roundId = syncDao.findRecordByCloudId(leagueId, "ROUND", d.string("roundCloudId"))?.localId ?: throw DependencyPendingException()
        val home = findOrCreateClub(leagueLocalId, d.string("homeClubName"))
        val away = findOrCreateClub(leagueLocalId, d.string("awayClubName"))
        val current = existingId?.let { matchDao.findById(it) }
            ?: matchDao.findByPairingAndLeg(seasonId, d.string("pairingKey"), d.int("leg", 1))
        val winnerKey = d["winnerClubKey"] as? String
        val winner = when (winnerKey) { clubKey(home.name) -> home.id; clubKey(away.name) -> away.id; else -> null }
        val entity = MatchEntity(
            id = current?.id ?: 0, seasonId = seasonId, roundId = roundId, homeClubId = home.id, awayClubId = away.id,
            pairingKey = d.string("pairingKey"), leg = d.int("leg", 1), stage = d.string("stage", "REGULAR"),
            groupIndex = d.nullableInt("groupIndex"), bracketPosition = d.nullableInt("bracketPosition"),
            penaltiesHome = d.nullableInt("penaltiesHome"), penaltiesAway = d.nullableInt("penaltiesAway"),
            winnerClubId = winner, advanceReason = d["advanceReason"] as? String, homeScore = d.nullableInt("homeScore"),
            awayScore = d.nullableInt("awayScore"),
            homeYellowCards = d.int("homeYellowCards"), homeRedCards = d.int("homeRedCards"),
            awayYellowCards = d.int("awayYellowCards"), awayRedCards = d.int("awayRedCards"),
            status = enumValue(d.string("status"), MatchStatus.SCHEDULED),
            scheduledAt = d.nullableLong("scheduledAt"), createdAt = d.long("createdAt"), updatedAt = d.long("clientUpdatedAt"),
        )
        return if (current == null) matchDao.insertAll(listOf(entity)).single() else current.id.also { matchDao.update(entity) }
    }

    private suspend fun applyStanding(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val seasonId = syncDao.findRecordByCloudId(leagueId, "SEASON", d.string("seasonCloudId"))?.localId ?: throw DependencyPendingException()
        val club = findOrCreateClub(leagueLocalId, d.string("clubName"))
        val current = existingId?.let { findStanding(it) } ?: standingDao.findBySeasonAndClub(seasonId, club.id)
        val entity = StandingEntity(
            id = current?.id ?: 0, seasonId = seasonId, clubId = club.id, groupIndex = d.nullableInt("groupIndex"),
            played = d.int("played"), wins = d.int("wins"), draws = d.int("draws"), losses = d.int("losses"),
            goalsFor = d.int("goalsFor"), goalsAgainst = d.int("goalsAgainst"), goalDifference = d.int("goalDifference"),
            points = d.int("points"), updatedAt = d.long("clientUpdatedAt"),
        )
        standingDao.upsertAll(listOf(entity))
        return current?.id ?: standingDao.findBySeasonAndClub(seasonId, club.id)!!.id
    }

    private suspend fun applyFinance(leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val clubId = syncDao.findRecordByCloudId(leagueId, "CLUB", d.string("clubCloudId"))?.localId
            ?: throw DependencyPendingException()
        val counterpartyId = d.string("counterpartyClubCloudId").takeIf { it.isNotBlank() }?.let {
            syncDao.findRecordByCloudId(leagueId, "CLUB", it)?.localId ?: throw DependencyPendingException()
        }
        val transferId = d.string("transferCloudId").takeIf { it.isNotBlank() }?.let {
            syncDao.findRecordByCloudId(leagueId, "TRANSFER", it)?.localId ?: throw DependencyPendingException()
        }

        val current = existingId?.let { financialDao.findById(it) }
        val entity = FinancialTransactionEntity(
            id = current?.id ?: 0,
            clubId = clubId,
            amountCr = d.long("amountCr"),
            description = d.string("description"),
            type = d.string("type", "ADJUSTMENT"),
            counterpartyClubId = counterpartyId,
            transferId = transferId,
            createdAt = d.long("createdAt")
        )
        return if (current == null) financialDao.insert(entity) else current.id.also { financialDao.update(entity) }
    }

    private suspend fun applyTransfer(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val originId = syncDao.findRecordByCloudId(leagueId, "CLUB", d.string("originClubCloudId"))?.localId
            ?: throw DependencyPendingException()
        val destId = syncDao.findRecordByCloudId(leagueId, "CLUB", d.string("destinationClubCloudId"))?.localId
            ?: throw DependencyPendingException()

        val current = existingId?.let { transferDao.findById(it) }
        val entity = TransferEntity(
            id = current?.id ?: 0,
            leagueId = leagueLocalId,
            playerName = d.string("playerName"),
            originClubId = originId,
            destinationClubId = destId,
            valueCr = d.long("valueCr"),
            type = d.string("type", "TRANSFER"),
            swapId = d.string("swapId"),
            seasonId = d.nullableLong("seasonId"),
            note = d.string("note"),
            createdAt = d.long("createdAt")
        )
        return if (current == null) transferDao.insert(entity) else current.id.also { transferDao.update(entity) }
    }

    private suspend fun applyPlayer(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val clubRecord = syncDao.findRecordByCloudId(leagueId, "CLUB", d.string("clubCloudId"))
            ?: throw DependencyPendingException()
        val clubId = clubRecord.localId
        
        val name = d.string("name")
        val externalId = d.string("externalPlayerId").trim().takeIf { it.isNotBlank() }
        
        // Tenta encontrar o jogador localmente: por ID de sincronização OU por Nome+Clube (recuperação)
        val current = existingId?.let { playerDao.findById(it) }
            ?: playerDao.findByNameAndClub(clubId, name)
            
        syncLog( "Sincronizando jogador: $name (Clube Local: $clubId, ID Externo: $externalId, Recuperado: ${current != null})")

        val entity = com.example.legacymasterliga.core.database.entity.PlayerEntity(
            id = current?.id ?: 0,
            leagueId = leagueLocalId,
            clubId = clubId,
            name = name,
            position = d.string("position"),
            marketStatus = enumValue(d.string("marketStatus"), com.example.legacymasterliga.core.model.MarketStatus.NOT_LISTED),
            askingPriceCr = d.nullableLong("askingPriceCr"),
            skillImageUri = d.nullableString("skillImageUri"),
            notes = d.string("notes"),
            externalPlayerId = externalId,
            attributesRaw = d.nullableString("attributesRaw"),
            overall = (d["overall"] as? Number)?.toInt(),
            isActive = true, // Forçamos Ativo na sincronização para garantir visibilidade
            createdAt = d.long("createdAt"),
            updatedAt = d.long("clientUpdatedAt")
        )
        
        val finalId = playerDao.upsert(entity)
        val resultId = when {
            finalId > 0 -> finalId
            current != null -> current.id
            else -> playerDao.findByNameAndClub(clubId, name)?.id ?: throw IllegalStateException("Falha ao recuperar ID do jogador $name")
        }
        
        syncLog( "Jogador $name salvo com sucesso. ID Local: $resultId")
        return resultId
    }

    private suspend fun applyNews(leagueLocalId: Long, existingId: Long?, d: Map<String, Any>): Long {
        val current = existingId?.let { newsDao.findById(it) }
        val entity = com.example.legacymasterliga.core.database.entity.NewsEntity(
            id = current?.id ?: 0,
            leagueId = leagueLocalId,
            title = d.string("title"), body = d.string("body"),
            category = d.string("category"), eventType = d.string("eventType"),
            dedupKey = d.string("dedupKey"), imageUri = d.nullableString("imageUri"),
            authorUserId = d.nullableLong("authorUserId"),
            publishedAt = d.long("publishedAt"),
        )
        return if (current == null) newsDao.upsert(entity) else current.id.also { newsDao.upsert(entity) }
    }

    private suspend fun applyArena(leagueLocalId: Long, existingId: Long?, d: Map<String, Any>): Long {
        val arenaDao = database.arenaDao()
        val current = existingId?.let { arenaDao.findById(it) }
        val entity = com.example.legacymasterliga.core.database.entity.ArenaDuelEntity(
            id = current?.id ?: 0,
            leagueId = leagueLocalId,
            clubAId = d.long("clubAId"),
            clubBId = d.long("clubBId"),
            stakeCr = d.long("stakeCr"),
            status = d.string("status"),
            resultType = d.string("resultType").takeIf { it.isNotBlank() },
            note = d.string("note").takeIf { it.isNotBlank() },
            createdByUserId = d.nullableLong("createdByUserId"),
            createdAt = d.long("createdAt"),
            resolvedAt = d.nullableLong("resolvedAt")
        )
        return if (current == null) arenaDao.insert(entity) else current.id.also { 
            arenaDao.resolve(entity.id, entity.status, entity.resultType ?: "", entity.resolvedAt ?: 0L)
        }
    }

    private suspend fun applyGoalEvent(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val matchId = syncDao.findRecordByCloudId(leagueId, "MATCH", d.string("matchCloudId"))?.localId ?: throw DependencyPendingException()
        val playerId = syncDao.findRecordByCloudId(leagueId, "PLAYER", d.string("playerCloudId"))?.localId ?: throw DependencyPendingException()
        val clubId = syncDao.findRecordByCloudId(leagueId, "CLUB", d.string("scoringClubCloudId"))?.localId ?: throw DependencyPendingException()
        
        val entity = com.example.legacymasterliga.core.database.entity.GoalEventEntity(
            id = existingId ?: 0,
            matchId = matchId,
            playerId = playerId,
            scoringClubId = clubId,
            isOwnGoal = d["isOwnGoal"] as? Boolean ?: false,
            createdAt = d.long("createdAt")
        )
        return goalEventDao.insert(entity)
    }

    private suspend fun applyAuctionLot(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val entity = com.example.legacymasterliga.core.database.entity.AuctionLotEntity(
            id = existingId ?: 0,
            leagueId = leagueLocalId,
            name = d.string("name"),
            startAt = d.long("startAt"),
            endAt = d.long("endAt"),
            status = d.string("status"),
            closedAt = d.nullableLong("closedAt"),
            createdByUserId = d.nullableLong("createdByUserId"),
            createdAt = d.long("createdAt")
        )
        return if (existingId == null) auctionDao.insertLot(entity) else entity.id.also { auctionDao.updateLot(entity) }
    }

    private suspend fun applyAuctionItem(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val lotId = syncDao.findRecordByCloudId(leagueId, "AUCTION_LOT", d.string("lotCloudId"))?.localId ?: throw DependencyPendingException()
        val playerId = syncDao.findRecordByCloudId(leagueId, "PLAYER", d.string("playerCloudId"))?.localId ?: throw DependencyPendingException()
        val leadingClubId = d.string("leadingClubCloudId").takeIf { it.isNotBlank() }?.let {
            syncDao.findRecordByCloudId(leagueId, "CLUB", it)?.localId ?: throw DependencyPendingException()
        }
        
        val entity = com.example.legacymasterliga.core.database.entity.AuctionItemEntity(
            id = existingId ?: 0,
            lotId = lotId,
            playerId = playerId,
            startingPriceCr = d.long("startingPriceCr"),
            currentBidCr = d.nullableLong("currentBidCr"),
            leadingClubId = leadingClubId,
            status = d.string("status"),
            createdAt = d.long("createdAt")
        )
        return if (existingId == null) auctionDao.insertItem(entity) else entity.id.also { auctionDao.updateItem(entity) }
    }

    private suspend fun applyAuctionBid(leagueLocalId: Long, leagueId: String, existingId: Long?, d: Map<String, Any>): Long {
        val itemId = syncDao.findRecordByCloudId(leagueId, "AUCTION_ITEM", d.string("itemCloudId"))?.localId ?: throw DependencyPendingException()
        val clubId = syncDao.findRecordByCloudId(leagueId, "CLUB", d.string("clubCloudId"))?.localId ?: throw DependencyPendingException()
        
        val entity = com.example.legacymasterliga.core.database.entity.AuctionBidEntity(
            id = existingId ?: 0,
            itemId = itemId,
            clubId = clubId,
            amountCr = d.long("amountCr"),
            bidByUserId = d.nullableLong("bidByUserId"),
            status = d.string("status"),
            createdAt = d.long("createdAt")
        )
        return if (existingId == null) auctionDao.insertBid(entity) else entity.id.also { auctionDao.updateBid(entity) }
    }

    private suspend fun recalculateStandings(seasonId: Long) {
        val season = seasonDao.findById(seasonId) ?: return
        val competition = competitionDao.findById(season.competitionId) ?: return
        val participants = participantDao.findActiveBySeason(seasonId)
        val allMatches = matchDao.findBySeason(seasonId)
        
        val accumulators = mutableMapOf<Pair<Long, Int?>, MutableSyncStanding>()
        val clubGroupMap = allMatches.associate { it.homeClubId to it.groupIndex } + 
                          allMatches.associate { it.awayClubId to it.groupIndex }

        participants.forEach { p ->
            val gIdx = if (competition.type == CompetitionType.LEAGUE) null else clubGroupMap[p.clubId]
            accumulators[Pair(p.clubId, gIdx)] = MutableSyncStanding()
        }

        allMatches.filter { it.status == MatchStatus.FINISHED }.forEach { match ->
            if (match.stage == "KNOCKOUT" || match.stage == "PLACEHOLDER") return@forEach

            val hScore = match.homeScore ?: 0
            val aScore = match.awayScore ?: 0
            val homeKey = if (competition.type == CompetitionType.LEAGUE) Pair(match.homeClubId, null) else Pair(match.homeClubId, match.groupIndex)
            val awayKey = if (competition.type == CompetitionType.LEAGUE) Pair(match.awayClubId, null) else Pair(match.awayClubId, match.groupIndex)

            val home = accumulators[homeKey]
            val away = accumulators[awayKey]
            if (home == null || away == null) return@forEach

            home.played++
            away.played++
            home.goalsFor += hScore
            home.goalsAgainst += aScore
            away.goalsFor += aScore
            away.goalsAgainst += hScore

            when {
                hScore > aScore -> {
                    home.wins++
                    away.losses++
                    home.points += competition.pointsForWin
                    away.points += competition.pointsForLoss
                }
                hScore < aScore -> {
                    away.wins++
                    home.losses++
                    away.points += competition.pointsForWin
                    home.points += competition.pointsForLoss
                }
                else -> {
                    home.draws++
                    away.draws++
                    home.points += competition.pointsForDraw
                    away.points += competition.pointsForDraw
                }
            }
        }

        val entities = accumulators.map { (key, value) ->
            StandingEntity(
                seasonId = seasonId, clubId = key.first, groupIndex = key.second,
                played = value.played, wins = value.wins, draws = value.draws,
                losses = value.losses, goalsFor = value.goalsFor, goalsAgainst = value.goalsAgainst,
                goalDifference = value.goalsFor - value.goalsAgainst, points = value.points,
                updatedAt = System.currentTimeMillis()
            )
        }
        standingDao.upsertAll(entities)
    }

    private data class MutableSyncStanding(
        var played: Int = 0, var wins: Int = 0, var draws: Int = 0, var losses: Int = 0,
        var goalsFor: Int = 0, var goalsAgainst: Int = 0, var points: Int = 0,
    )

    private suspend fun findOrCreateClub(leagueId: Long, name: String): ClubEntity {
        clubDao.findByLeagueAndName(leagueId, name)?.let { return it }
        val id = clubDao.insert(ClubEntity(leagueId = leagueId, name = name))
        return clubDao.findById(id)!!
    }

    private suspend fun findStanding(id: Long): StandingEntity? = standingDao.findById(id)

    private fun collectionFor(type: String): String = when (type.trim().uppercase()) {
        "COMPETITION" -> "competitions"
        "CLUB" -> "clubs"
        "USER", "MEMBER" -> "members"
        "SEASON" -> "seasons"
        "PARTICIPANT" -> "participants"
        "ROUND" -> "rounds"
        "MATCH" -> "matches"
        "STANDING" -> "standings"
        "FINANCE" -> "financial_transactions"
        "TRANSFER" -> "transfers"
        "PLAYER" -> "players"
        "NEWS" -> "news"
        "ARENA" -> "arena_duels"
        "GOAL_EVENT" -> "goal_events"
        "AUCTION_LOT" -> "auction_lots"
        "AUCTION_ITEM" -> "auction_items"
        "AUCTION_BID" -> "auction_bids"
        else -> {
            syncLog( "Falha crítica: Tipo '$type' não mapeado em collectionFor")
            error("Tipo não sincronizável: $type")
        }
    }

    private fun clubKey(name: String): String = name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private inline fun <reified T : Enum<T>> enumValue(value: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback

    private fun Map<String, Any>.string(key: String, fallback: String = ""): String = this[key] as? String ?: fallback
    private fun Map<String, Any>.nullableString(key: String): String? = (this[key] as? String)?.ifBlank { null }
    private fun Map<String, Any>.long(key: String, fallback: Long = 0): Long = (this[key] as? Number)?.toLong() ?: fallback
    private fun Map<String, Any>.int(key: String, fallback: Int = 0): Int = (this[key] as? Number)?.toInt() ?: fallback
    private fun Map<String, Any>.nullableLong(key: String): Long? = (this[key] as? Number)?.toLong()
    private fun Map<String, Any>.nullableInt(key: String): Int? = (this[key] as? Number)?.toInt()
    private fun dStatus(data: Map<String, Any>): MatchStatus = enumValue(data.string("status"), MatchStatus.SCHEDULED)

    private class DependencyPendingException : IllegalStateException()

    private sealed interface TransactionOutcome {
        data class Uploaded(val revision: Long) : TransactionOutcome
        data class RemoteWins(val data: Map<String, Any>) : TransactionOutcome
        data class ScoreConflict(val remotePayload: String) : TransactionOutcome
    }

    private companion object {
        const val TAG = "OnlineSportsSync"
        const val SCHEMA_VERSION = 1
        const val ACTIVE = "ACTIVE"
        val TYPES = listOf("USER", "MEMBER", "COMPETITION", "CLUB", "SEASON", "PARTICIPANT", "ROUND", "MATCH", "STANDING", "FINANCE", "TRANSFER", "PLAYER", "NEWS", "ARENA", "GOAL_EVENT", "AUCTION_LOT", "AUCTION_ITEM", "AUCTION_BID")
    }
}
// Build Trigger: v2 - Ensuring ARENA sync is compiled correctly
