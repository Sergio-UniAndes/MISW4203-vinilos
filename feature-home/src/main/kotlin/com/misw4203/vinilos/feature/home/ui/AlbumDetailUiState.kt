package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.HomeItem

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val album: HomeItem? = null,
    val canCreate: Boolean = false,
    val showAddTrackDialog: Boolean = false,
    val isAddingTrack: Boolean = false,
    val trackName: String = "",
    val trackDuration: String = "",
    val addTrackError: String? = null,
)

sealed interface AlbumDetailUiEffect {
    data object TrackAdded : AlbumDetailUiEffect
}
