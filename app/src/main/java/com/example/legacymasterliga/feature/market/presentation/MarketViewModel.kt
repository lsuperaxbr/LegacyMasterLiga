package com.example.legacymasterliga.feature.market.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.ClubDao
import com.example.legacymasterliga.core.model.MarketStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.Player
import com.example.legacymasterliga.domain.model.User
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.domain.repository.PlayerRepository
import com.example.legacymasterliga.feature.finance.domain.ClubBalance
import com.example.legacymasterliga.feature.finance.domain.FinanceRepository
import com.example.legacymasterliga.feature.finance.domain.MarketTransfer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MarketUiState(
    val leagues: List<League> = emptyList(), 
    val selectedLeagueId: Long? = null,
    val clubs: List<Club> = emptyList(), 
    val balances: List<ClubBalance> = emptyList(),
    val transfers: List<MarketTransfer> = emptyList(),
    val players: List<Player> = emptyList(),
    val query: String? = null,
    val statusFilter: MarketStatus? = null,
    val clubFilter: Long? = null,
    val isAdmin: Boolean = false,
    val ownClubIds: List<Long> = emptyList(), 
    val feedback: String? = null,
    val isLoading: Boolean = false,
)

private data class MarketContext(val leagues: List<League>, val leagueId: Long?, val isAdmin: Boolean, val ownClubIds: List<Long>)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val playerRepository: PlayerRepository,
    leagueRepository: LeagueRepository,
    clubRepository: ClubRepository,
    authRepository: AuthRepository,
    clubDao: ClubDao,
) : ViewModel() {
    private val selectedLeague = MutableStateFlow<Long?>(null)
    private val playerQuery = MutableStateFlow<String?>(null)
    private val playerStatusFilter = MutableStateFlow<MarketStatus?>(null)
    private val playerClubFilter = MutableStateFlow<Long?>(null)
    
    private val feedback = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val user = authRepository.currentUser
    private val presidentClubs = user.flatMapLatest { current -> if (current?.role == UserRole.PRESIDENT) clubDao.observeAllByPresident(current.id) else flowOf(emptyList()) }
    private val context = combine(user, leagueRepository.observeAll(), presidentClubs, selectedLeague) { current, leagues, own, choice ->
        val admin = current?.role == UserRole.ADMINISTRATOR
        val leagueId = if (admin) choice ?: leagues.firstOrNull()?.id else own.firstOrNull()?.leagueId ?: leagues.firstOrNull()?.id
        MarketContext(leagues, leagueId, admin, own.map { it.id })
    }
    private val data = context.flatMapLatest { ctx ->
        val id = ctx.leagueId ?: return@flatMapLatest flowOf(DataBundle())
        combine(
            clubRepository.observeActiveByLeague(id),
            repository.observeBalances(id),
            repository.observeTransfers(id),
            combine(playerQuery, playerStatusFilter, playerClubFilter) { q, s, c -> Triple(q, s, c) }
                .flatMapLatest { (q, s, c) -> playerRepository.observeMarket(id, s, c, q) }
        ) { clubs, balances, transfers, players ->
            DataBundle(clubs, balances, transfers, players)
        }
    }
    val uiState: StateFlow<MarketUiState> = combine(
        context, data, feedback, _isLoading, playerQuery, playerStatusFilter, playerClubFilter
    ) { params ->
        val ctx = params[0] as MarketContext
        val d = params[1] as DataBundle
        val message = params[2] as String?
        val loading = params[3] as Boolean
        val query = params[4] as String?
        val status = params[5] as MarketStatus?
        val club = params[6] as Long?

        MarketUiState(
            leagues = ctx.leagues,
            selectedLeagueId = ctx.leagueId,
            clubs = d.clubs,
            balances = d.balances,
            transfers = d.transfers,
            players = d.players,
            query = query,
            statusFilter = status,
            clubFilter = club,
            isAdmin = ctx.isAdmin,
            ownClubIds = ctx.ownClubIds,
            feedback = message,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MarketUiState())

    fun selectLeague(id: Long) { selectedLeague.value = id }
    
    fun setPlayerQuery(query: String?) { playerQuery.value = query }
    fun setPlayerStatusFilter(status: MarketStatus?) { playerStatusFilter.value = status }
    fun setPlayerClubFilter(clubId: Long?) { playerClubFilter.value = clubId }

    fun clearFeedback() { feedback.value = null }
    
    fun transfer(player: String, originId: Long, destinationId: Long, valueCr: Long, note: String? = null, type: String = "TRANSFER") = viewModelScope.launch {
        if (uiState.value.isLoading) return@launch
        val actor = user.firstOrNull()
        val leagueId = uiState.value.selectedLeagueId
        if (actor == null || leagueId == null) { feedback.value = "Sessão ou liga inválida."; return@launch }
        feedback.value = null
        _isLoading.value = true
        runCatching { repository.registerTransfer(actor, leagueId, player, originId, destinationId, valueCr, note, type) }
            .onSuccess {
                // Se for dispensa, marca jogador automaticamente como À Venda por 5 CR
                if (type == "RELEASE") {
                    val dispensedPlayer = uiState.value.players
                        .find { it.name.trim().equals(player.trim(), ignoreCase = true) }
                    if (dispensedPlayer != null) {
                        runCatching {
                            playerRepository.updateMarketStatus(
                                dispensedPlayer.id,
                                MarketStatus.FOR_SALE,
                                5L
                            )
                        }
                    }
                }
                feedback.value = "Operação registrada com sucesso."
            }
            .onFailure { feedback.value = it.message ?: "Não foi possível realizar a operação." }
        _isLoading.value = false
    }

    fun transferRealPlayer(playerId: Long, destinationClubId: Long, valueCr: Long, note: String? = null) = viewModelScope.launch {
        if (uiState.value.isLoading) return@launch
        val actor = user.firstOrNull()
        val leagueId = uiState.value.selectedLeagueId
        val currentPlayer = uiState.value.players.find { it.id == playerId }
        if (actor == null || leagueId == null || currentPlayer == null) {
            feedback.value = "Não foi possível localizar o jogador ou a sessão."
            return@launch
        }
        val originClubId = currentPlayer.clubId
        if (originClubId == destinationClubId) {
            feedback.value = "O jogador já pertence a este clube."
            return@launch
        }
        feedback.value = null
        _isLoading.value = true
        runCatching {
            repository.transferPlayerAtomic(actor, leagueId, currentPlayer, destinationClubId, valueCr, note)
        }.onSuccess {
            feedback.value = "${currentPlayer.name} transferido com sucesso."
            _isLoading.value = false
        }.onFailure { error ->
            feedback.value = error.message ?: "Não foi possível concluir a transferência."
            _isLoading.value = false
        }
    }

    fun deleteTransfer(transferId: Long) = viewModelScope.launch {
        if (uiState.value.isLoading) return@launch
        _isLoading.value = true
        runCatching { repository.revertTransfer(transferId) }
            .onSuccess { result ->
                feedback.value = if (result.warning != null) result.warning else "Transferência revertida com sucesso."
            }
            .onFailure { error -> feedback.value = error.message ?: "Não foi possível reverter." }
        _isLoading.value = false
    }

    fun addFreeAgentsBatch(names: List<String>, priceCr: Long) = viewModelScope.launch {
        val leagueId = uiState.value.selectedLeagueId ?: run {
            feedback.value = "Selecione uma liga primeiro."
            return@launch
        }
        val bankClub = uiState.value.clubs.find { it.isBank } ?: run {
            feedback.value = "Banco da Liga não encontrado."
            return@launch
        }
        var added = 0
        var skipped = 0
        names.forEach { name ->
            runCatching {
                playerRepository.savePlayer(
                    Player(
                        id = 0,
                        leagueId = leagueId,
                        clubId = bankClub.id,
                        name = name.trim(),
                        position = null,
                        marketStatus = MarketStatus.FOR_SALE,
                        askingPriceCr = priceCr,
                        skillImageUri = null,
                        notes = null,
                        externalPlayerId = null,
                    )
                )
            }.onSuccess { added++ }.onFailure { skipped++ }
        }
        feedback.value = if (skipped == 0)
            "$added reforço(s) adicionado(s) ao Banco."
        else
            "$added adicionado(s). $skipped ignorado(s)."
    }

    fun swap(playerA: String, clubAId: Long, playerB: String, clubBId: Long, compensation: Long, payerId: Long?, note: String?) = viewModelScope.launch {
        if (uiState.value.isLoading) return@launch
        val actor = user.firstOrNull()
        val leagueId = uiState.value.selectedLeagueId
        if (actor == null || leagueId == null) { feedback.value = "Sessão ou liga inválida."; return@launch }
        feedback.value = null
        _isLoading.value = true
        runCatching { repository.registerSwap(actor, leagueId, playerA, clubAId, playerB, clubBId, compensation, payerId, note) }
            .onSuccess { feedback.value = "Troca realizada com sucesso." }
            .onFailure { feedback.value = it.message ?: "Não foi possível realizar a troca." }
        _isLoading.value = false
    }

    fun setPlayerMarketStatus(player: Player, status: MarketStatus, priceCr: Long?) {
        viewModelScope.launch {
            val ownClubIds = uiState.value.ownClubIds
            val isAdmin = uiState.value.isAdmin
            // Só permite se for admin ou se o jogador pertence a um dos clubes do presidente
            if (!isAdmin && !ownClubIds.contains(player.clubId)) {
                feedback.value = "Você só pode alterar jogadores do seu clube."
                return@launch
            }
            runCatching {
                playerRepository.updateMarketStatus(player.id, status, priceCr)
            }
            .onSuccess { feedback.value = "Status atualizado." }
            .onFailure { feedback.value = it.message ?: "Não foi possível atualizar o status." }
        }
    }

    fun addFreeAgent(name: String, priceCr: Long) = viewModelScope.launch {
        if (uiState.value.isLoading) return@launch
        val leagueId = uiState.value.selectedLeagueId
        val bankClub = uiState.value.clubs.find { it.isBank }
        if (leagueId == null || bankClub == null) {
            feedback.value = "Dados da liga ou banco inválidos."
            return@launch
        }
        
        _isLoading.value = true
        runCatching {
            val player = Player(
                id = 0,
                leagueId = leagueId,
                clubId = bankClub.id,
                name = name.trim(),
                position = null,
                marketStatus = MarketStatus.FOR_SALE,
                askingPriceCr = priceCr,
                skillImageUri = null,
                notes = null,
                externalPlayerId = null
            )
            playerRepository.savePlayer(player)
        }.onSuccess {
            feedback.value = "Reforço adicionado com sucesso."
        }.onFailure {
            feedback.value = "Falha ao adicionar reforço: ${it.message}"
        }
        _isLoading.value = false
    }

    private data class DataBundle(
        val clubs: List<Club> = emptyList(),
        val balances: List<ClubBalance> = emptyList(),
        val transfers: List<MarketTransfer> = emptyList(),
        val players: List<Player> = emptyList()
    )
}
