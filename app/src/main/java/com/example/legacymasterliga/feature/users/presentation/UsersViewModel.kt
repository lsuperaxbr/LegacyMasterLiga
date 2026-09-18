package com.example.legacymasterliga.feature.users.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.security.PasswordHasher
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val clubRepository: ClubRepository,
    private val passwordHasher: PasswordHasher,
    authRepository: AuthRepository,
    leagueRepository: LeagueRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val feedback = MutableStateFlow<UserFeedback?>(null)
    private val clubs = selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList()) else clubRepository.observeManagedByLeague(leagueId)
    }

    private val baseState = combine(
        userRepository.observeAll(),
        leagueRepository.observeAll(),
        clubs,
        selectedLeagueId,
        authRepository.currentUser,
    ) { users, leagues, clubList, selectedId, currentUser ->
        val effectiveLeagueId = selectedId ?: leagues.firstOrNull()?.id
        if (selectedId == null && effectiveLeagueId != null) selectedLeagueId.value = effectiveLeagueId
        UsersUiState(
            users = users,
            leagues = leagues,
            clubs = clubList,
            selectedLeagueId = effectiveLeagueId,
            currentUserId = currentUser?.id,
        )
    }

    val uiState: StateFlow<UsersUiState> = combine(baseState, feedback) { state, currentFeedback ->
        state.copy(feedback = currentFeedback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UsersUiState())

    fun selectLeague(leagueId: Long) {
        selectedLeagueId.value = leagueId
        feedback.value = null
    }

    fun saveUser(
        userId: Long?,
        username: String,
        displayName: String,
        password: String,
        role: UserRole,
        status: AccountStatus,
        clubId: Long?,
    ) {
        val cleanUsername = username.trim()
        val cleanName = displayName.trim()
        if (cleanUsername.length < 3 || cleanName.length < 2) {
            feedback.value = UserFeedback.Error("Informe usuário e nome válidos.")
            return
        }
        if (userId == null && password.length < 6) {
            feedback.value = UserFeedback.Error("A senha inicial deve ter pelo menos 6 caracteres.")
            return
        }
        if (userId == uiState.value.currentUserId && (role != UserRole.ADMINISTRATOR || status != AccountStatus.ACTIVE)) {
            feedback.value = UserFeedback.Error("O Administrador conectado não pode remover o próprio acesso.")
            return
        }

        viewModelScope.launch {
            runCatching {
                val savedId = if (userId == null) {
                    val salt = passwordHasher.generateSalt()
                    userRepository.create(
                        username = cleanUsername,
                        displayName = cleanName,
                        passwordHash = passwordHasher.hash(password.toCharArray(), salt),
                        passwordSalt = salt,
                        role = role,
                    ).also { createdId ->
                        if (status != AccountStatus.ACTIVE) {
                            userRepository.updateProfile(createdId, cleanName, role, status)
                        }
                    }
                } else {
                    userRepository.updateProfile(userId, cleanName, role, status)
                    if (password.isNotBlank()) {
                        require(password.length >= 6) { "A nova senha deve ter pelo menos 6 caracteres." }
                        val salt = passwordHasher.generateSalt()
                        userRepository.updatePassword(userId, passwordHasher.hash(password.toCharArray(), salt), salt)
                    }
                    userId
                }
                clubRepository.assignPresident(savedId, clubId)
            }.onSuccess {
                feedback.value = UserFeedback.Success(if (userId == null) "Usuário criado com sucesso." else "Usuário atualizado com sucesso.")
            }.onFailure { error ->
                feedback.value = UserFeedback.Error(
                    if (error.message?.contains("UNIQUE", ignoreCase = true) == true) "Este nome de usuário já está em uso."
                    else error.message ?: "Não foi possível salvar o usuário.",
                )
            }
        }
    }

    fun clearFeedback() { feedback.value = null }
}

data class UsersUiState(
    val users: List<User> = emptyList(),
    val leagues: List<League> = emptyList(),
    val clubs: List<Club> = emptyList(),
    val selectedLeagueId: Long? = null,
    val currentUserId: Long? = null,
    val feedback: UserFeedback? = null,
)

sealed interface UserFeedback {
    data class Success(val message: String) : UserFeedback
    data class Error(val message: String) : UserFeedback
}
