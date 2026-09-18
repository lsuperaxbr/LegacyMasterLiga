package com.example.legacymasterliga.feature.auction.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.AuctionItemRow
import com.example.legacymasterliga.core.database.dao.AuctionLotSummary
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.feature.auction.domain.AuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuctionDetailUiState(
    val lot: AuctionLotSummary? = null,
    val items: List<AuctionItemRow> = emptyList(),
    val managedClubs: List<Club> = emptyList(),
    val selectedClubId: Long? = null,
    val currentUserId: Long? = null,
    val isLoading: Boolean = true,
    val feedback: String? = null
)

@HiltViewModel
class AuctionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val auctionRepository: AuctionRepository,
    private val authRepository: AuthRepository,
    private val clubRepository: ClubRepository,
    private val leagueRepository: com.example.legacymasterliga.domain.repository.LeagueRepository,
) : ViewModel() {

    private val lotId: Long = checkNotNull(savedStateHandle["lotId"])
    
    private val _uiState = MutableStateFlow(AuctionDetailUiState())
    val uiState: StateFlow<AuctionDetailUiState> = _uiState.asStateFlow()

    private val selectedClubId = MutableStateFlow<Long?>(null)

    init {
        loadData()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val leagueId = leagueRepository.observeAll().firstOrNull()?.firstOrNull()?.id ?: 0L
            
            val managedClubsFlow = if (user != null) {
                clubRepository.observeManagedByLeague(leagueId).map { clubs ->
                    val userClubs = clubs.filter { it.presidentUserId == user.id }
                    if (selectedClubId.value == null && userClubs.isNotEmpty()) {
                        selectedClubId.value = userClubs.first().id
                    }
                    userClubs
                }
            } else {
                flowOf(emptyList())
            }

            val lotFlow = auctionRepository.observeLots(leagueId).map { lots ->
                lots.find { it.id == lotId }
            }

            combine(
                auctionRepository.observeItems(lotId),
                lotFlow,
                managedClubsFlow,
                selectedClubId,
                flowOf(user?.id)
            ) { items, lotSummary, managedClubs, selectedId, uid ->
                AuctionDetailUiState(
                    lot = lotSummary,
                    items = items,
                    managedClubs = managedClubs,
                    selectedClubId = selectedId,
                    currentUserId = uid,
                    isLoading = false
                )
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    fun selectClub(id: Long) {
        selectedClubId.value = id
    }

    fun placeBid(itemId: Long, amount: Long) {
        val clubId = uiState.value.selectedClubId ?: return
        val userId = uiState.value.currentUserId ?: return
        
        viewModelScope.launch {
            auctionRepository.placeBid(itemId, clubId, amount, userId)
                .onSuccess {
                    _uiState.update { it.copy(feedback = "Lance realizado com sucesso!") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(feedback = e.message ?: "Erro ao dar lance.") }
                }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }
}
