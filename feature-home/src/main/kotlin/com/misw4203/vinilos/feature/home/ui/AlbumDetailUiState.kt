package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
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
    val comments: List<AlbumComment> = emptyList(),
    val commentDraft: String = "",
    val commentRating: Int = DEFAULT_COMMENT_RATING,
    val isPostingComment: Boolean = false,
    val commentError: String? = null,
) {
    companion object {
        const val DEFAULT_COMMENT_RATING: Int = 5
    }
}

sealed interface AlbumDetailUiEffect {
    data object TrackAdded : AlbumDetailUiEffect
    data object CommentPosted : AlbumDetailUiEffect
}
