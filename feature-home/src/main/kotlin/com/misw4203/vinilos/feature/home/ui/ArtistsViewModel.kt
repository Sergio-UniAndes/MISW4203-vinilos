package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ArtistsUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
)

class ArtistsViewModel(
    observeArtistsUseCase: ObserveArtistsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<ArtistsUiState> = combine(observeArtistsUseCase(), query) { all, currentQuery ->
        val trimmed = currentQuery.trim()
        val filtered = if (trimmed.isEmpty()) {
            all
        } else {
            all.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
        ArtistsUiState(
            isLoading = false,
            artists = filtered,
            totalCount = all.size,
            query = currentQuery,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ArtistsUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }
}
