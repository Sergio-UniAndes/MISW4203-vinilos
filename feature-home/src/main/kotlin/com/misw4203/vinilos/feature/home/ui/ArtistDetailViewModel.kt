package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ArtistDetailViewModel(
    artistId: Long,
    observeArtistDetailUseCase: ObserveArtistDetailUseCase,
) : ViewModel() {

    val uiState: StateFlow<ArtistDetailUiState> = observeArtistDetailUseCase(artistId)
        .map { artist ->
            ArtistDetailUiState(
                isLoading = false,
                artist = artist,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArtistDetailUiState(),
        )
}
