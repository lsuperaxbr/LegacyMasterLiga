package com.example.legacymasterliga.feature.clubprofile.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.feature.clubprofile.domain.ClubProfileHeader
import com.example.legacymasterliga.feature.clubprofile.domain.ClubProfileRepository
import com.example.legacymasterliga.feature.clubprofile.domain.ClubRecentResult
import com.example.legacymasterliga.feature.clubprofile.domain.ClubSeasonHistory
import com.example.legacymasterliga.core.database.dao.TransferDao
import com.example.legacymasterliga.core.database.dao.ClubTrophyRow
import com.example.legacymasterliga.core.database.model.TransferHistoryRow
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import com.example.legacymasterliga.feature.results.domain.ResultsRepository
import com.example.legacymasterliga.feature.results.domain.Standing
import com.example.legacymasterliga.feature.schedule.domain.ScheduleRepository
import com.example.legacymasterliga.feature.schedule.domain.ScheduleSeasonOption
import com.example.legacymasterliga.core.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClubProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    profileRepository: ClubProfileRepository,
    scheduleRepository: ScheduleRepository,
    resultsRepository: ResultsRepository,
    private val playerRepository: PlayerRepository,
    private val financeRepository: FinanceRepository,
    private val authRepository: AuthRepository,
    private val transferDao: TransferDao,
) : ViewModel() {
    private val clubId: Long = checkNotNull(savedStateHandle["clubId"])
    private val requestedSeasonId: Long? = savedStateHandle.get<Long>("seasonId")?.takeIf { it > 0L }
    private val selectedSeasonId = MutableStateFlow(requestedSeasonId)
    
    private val _feedback = MutableStateFlow<String?>(null)
    val feedback: StateFlow<String?> = _feedback.asStateFlow()

    private val header = profileRepository.observeHeader(clubId)
    private val roster = playerRepository.observeByClub(clubId)
    private val seasons = header.flatMapLatest { profile ->
        if (profile == null) flowOf(emptyList()) else scheduleRepository.observeSeasonOptions(profile.leagueId)
    }
    private val standings = selectedSeasonId.flatMapLatest { seasonId ->
        if (seasonId == null) flowOf(emptyList()) else resultsRepository.observeStandings(seasonId)
    }
    private val recentResults = selectedSeasonId.flatMapLatest { seasonId ->
        if (seasonId == null) flowOf(emptyList()) else profileRepository.observeRecentResults(clubId, seasonId)
    }
    private val history = profileRepository.observeHistory(clubId)
    private val transfers = header.flatMapLatest { profile ->
        if (profile == null) flowOf(emptyList()) else transferDao.observeByClub(profile.leagueId, profile.clubId)
    }
    private val trophies = header.flatMapLatest { profile ->
        if (profile == null) flowOf(emptyList()) else transferDao.observeTrophiesByClub(profile.clubId)
    }
    private val currentUser = authRepository.currentUser

    private val currentState = combine(
        header,
        seasons,
        selectedSeasonId,
        standings,
        recentResults,
        roster,
        transfers,
        trophies,
        currentUser,
    ) { params ->
        val profile = params[0] as ClubProfileHeader?
        val seasonOptions = params[1] as List<ScheduleSeasonOption>
        val seasonId = params[2] as Long?
        val table = params[3] as List<Standing>
        val recent = params[4] as List<ClubRecentResult>
        val players = params[5] as List<com.example.legacymasterliga.domain.model.Player>
        val transferList = params[6] as List<TransferHistoryRow>
        val trophyList = params[7] as List<ClubTrophyRow>
        val user = params[8] as com.example.legacymasterliga.domain.model.User?

        val effectiveSeasonId = seasonId?.takeIf { id -> seasonOptions.any { it.seasonId == id } }
            ?: seasonOptions.firstOrNull()?.seasonId
        if (effectiveSeasonId != seasonId) selectedSeasonId.value = effectiveSeasonId
        ClubProfileUiState(
            header = profile,
            seasons = seasonOptions,
            selectedSeasonId = effectiveSeasonId,
            standing = table.firstOrNull { it.clubId == clubId },
            recentResults = recent,
            players = players,
            transfers = transferList,
            trophies = trophyList,
            isAdmin = user?.role == UserRole.ADMINISTRATOR,
            canManage = user?.role == UserRole.ADMINISTRATOR || (user != null && profile != null && user.id == profile.presidentUserId),
        )
    }

    val uiState: StateFlow<ClubProfileUiState> = combine(currentState, history) { state, seasonHistory ->
        state.copy(history = seasonHistory)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClubProfileUiState())

    fun selectSeason(seasonId: Long) {
        selectedSeasonId.value = seasonId
    }

    private val isLeagueBank: Boolean
        get() {
            val name = uiState.value.header?.clubName ?: return false
            return name.contains("banco", ignoreCase = true)
        }

    private val MAX_SQUAD = 32

    fun addPlayer(name: String, position: String?) {
        viewModelScope.launch {
            if (!isLeagueBank && uiState.value.players.size >= MAX_SQUAD) {
                _feedback.value = "Limite de $MAX_SQUAD jogadores atingido no elenco."
                return@launch
            }
            val leagueId = uiState.value.header?.leagueId ?: return@launch
            saveSinglePlayer(name, position, leagueId, com.example.legacymasterliga.core.model.MarketStatus.NOT_LISTED, null)
        }
    }

    fun addPlayersBatch(names: List<String>, initialPrice: Long?, putForSale: Boolean) {
        viewModelScope.launch {
            val leagueId = uiState.value.header?.leagueId ?: return@launch
            
            val availableSlots = if (isLeagueBank) names.size
                                 else (MAX_SQUAD - uiState.value.players.size).coerceAtLeast(0)
            val namesToAdd = names.take(availableSlots)
            if (namesToAdd.isEmpty()) {
                _feedback.value = "Limite de $MAX_SQUAD jogadores atingido no elenco."
                return@launch
            }
            val skipped = names.size - namesToAdd.size

            val status = if (putForSale) com.example.legacymasterliga.core.model.MarketStatus.FOR_SALE else com.example.legacymasterliga.core.model.MarketStatus.NOT_LISTED
            namesToAdd.forEach { name ->
                saveSinglePlayer(name, null, leagueId, status, initialPrice)
            }
            
            _feedback.value = if (skipped == 0) "Jogadores adicionados com sucesso."
                             else "Adicionados ${namesToAdd.size} jogadores. $skipped ignorados pelo limite de $MAX_SQUAD."
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    fun dispensePlayer(player: com.example.legacymasterliga.domain.model.Player) {
        viewModelScope.launch {
            val actor = currentUser.firstOrNull() ?: return@launch
            val leagueId = uiState.value.header?.leagueId ?: return@launch
            runCatching { financeRepository.dispensePlayer(actor, leagueId, player) }
                .onSuccess { _feedback.value = "${player.name} foi dispensado. Taxa de 5 CR cobrada." }
                .onFailure { error -> _feedback.value = error.message ?: "Não foi possível dispensar o jogador." }
        }
    }

    fun revertTransfer(transferId: Long) {
        viewModelScope.launch {
            runCatching { financeRepository.revertTransfer(transferId) }
                .onSuccess { result ->
                    _feedback.value = if (result.warning != null) result.warning else "Transferência revertida com sucesso."
                }
                .onFailure { error -> _feedback.value = error.message ?: "Não foi possível reverter." }
        }
    }

    fun precifyAllPlayers(price: Long) {
        viewModelScope.launch {
            playerRepository.updateClubPlayersMarket(clubId, com.example.legacymasterliga.core.model.MarketStatus.FOR_SALE, price)
        }
    }

    private suspend fun saveSinglePlayer(name: String, position: String?, leagueId: Long, status: com.example.legacymasterliga.core.model.MarketStatus, price: Long?) {
        playerRepository.savePlayer(
            com.example.legacymasterliga.domain.model.Player(
                id = 0,
                leagueId = leagueId,
                clubId = clubId,
                name = name.trim(),
                position = position,
                marketStatus = status,
                askingPriceCr = price,
                skillImageUri = null,
                notes = null,
                externalPlayerId = null
            )
        )
    }
}

data class ClubProfileUiState(
    val header: ClubProfileHeader? = null,
    val seasons: List<ScheduleSeasonOption> = emptyList(),
    val selectedSeasonId: Long? = null,
    val standing: Standing? = null,
    val recentResults: List<ClubRecentResult> = emptyList(),
    val history: List<ClubSeasonHistory> = emptyList(),
    val players: List<com.example.legacymasterliga.domain.model.Player> = emptyList(),
    val transfers: List<TransferHistoryRow> = emptyList(),
    val trophies: List<ClubTrophyRow> = emptyList(),
    val isAdmin: Boolean = false,
    val canManage: Boolean = false,
)
