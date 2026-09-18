package com.example.legacymasterliga.feature.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import com.example.legacymasterliga.feature.news.domain.NewsArticle
import com.example.legacymasterliga.feature.news.domain.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NewsUiState(
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: Long? = null,
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val articles: List<NewsArticle> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewsViewModel @Inject constructor(
    leagueRepository: LeagueRepository,
    private val newsRepository: NewsRepository,
) : ViewModel() {
    private val selectedLeagueId = MutableStateFlow<Long?>(null)
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val context = combine(leagueRepository.observeAll(), selectedLeagueId) { leagues, selected ->
        leagues to (selected ?: leagues.firstOrNull()?.id)
    }
    private val articles = context.flatMapLatest { (_, leagueId) ->
        if (leagueId == null) flowOf(emptyList()) else newsRepository.observeByLeague(leagueId)
    }

    val uiState: StateFlow<NewsUiState> = combine(context, articles, selectedCategory) { current, allArticles, category ->
        val effectiveCategory = category?.takeIf { selected -> allArticles.any { it.category == selected } }
        NewsUiState(
            leagues = current.first,
            selectedLeagueId = current.second,
            selectedCategory = effectiveCategory,
            categories = allArticles.map { it.category }.distinct().sorted(),
            articles = if (effectiveCategory == null) allArticles else allArticles.filter { it.category == effectiveCategory },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewsUiState())

    fun selectLeague(leagueId: Long) {
        selectedLeagueId.value = leagueId
        selectedCategory.value = null
    }

    fun selectCategory(category: String?) {
        selectedCategory.value = category
    }
}
