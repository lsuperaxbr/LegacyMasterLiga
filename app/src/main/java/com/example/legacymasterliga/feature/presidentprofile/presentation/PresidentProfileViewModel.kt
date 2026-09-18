package com.example.legacymasterliga.feature.presidentprofile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.PresidentProfileDao
import com.example.legacymasterliga.core.database.dao.PresidentClubRow
import com.example.legacymasterliga.core.database.dao.PresidentCareerRow
import com.example.legacymasterliga.domain.repository.AuthRepository
import com.example.legacymasterliga.feature.presidentprofile.domain.PresidentProfileSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PresidentProfileViewModel @Inject constructor(
    private val presidentProfileDao: PresidentProfileDao,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<PresidentProfileSnapshot> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(PresidentProfileSnapshot(isLoading = false))
            } else {
                combine(
                    presidentProfileDao.observeClubs(user.id),
                    presidentProfileDao.observeTitles(user.id),
                    presidentProfileDao.observeCupTitles(user.id),
                    presidentProfileDao.observeSeasonsPlayed(user.id),
                    presidentProfileDao.observeCareerTotals(user.id),
                    presidentProfileDao.observeCurrentBalance(user.id),
                    presidentProfileDao.observeMarketMovement(user.id)
                ) { arr ->
                    val clubs = arr[0] as List<PresidentClubRow>
                    val titles = arr[1] as Int
                    val cupTitles = arr[2] as Int
                    val seasons = arr[3] as Int
                    val career = arr[4] as PresidentCareerRow
                    val balance = arr[5] as Long
                    val market = arr[6] as Long

                    PresidentProfileSnapshot(
                        displayName = user.displayName,
                        username = user.username,
                        clubs = clubs.map { it.name },
                        titles = titles,
                        cupTitles = cupTitles,
                        seasonsPlayed = seasons,
                        played = career.played,
                        wins = career.wins,
                        draws = career.draws,
                        losses = career.losses,
                        goalsFor = career.goalsFor,
                        goalsAgainst = career.goalsAgainst,
                        winRate = (career.wins * 100.0 / career.played.coerceAtLeast(1)),
                        currentBalanceCr = balance,
                        marketMovementCr = market,
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PresidentProfileSnapshot(isLoading = true)
        )
}
