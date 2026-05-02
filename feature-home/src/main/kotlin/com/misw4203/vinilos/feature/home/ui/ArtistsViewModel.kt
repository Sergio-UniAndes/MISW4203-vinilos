package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ArtistsUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val error: String? = null,
)

class ArtistsViewModel(
    observeArtistsUseCase: ObserveArtistsUseCase,
) : ViewModel() {

    val uiState: StateFlow<ArtistsUiState> = observeArtistsUseCase()
        .map { artists ->
            ArtistsUiState(
                isLoading = false,
                artists = artists,
                error = null,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArtistsUiState(),
        )
}

