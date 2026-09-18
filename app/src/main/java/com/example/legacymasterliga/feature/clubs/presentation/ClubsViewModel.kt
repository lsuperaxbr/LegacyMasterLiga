package com.example.legacymasterliga.feature.clubs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.UserRepository
import com.example.legacymasterliga.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClubsViewModel @Inject constructor(
    private val clubRepository: ClubRepository,
    leagueRepository: LeagueRepository,
    userRepository: UserRepository,
    authRepository: AuthRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val feedback = MutableStateFlow<ClubFeedback?>(null)

    private val leagues = leagueRepository.observeAll()
    private val users = userRepository.observeAll()
    private val currentUser = authRepository.currentUser
    private val clubs = selectedLeagueId.flatMapLatest { leagueId ->
        if (leagueId == null) flowOf(emptyList()) 
        else clubRepository.observeManagedByLeague(leagueId).map { list -> list.filter { !it.isBank } }
    }

    val uiState: StateFlow<ClubsUiState> = combine(
        leagues,
        users,
        selectedLeagueId,
        clubs,
        feedback,
        currentUser
    ) { params ->
        val leagueList = params[0] as List<League>
        val userList = params[1] as List<User>
        val selectedId = params[2] as Long?
        val clubList = params[3] as List<Club>
        val currentFeedback = params[4] as ClubFeedback?
        val user = params[5] as User?

        val effectiveId = selectedId ?: leagueList.firstOrNull()?.id
        if (selectedId == null && effectiveId != null) selectedLeagueId.value = effectiveId
        ClubsUiState(
            leagues = leagueList,
            users = userList,
            selectedLeagueId = effectiveId,
            clubs = clubList,
            feedback = currentFeedback,
            user = user,
            currentUserId = user?.id,
            isAdmin = user?.role == com.example.legacymasterliga.core.model.UserRole.ADMINISTRATOR,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClubsUiState())

    fun selectLeague(leagueId: Long) {
        selectedLeagueId.value = leagueId
        feedback.value = null
    }

    fun saveClub(
        clubId: Long?,
        name: String,
        crestUri: String?,
        presidentUserId: Long?,
        initialBalance: Long = 0L,
    ) {
        val leagueId = selectedLeagueId.value
        if (leagueId == null) {
            feedback.value = ClubFeedback.Error("Selecione uma liga primeiro.")
            return
        }
        val normalizedName = name.trim()
        if (normalizedName.length < 2) {
            feedback.value = ClubFeedback.Error("Informe um nome de clube válido.")
            return
        }
        viewModelScope.launch {
            runCatching {
                if (clubId == null) {
                    clubRepository.create(leagueId, normalizedName, crestUri, presidentUserId, initialBalance)
                } else {
                    clubRepository.update(clubId, leagueId, normalizedName, crestUri, presidentUserId)
                }
            }.onSuccess {
                feedback.value = ClubFeedback.Success(if (clubId == null) "Clube criado com sucesso." else "Clube atualizado com sucesso.")
            }.onFailure { error ->
                val message = if (error.message?.contains("UNIQUE", ignoreCase = true) == true) {
                    "Já existe um clube com esse nome nesta liga."
                } else {
                    error.message ?: "Não foi possível salvar o clube."
                }
                feedback.value = ClubFeedback.Error(message)
            }
        }
    }

    fun bulkCreateClubs(rawNames: List<String>) {
        val leagueId = selectedLeagueId.value
        if (leagueId == null) {
            feedback.value = ClubFeedback.Error("Selecione uma liga primeiro.")
            return
        }
        val names = rawNames
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
        if (names.isEmpty()) {
            feedback.value = ClubFeedback.Error("Nenhum nome válido encontrado na lista.")
            return
        }
        viewModelScope.launch {
            var created = 0
            var skipped = 0
            names.forEach { name ->
                runCatching {
                    clubRepository.create(leagueId, name, null, null, 0L)
                }.onSuccess {
                    created++
                }.onFailure {
                    // Nome duplicado ou inválido: contabiliza e segue para o próximo, sem interromper o lote.
                    skipped++
                }
            }
            feedback.value = if (skipped == 0) {
                ClubFeedback.Success("$created clube(s) criado(s) com sucesso.")
            } else {
                ClubFeedback.Success("$created clube(s) criado(s). $skipped ignorado(s) (nome duplicado ou inválido).")
            }
        }
    }

    fun deleteClub(clubId: Long) {
        viewModelScope.launch {
            runCatching { clubRepository.delete(clubId) }
                .onSuccess { feedback.value = ClubFeedback.Success("Clube excluído com sucesso.") }
                .onFailure { error -> feedback.value = ClubFeedback.Error(error.message ?: "Não foi possível excluir o clube.") }
        }
    }

    fun setClubActive(clubId: Long, active: Boolean) {
        viewModelScope.launch {
            runCatching { clubRepository.setActive(clubId, active) }
                .onSuccess { feedback.value = ClubFeedback.Success(if (active) "Clube ativado." else "Clube desativado.") }
                .onFailure { error -> feedback.value = ClubFeedback.Error(error.message ?: "Não foi possível atualizar o clube.") }
        }
    }

    fun clearFeedback() {
        feedback.value = null
    }
}

data class ClubsUiState(
    val leagues: List<League> = emptyList(),
    val users: List<User> = emptyList(),
    val selectedLeagueId: Long? = null,
    val clubs: List<Club> = emptyList(),
    val feedback: ClubFeedback? = null,
    val user: User? = null,
    val currentUserId: Long? = null,
    val isAdmin: Boolean = false,
)

sealed interface ClubFeedback {
    data class Success(val message: String) : ClubFeedback
    data class Error(val message: String) : ClubFeedback
}
