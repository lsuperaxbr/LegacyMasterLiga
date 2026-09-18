package com.example.legacymasterliga.feature.auction.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.AuctionLotSummary
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.auction.domain.AuctionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuctionListUiState(
    val leagueId: Long? = null,
    val lots: List<AuctionLotSummary> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AuctionListViewModel @Inject constructor(
    private val auctionRepository: AuctionRepository,
    private val leagueRepository: LeagueRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val leagueIdFlow = leagueRepository.observeAll().map { it.firstOrNull()?.id }
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AuctionListUiState> = combine(
        leagueIdFlow.flatMapLatest { id -> 
            if (id == null) flowOf(emptyList()) else auctionRepository.observeLots(id)
        },
        leagueIdFlow,
        authRepository.currentUser
    ) { lots, leagueId, user ->
        if (leagueId != null) {
            viewModelScope.launch { auctionRepository.closeExpiredLots(leagueId) }
            AuctionListUiState(
                leagueId = leagueId,
                lots = lots,
                isAdmin = user?.role == UserRole.ADMINISTRATOR,
                isLoading = false
            )
        } else {
            AuctionListUiState(isLoading = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuctionListUiState())

    fun refresh() {
        uiState.value.leagueId?.let { id ->
            viewModelScope.launch {
                auctionRepository.closeExpiredLots(id)
            }
        }
    }
}
