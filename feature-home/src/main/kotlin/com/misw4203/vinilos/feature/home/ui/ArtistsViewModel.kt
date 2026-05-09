package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

data class ArtistsUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
)

@OptIn(FlowPreview::class)
class ArtistsViewModel(
    observeArtistsUseCase: ObserveArtistsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val allArtists = observeArtistsUseCase().distinctUntilChanged()

    private val filteredArtists = combine(
        allArtists,
        query.debounce(300).distinctUntilChanged()
    ) { all, currentQuery ->
        val trimmed = currentQuery.trim()
        if (trimmed.isEmpty()) {
            all
        } else {
            all.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }.distinctUntilChanged()

    val uiState: StateFlow<ArtistsUiState> = combine(
        allArtists,
        filteredArtists,
        query
    ) { all, filtered, currentQuery ->
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
