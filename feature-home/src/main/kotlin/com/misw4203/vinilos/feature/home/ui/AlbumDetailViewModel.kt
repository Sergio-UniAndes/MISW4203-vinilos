package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AlbumDetailViewModel(
    albumId: String,
    observeAlbumDetailUseCase: ObserveAlbumDetailUseCase,
) : ViewModel() {

    val uiState: StateFlow<AlbumDetailUiState> = observeAlbumDetailUseCase(albumId)
        .map { album ->
            AlbumDetailUiState(
                isLoading = false,
                album = album,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumDetailUiState(),
        )
}

