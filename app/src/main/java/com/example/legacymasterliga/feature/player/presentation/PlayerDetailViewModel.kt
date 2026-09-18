package com.example.legacymasterliga.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.PlayerAttributesParser
import com.example.legacymasterliga.domain.model.Player
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlayerDetailUiState(
    val player: Player? = null,
    val goalsScored: Int = 0,
    val isLoading: Boolean = true,
    val canManage: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerRepository: PlayerRepository,
    private val authRepository: AuthRepository,
    private val clubDao: ClubDao,
    private val goalEventDao: com.example.legacymasterliga.core.database.dao.GoalEventDao,
) : ViewModel() {
    private val playerId: Long = checkNotNull(savedStateHandle["playerId"])
    
    private val user = authRepository.currentUser
    private val presidentClubs = user.flatMapLatest { current ->
        if (current?.role == UserRole.PRESIDENT) clubDao.observeAllByPresident(current.id) else flowOf(emptyList())
    }

    private val _uiState = MutableStateFlow(PlayerDetailUiState())
    val uiState: StateFlow<PlayerDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlayer()
        observeGoals()
    }

    private fun observeGoals() {
        goalEventDao.observeGoalsByPlayer(playerId)
            .onEach { count -> _uiState.update { it.copy(goalsScored = count) } }
            .launchIn(viewModelScope)
    }

    private fun loadPlayer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val player = playerRepository.findById(playerId)
            val currentUser = user.first()
            val isAdmin = currentUser?.role == UserRole.ADMINISTRATOR
            val ownClubs = presidentClubs.first()
            val canManage = isAdmin || (player != null && ownClubs.any { it.id == player.clubId })

            if (player != null) {
                _uiState.update { it.copy(player = player, isLoading = false, canManage = canManage) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Jogador não encontrado.") }
            }
        }
    }

    fun updateMarketStatus(status: MarketStatus, price: Long?) {
        viewModelScope.launch {
            if (!uiState.value.canManage) {
                _uiState.update { it.copy(errorMessage = "Você não tem permissão para alterar este jogador.") }
                return@launch
            }
            playerRepository.updateMarketStatus(playerId, status, price)
            loadPlayer()
        }
    }

    fun updatePlayerDetails(name: String, position: String?, overall: Int?) {
        viewModelScope.launch {
            if (!uiState.value.canManage) {
                _uiState.update { it.copy(errorMessage = "Você não tem permissão para editar este jogador.") }
                return@launch
            }
            val current = uiState.value.player ?: return@launch
            playerRepository.savePlayer(
                current.copy(
                    name = name.trim(),
                    position = position?.trim()?.ifBlank { null },
                    overall = overall,
                )
            )
            loadPlayer()
        }
    }

    fun updateAttributesFromText(pastedText: String) {
        viewModelScope.launch {
            if (!uiState.value.canManage) return@launch
            val result = PlayerAttributesParser.parse(pastedText)
            if (result.attributesRaw == null) {
                _uiState.update { it.copy(errorMessage = "Encontrei apenas ${result.foundCount} de 26 atributos. Faltando: ${result.missing.joinToString(", ")}") }
                return@launch
            }
            val current = uiState.value.player ?: return@launch
            playerRepository.savePlayer(current.copy(attributesRaw = result.attributesRaw))
            loadPlayer()
        }
    }

    fun deletePlayer(onDeleted: () -> Unit) {
        viewModelScope.launch {
            if (!uiState.value.canManage) {
                _uiState.update { it.copy(errorMessage = "Você não tem permissão para excluir este jogador.") }
                return@launch
            }
            playerRepository.deletePlayer(playerId)
            onDeleted()
        }
    }
}
