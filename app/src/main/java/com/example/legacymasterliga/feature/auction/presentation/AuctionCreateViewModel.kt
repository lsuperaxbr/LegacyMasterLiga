package com.example.legacymasterliga.feature.auction.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.database.dao.PlayerDao
import com.example.legacymasterliga.core.database.model.PlayerWithClub
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.auction.domain.AuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuctionCreateUiState(
    val leagueId: Long? = null,
    val bankPlayers: List<PlayerWithClub> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val feedback: String? = null
)

@HiltViewModel
class AuctionCreateViewModel @Inject constructor(
    private val auctionRepository: AuctionRepository,
    private val leagueRepository: LeagueRepository,
    private val authRepository: AuthRepository,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuctionCreateUiState())
    val uiState: StateFlow<AuctionCreateUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            val leagueId = leagueRepository.observeAll().firstOrNull()?.firstOrNull()?.id
            if (leagueId != null) {
                val bank = clubDao.findBankByLeague(leagueId)
                if (bank != null) {
                    playerDao.observeByClub(bank.id).collectLatest { players ->
                        _uiState.update { it.copy(leagueId = leagueId, bankPlayers = players, isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, feedback = "Banco da Liga não encontrado.") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun createLot(name: String, startAt: Long, endAt: Long, playerIds: List<Long>, onCreated: () -> Unit) {
        if (name.isBlank() || playerIds.isEmpty()) {
            _uiState.update { it.copy(feedback = "Preencha todos os campos e selecione ao menos um jogador.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val userId = authRepository.currentUser.first()?.id ?: 0L
            val leagueId = uiState.value.leagueId ?: return@launch
            
            runCatching {
                auctionRepository.createLot(leagueId, name, startAt, endAt, playerIds, userId)
            }.onSuccess {
                onCreated()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, feedback = e.message ?: "Erro ao criar leilão.") }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }
}
