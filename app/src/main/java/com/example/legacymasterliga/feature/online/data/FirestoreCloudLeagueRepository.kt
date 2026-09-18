package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.feature.online.domain.CloudInvite
import com.example.legacymasterliga.feature.online.domain.CloudLeague
import com.example.legacymasterliga.feature.online.domain.CloudLeagueRepository
import com.example.legacymasterliga.feature.online.domain.CloudMember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreCloudLeagueRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val leagueDao: LeagueDao,
    private val userDao: UserDao,
    private val syncManager: com.example.legacymasterliga.feature.online.sync.OnlineSportsSyncManager,
) : CloudLeagueRepository {

    override suspend fun promoteToCloud(localLeagueId: Long): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Autenticação necessária.")
        val localLeague = leagueDao.findById(localLeagueId) ?: error("Liga local não encontrada.")
        val user = userDao.findByFirebaseUid(uid)
            ?: error("Seu usuário local precisa estar vinculado à conta online antes de promover a liga.")

        localLeague.cloudLeagueId?.let { return@runCatching it }

        val cloudId = UUID.randomUUID().toString()
        val cloudLeague = CloudLeague(
            cloudLeagueId = cloudId,
            name = localLeague.name,
            ownerUid = uid,
            status = "ACTIVE",
            cloudId = cloudId
        )
        val member = CloudMember(
            firebaseUid = uid,
            username = user.username,
            displayName = user.displayName,
            role = UserRole.ADMINISTRATOR,
            joinedAt = System.currentTimeMillis(),
            cloudId = uid
        )

        firestore.runTransaction { transaction ->
            val leagueRef = firestore.collection("leagues").document(cloudId)
            val profileRef = firestore.collection("user_profiles").document(uid)
            transaction.set(leagueRef, cloudLeague)
            transaction.set(leagueRef.collection("members").document(uid), member)
            
            // Garantia: Atualiza perfil com a liga recém-criada
            transaction.update(profileRef, "cloudLeagueId", cloudId)
            transaction.update(profileRef, "memberships", FieldValue.arrayUnion(cloudId))
        }.await()

        leagueDao.linkCloudId(localLeagueId, cloudId)
        syncManager.refreshImmediately()
        cloudId
    }

    override suspend fun generateInvite(cloudLeagueId: String, maxUses: Int): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Autenticação necessária.")
        require(maxUses in 1..100) { "O limite do convite deve ficar entre 1 e 100 usos." }
        
        val league = firestore.collection("leagues").document(cloudLeagueId).get().await()
            .toObject(CloudLeague::class.java) ?: error("Liga online não encontrada.")
        check(league.ownerUid == uid) { "Somente o administrador da liga pode gerar convites." }

        // Reuso: Busca convite ativo equivalente
        val existing = firestore.collection("invites")
            .whereEqualTo("leagueId", cloudLeagueId)
            .whereEqualTo("createdBy", uid)
            .whereEqualTo("status", "ACTIVE")
            .whereEqualTo("maxUses", maxUses)
            .get().await()
            .toObjects(CloudInvite::class.java)
            .firstOrNull { it.uses < it.maxUses && (it.expiresAt == null || it.expiresAt > System.currentTimeMillis()) }

        existing?.inviteCode ?: createUniqueInvite(cloudLeagueId, uid, maxUses)
    }

    override suspend fun joinByInvite(inviteCode: String): Result<String> = runCatching {
        require(inviteCode.length == 6 && inviteCode.all(Char::isDigit)) {
            "O código de convite deve possuir 6 dígitos numéricos."
        }
        val uid = auth.currentUser?.uid ?: error("Autenticação necessária.")
        val user = userDao.findByFirebaseUid(uid)
            ?: error("Vincule sua conta local antes de entrar em uma liga.")

        // The invite is read and consumed inside the same Firestore transaction.
        // Concurrent devices cannot both consume the last available use.
        val joinedLeague = firestore.runTransaction { transaction ->
            val inviteRef = firestore.collection("invites").document(inviteCode)
            val invite = transaction.get(inviteRef).toObject(CloudInvite::class.java)
                ?: error("Código de convite inválido ou expirado.")
            check(invite.status == "ACTIVE") { "Este convite foi desativado." }
            check(invite.expiresAt == null || invite.expiresAt > System.currentTimeMillis()) {
                "Este convite expirou."
            }

            val leagueRef = firestore.collection("leagues").document(invite.leagueId)
            val memberRef = leagueRef.collection("members").document(uid)
            val league = transaction.get(leagueRef).toObject(CloudLeague::class.java)
                ?: error("Liga online não encontrada.")
            val existingMember = transaction.get(memberRef)

            if (!existingMember.exists()) {
                check(invite.uses < invite.maxUses) { "Limite de usos deste convite atingido." }
                transaction.set(
                    memberRef,
                    CloudMember(
                        firebaseUid = uid,
                        username = user.username,
                        displayName = user.displayName,
                        role = UserRole.PRESIDENT,
                        joinedAt = System.currentTimeMillis(),
                        inviteCode = inviteCode,
                        cloudId = uid
                    ),
                )
                transaction.update(inviteRef, "uses", FieldValue.increment(1))
                
                // Salva o ID da liga no perfil para reconexão automática
                val profileRef = firestore.collection("user_profiles").document(uid)
                transaction.update(profileRef, "cloudLeagueId", invite.leagueId)
                
                // Adiciona à lista de memberships para descoberta garantida
                transaction.update(profileRef, "memberships", FieldValue.arrayUnion(invite.leagueId))
            } else {
                // Mesmo que já seja membro, garante que o perfil online aponte para a liga certa
                val profileRef = firestore.collection("user_profiles").document(uid)
                transaction.update(profileRef, "cloudLeagueId", invite.leagueId)
                transaction.update(profileRef, "memberships", FieldValue.arrayUnion(invite.leagueId))
            }
            league
        }.await()

        mirrorLeague(joinedLeague)
        syncManager.refreshImmediately()
        joinedLeague.cloudLeagueId
    }

    override fun observeLeague(cloudLeagueId: String): Flow<CloudLeague?> = callbackFlow {
        val listener = firestore.collection("leagues").document(cloudLeagueId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(CloudLeague::class.java))
            }
        awaitClose { listener.remove() }
    }

    override fun observeMembers(cloudLeagueId: String): Flow<List<CloudMember>> = callbackFlow {
        val listener = firestore.collection("leagues").document(cloudLeagueId).collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(CloudMember::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override fun observeActiveInvite(cloudLeagueId: String): Flow<CloudInvite?> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(null)
            return@callbackFlow
        }
        val listener = firestore.collection("invites")
            .whereEqualTo("leagueId", cloudLeagueId)
            .whereEqualTo("createdBy", uid)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val invite = snapshot?.toObjects(CloudInvite::class.java)
                    ?.firstOrNull { it.uses < it.maxUses && (it.expiresAt == null || it.expiresAt > System.currentTimeMillis()) }
                trySend(invite)
            }
        awaitClose { listener.remove() }
    }

    private suspend fun createUniqueInvite(cloudLeagueId: String, uid: String, maxUses: Int): String {
        repeat(10) {
            val code = (1..6).map { Random.nextInt(10) }.joinToString("")
            val invite = CloudInvite(
                inviteCode = code,
                leagueId = cloudLeagueId,
                createdBy = uid,
                maxUses = maxUses,
            )
            val created = runCatching {
                firestore.runTransaction { transaction ->
                    val ref = firestore.collection("invites").document(code)
                    check(!transaction.get(ref).exists()) { "COLLISION" }
                    transaction.set(ref, invite)
                }.await()
            }
            if (created.isSuccess) return code
        }
        error("Não foi possível gerar um código exclusivo. Tente novamente.")
    }

    private suspend fun mirrorLeague(cloudLeague: CloudLeague) {
        if (leagueDao.findByCloudId(cloudLeague.cloudLeagueId) != null) return

        val sameName = leagueDao.findByName(cloudLeague.name)
        if (sameName != null) {
            check(sameName.cloudLeagueId == null || sameName.cloudLeagueId == cloudLeague.cloudLeagueId) {
                "Já existe outra liga local com o nome ${cloudLeague.name}."
            }
            leagueDao.linkCloudId(sameName.id, cloudLeague.cloudLeagueId)
            return
        }

        leagueDao.insert(
            LeagueEntity(
                name = cloudLeague.name,
                cloudLeagueId = cloudLeague.cloudLeagueId,
                isOnline = true,
            ),
        )
    }
}
