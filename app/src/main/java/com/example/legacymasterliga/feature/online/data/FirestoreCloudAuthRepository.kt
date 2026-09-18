package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.database.dao.UserDao
import com.example.legacymasterliga.core.network.FirebaseProjectMismatchException
import com.example.legacymasterliga.core.network.OnlineProfileBootstrapException
import com.example.legacymasterliga.core.network.OnlineProfileIdentityMismatchException
import com.example.legacymasterliga.feature.online.domain.CloudAuthRepository
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

@Singleton
class FirestoreCloudAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
) : CloudAuthRepository {

    override suspend fun createOnlineAccount(email: String, password: String): Result<String> = runCatching {
        validateFirebaseProject()
        val result = withTimeout(AUTH_TIMEOUT_MS) {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
        }
        result.user?.uid ?: error("Falha ao obter identidade única.")
    }

    override suspend fun registerOnlineAccount(
        email: String,
        password: String,
        displayName: String,
        username: String,
        localUserId: Long,
    ): Result<String> = runCatching {
        validateFirebaseProject()
        userDao.findById(localUserId)
            ?: error("Sessão local inválida. Faça login novamente.")

        val uid = createOnlineAccount(email, password).getOrThrow()

        getOrCreateProfile(
            uid = uid,
            customDisplayName = displayName,
            customUsername = username,
            preferredLocalUserId = localUserId
        ).getOrThrow()
        userDao.linkFirebase(localUserId, uid)
        uid
    }

    override suspend fun signInOnline(email: String, password: String): Result<String> = runCatching {
        validateFirebaseProject()
        val result = withTimeout(AUTH_TIMEOUT_MS) {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
        }
        result.user?.uid ?: error("Falha ao obter UID.")
    }

    override suspend fun fetchProfile(uid: String): Result<CloudUserProfile?> = runCatching {
        validateFirebaseProject()
        val snapshot = withTimeout(PROFILE_TIMEOUT_MS) {
            firestore.collection(CloudUserProfileDocument.COLLECTION).document(uid).get().await()
        }
        snapshot.data?.let { CloudUserProfileDocument.decode(uid, it) }
    }

    override suspend fun getOrCreateProfile(
        uid: String,
        customDisplayName: String?,
        customUsername: String?,
        preferredLocalUserId: Long?,
    ): Result<CloudUserProfile> = runCatching {
        validateFirebaseProject()
        val firebaseUser = auth.currentUser
            ?: throw OnlineProfileIdentityMismatchException(uid, null)
        if (firebaseUser.uid != uid) {
            throw OnlineProfileIdentityMismatchException(uid, firebaseUser.uid)
        }

        val localUser = preferredLocalUserId
            ?.takeIf { it > 0L }
            ?.let { userDao.findById(it) }
            ?: userDao.findByFirebaseUid(uid)
        val candidate = CloudUserProfileFactory.create(
            uid = uid,
            email = firebaseUser.email,
            firebaseDisplayName = firebaseUser.displayName,
            localUser = localUser,
            customDisplayName = customDisplayName,
            customUsername = customUsername,
        )
        val reference = firestore.collection(CloudUserProfileDocument.COLLECTION).document(uid)

        val profile = try {
            withTimeout(PROFILE_TIMEOUT_MS) {
                val snapshot = reference.get().await()
                if (!snapshot.exists()) {
                    // Criação atômica inicial
                    reference.set(CloudUserProfileDocument.payload(candidate)).await()
                    candidate
                } else {
                    // Perfil já existe, apenas garante que está atualizado (sem travar se falhar)
                    val existing = CloudUserProfileDocument.decode(uid, snapshot.data!!)
                    reference.update("updatedAt", System.currentTimeMillis())
                    existing
                }
            }
        } catch (error: Throwable) {
            android.util.Log.e("AuthRepository", "Falha ao obter/criar perfil: ${error.message}", error)
            throwBootstrap("profile.get-or-create", uid, error)
        }

        profile
    }

    override suspend fun signOutOnline() {
        auth.signOut()
    }

    override fun currentFirebaseUid(): String? = auth.currentUser?.uid

    override fun observeOnlineProfile(): Flow<CloudUserProfile?> = callbackFlow {
        var profileListener: com.google.firebase.firestore.ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { currentAuth ->
            profileListener?.remove()
            profileListener = null

            val uid = currentAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                profileListener = firestore.collection(CloudUserProfileDocument.COLLECTION).document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        val profile = snapshot?.data?.let { data ->
                            runCatching { CloudUserProfileDocument.decode(uid, data) }.getOrNull()
                        }
                        trySend(profile)
                    }
            }
        }

        auth.addAuthStateListener(authListener)
        awaitClose {
            profileListener?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }

    override suspend fun linkLocalUser(localUserId: Long, firebaseUid: String): Result<Unit> = runCatching {
        val existing = userDao.findByFirebaseUid(firebaseUid)
        if (existing != null && existing.id != localUserId) {
            error("Esta conta online já pertence a outro participante neste dispositivo.")
        }
        userDao.linkFirebase(localUserId, firebaseUid)
    }

    private fun validateFirebaseProject() {
        val actualProject = auth.app.options.projectId
        if (actualProject != EXPECTED_PROJECT_ID) throw FirebaseProjectMismatchException(actualProject)
    }

    private fun throwBootstrap(stage: String, uid: String, error: Throwable): Nothing {
        if (error is CancellationException) throw error
        throw OnlineProfileBootstrapException(stage, uid, error)
    }

    private companion object {
        const val EXPECTED_PROJECT_ID = "legacy-master-liga"
        const val AUTH_TIMEOUT_MS = 30_000L
        const val PROFILE_TIMEOUT_MS = 30_000L
    }
}
