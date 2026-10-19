package com.example.legacymasterliga.feature.attributes.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.repository.ClubRepository
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.attributes.domain.ImportSquadFolderResult
import com.example.legacymasterliga.feature.attributes.domain.ImportSquadFromFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
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

data class ImportSquadFromFolderUiState(
    val clubs: List<Club> = emptyList(),
    val selectedClubId: Long? = null,
    val selectedFolderUri: Uri? = null,
    val folderName: String? = null,
    val isLoading: Boolean = false,
    val progressMessage: String? = null,
    val importResult: ImportSquadFolderResult? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ImportSquadFromFolderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    leagueRepository: LeagueRepository,
    private val clubRepository: ClubRepository,
    private val importSquadFromFolderUseCase: ImportSquadFromFolderUseCase
) : ViewModel() {

    private val initialClubId: Long? = savedStateHandle.get<Long>("clubId")?.takeIf { it > 0 }

    private val activeLeague = leagueRepository.observeAll().map { list -> list.firstOrNull { it.status.name == "ACTIVE" } ?: list.firstOrNull() }
    private val clubs = activeLeague.flatMapLatest { league ->
        if (league == null) flowOf(emptyList())
        else clubRepository.observeManagedByLeague(league.id).map { list -> list.filter { !it.isBank } }
    }

    private val _selectedClubId = MutableStateFlow<Long?>(initialClubId)
    private val _selectedFolderUri = MutableStateFlow<Uri?>(null)
    private val _folderName = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _progressMessage = MutableStateFlow<String?>(null)
    private val _importResult = MutableStateFlow<ImportSquadFolderResult?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ImportSquadFromFolderUiState> = combine(
        clubs,
        _selectedClubId,
        _selectedFolderUri,
        _folderName,
        _isLoading,
        _progressMessage,
        _importResult,
        _errorMessage
    ) { params ->
        val clubList = params[0] as List<Club>
        val selClubId = params[1] as Long?
        val selUri = params[2] as Uri?
        val fName = params[3] as String?
        val loading = params[4] as Boolean
        val progress = params[5] as String?
        val result = params[6] as ImportSquadFolderResult?
        val err = params[7] as String?

        val effectiveClubId = selClubId ?: clubList.firstOrNull()?.id

        ImportSquadFromFolderUiState(
            clubs = clubList,
            selectedClubId = effectiveClubId,
            selectedFolderUri = selUri,
            folderName = fName,
            isLoading = loading,
            progressMessage = progress,
            importResult = result,
            errorMessage = err
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ImportSquadFromFolderUiState())

    fun selectClub(clubId: Long) {
        _selectedClubId.value = clubId
        _importResult.value = null
        _errorMessage.value = null
    }

    fun selectFolder(uri: Uri, folderName: String?) {
        _selectedFolderUri.value = uri
        _folderName.value = folderName ?: uri.lastPathSegment
        _importResult.value = null
        _errorMessage.value = null
    }

    fun importSquad(context: Context) {
        val clubId = uiState.value.selectedClubId
        if (clubId == null) {
            _errorMessage.value = "Selecione um clube antes de importar."
            return
        }
        val folderUri = uiState.value.selectedFolderUri
        if (folderUri == null) {
            _errorMessage.value = "Selecione a pasta do clube antes de importar."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _progressMessage.value = "Varrendo pasta e importando atletas..."
            _importResult.value = null
            _errorMessage.value = null

            val result = importSquadFromFolderUseCase(clubId, folderUri, context)

            _isLoading.value = false
            _progressMessage.value = null

            if (result.errorMessage != null) {
                _errorMessage.value = result.errorMessage
            } else {
                _importResult.value = result
            }
        }
    }

    fun clearFeedback() {
        _errorMessage.value = null
        _importResult.value = null
    }
}
