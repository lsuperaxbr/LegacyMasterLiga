package com.example.legacymasterliga.feature.attributes.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.attributes.domain.ImportTeamAttributesResult
import com.example.legacymasterliga.feature.attributes.domain.ImportTeamAttributesUseCase
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

data class ImportTeamAttributesUiState(
    val clubs: List<Club> = emptyList(),
    val selectedClubId: Long? = null,
    val rawText: String = "",
    val isProcessing: Boolean = false,
    val result: ImportTeamAttributesResult? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ImportTeamAttributesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    leagueRepository: LeagueRepository,
    private val clubRepository: ClubRepository,
    private val importUseCase: ImportTeamAttributesUseCase
) : ViewModel() {

    private val navClubId: Long? = savedStateHandle.get<Long>("clubId")?.takeIf { it > 0L }

    private val selectedClubId = MutableStateFlow<Long?>(navClubId)
    private val rawText = MutableStateFlow("")
    private val isProcessing = MutableStateFlow(false)
    private val result = MutableStateFlow<ImportTeamAttributesResult?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val activeLeagueId = leagueRepository.observeAll().map { list -> list.firstOrNull()?.id }

    private val clubs = activeLeagueId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else clubRepository.observeManagedByLeague(id)
    }

    val uiState: StateFlow<ImportTeamAttributesUiState> = combine(
        clubs,
        selectedClubId,
        rawText,
        isProcessing,
        result
    ) { flowArray ->
        @Suppress("UNCHECKED_CAST")
        val clubList = flowArray[0] as List<Club>
        val selectedId = flowArray[1] as Long?
        val text = flowArray[2] as String
        val processing = flowArray[3] as Boolean
        val res = flowArray[4] as ImportTeamAttributesResult?
        val effectiveClubId = selectedId ?: navClubId ?: clubList.firstOrNull()?.id
        ImportTeamAttributesUiState(
            clubs = clubList,
            selectedClubId = effectiveClubId,
            rawText = text,
            isProcessing = processing,
            result = res,
            errorMessage = errorMessage.value
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ImportTeamAttributesUiState())

    fun selectClub(clubId: Long) {
        selectedClubId.value = clubId
    }

    fun onTextChange(newText: String) {
        rawText.value = newText
    }

    fun importAttributes() = viewModelScope.launch {
        val targetClubId = uiState.value.selectedClubId
        if (targetClubId == null) {
            errorMessage.value = "Selecione um clube primeiro."
            return@launch
        }
        val text = rawText.value
        if (text.isBlank()) {
            errorMessage.value = "Cole o conteúdo do arquivo .txt antes de importar."
            return@launch
        }

        isProcessing.value = true
        errorMessage.value = null

        val res = importUseCase(targetClubId, text)
        result.value = res
        isProcessing.value = false
    }

    fun clearResult() {
        result.value = null
        errorMessage.value = null
    }

    fun clearMessage() {
        errorMessage.value = null
    }
}
