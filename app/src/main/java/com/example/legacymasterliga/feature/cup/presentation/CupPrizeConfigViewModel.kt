package com.example.legacymasterliga.feature.cup.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.core.database.dao.PrizeDao
import com.example.legacymasterliga.core.database.entity.CompetitionPrizeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CupPrizeConfigUiState(
    val championCr: String = "",
    val runnerUpCr: String = "",
    val participationCr: String = "",
    val feedback: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CupPrizeConfigViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val prizeDao: PrizeDao
) : ViewModel() {

    private val competitionId: Long = checkNotNull(savedStateHandle["competitionId"])
    private val _feedback = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<CupPrizeConfigUiState> = prizeDao.observeConfiguration(competitionId)
        .map { config ->
            CupPrizeConfigUiState(
                championCr = config?.championPrizeCr?.toString() ?: "0",
                runnerUpCr = config?.runnerUpPrizeCr?.toString() ?: "0",
                participationCr = config?.participationPrizeCr?.toString() ?: "0",
                isLoading = false
            )
        }
        .combine(_feedback) { state, msg -> state.copy(feedback = message(msg)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CupPrizeConfigUiState())

    private fun message(msg: String?): String? = msg

    fun savePrizes(champion: String, runnerUp: String, participation: String) {
        viewModelScope.launch {
            val championCr = champion.toLongOrNull() ?: 0L
            val runnerUpCr = runnerUp.toLongOrNull() ?: 0L
            val participationCr = participation.toLongOrNull() ?: 0L
            
            prizeDao.upsertConfiguration(
                CompetitionPrizeEntity(
                    competitionId = competitionId,
                    championPrizeCr = championCr,
                    runnerUpPrizeCr = runnerUpCr,
                    participationPrizeCr = participationCr,
                )
            )
            _feedback.value = "Premiação da Copa salva."
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }
}
