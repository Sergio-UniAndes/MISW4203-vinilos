package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.HomeItem

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val album: HomeItem? = null,
)

