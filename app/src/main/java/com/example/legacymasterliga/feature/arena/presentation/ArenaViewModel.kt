package com.example.legacymasterliga.feature.arena.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.ArenaDuelRow
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.arena.domain.ArenaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArenaUiState(
    val leagueId: Long? = null,
    val duels: List<ArenaDuelRow> = emptyList(),
    val availableClubs: List<Club> = emptyList(),
    val currentUserRole: UserRole? = null,
    val currentUserId: Long? = null,
    val ownClubIds: List<Long> = emptyList(),
    val feedback: String? = null,
    val isLoading: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArenaViewModel @Inject constructor(
    private val arenaRepository: ArenaRepository,
    private val clubRepository: ClubRepository,
    private val authRepository: AuthRepository,
    private val leagueRepository: LeagueRepository,
) : ViewModel() {

    private val _feedback = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    private val user = authRepository.currentUser
    
    private val leagueId = leagueRepository.observeAll()
        .map { it.firstOrNull()?.id }
        .filterNotNull()

    val uiState: StateFlow<ArenaUiState> = combine(
        leagueId,
        leagueId.flatMapLatest { id -> arenaRepository.observeByLeague(id) },
        leagueId.flatMapLatest { id -> clubRepository.observeActiveByLeague(id) },
        user,
        _feedback,
        _isLoading
    ) { params ->
        val currentLeagueId = params[0] as Long?
        val duels = params[1] as List<ArenaDuelRow>
        val clubs = params[2] as List<Club>
        val currentUser = params[3] as User?
        val message = params[4] as String?
        val loading = params[5] as Boolean

        ArenaUiState(
            leagueId = currentLeagueId,
            duels = duels,
            availableClubs = clubs.filter { !it.isBank },
            currentUserRole = currentUser?.role,
            currentUserId = currentUser?.id,
            ownClubIds = if (currentUser?.role == UserRole.PRESIDENT) clubs.filter { it.presidentUserId == currentUser.id }.map { it.id } else emptyList(),
            feedback = message,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArenaUiState())

    fun createDuel(clubAId: Long, clubBId: Long, stakeCr: Long, note: String?) {
        val currentLeagueId = uiState.value.leagueId ?: return
        val userId = uiState.value.currentUserId ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                arenaRepository.createDuel(currentLeagueId, clubAId, clubBId, stakeCr, note, userId)
            }.onSuccess {
                _feedback.value = "Duelo criado com sucesso!"
            }.onFailure {
                _feedback.value = it.message ?: "Erro ao criar duelo."
            }
            _isLoading.value = false
        }
    }

    fun resolveDuel(duelId: Long, resultType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                arenaRepository.resolveDuel(duelId, resultType)
            }.onSuccess {
                _feedback.value = "Duelo resolvido!"
            }.onFailure {
                _feedback.value = it.message ?: "Erro ao resolver duelo."
            }
            _isLoading.value = false
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }
}
