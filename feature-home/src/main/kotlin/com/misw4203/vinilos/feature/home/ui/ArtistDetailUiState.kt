package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.Artist

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val artist: Artist? = null,
)
