package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.AddTrackUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCollectorsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCommentsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.PostCommentUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AlbumDetailViewModel(
    private val albumId: String,
    observeAlbumDetailUseCase: ObserveAlbumDetailUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    observeCommentsUseCase: ObserveCommentsUseCase,
    observeCollectorsUseCase: ObserveCollectorsUseCase,
    private val addTrackUseCase: AddTrackUseCase,
    private val postCommentUseCase: PostCommentUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(AddTrackDialogState())
    private val _commentForm = MutableStateFlow(CommentFormState())

    private val firstCollectorId: StateFlow<Long?> = observeCollectorsUseCase()
        .map { collectors -> collectors.firstOrNull()?.id }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<AlbumDetailUiState> = combine(
        observeAlbumDetailUseCase(albumId),
        observeSessionUseCase(),
        observeCommentsUseCase(albumId),
        _dialogState,
        _commentForm,
    ) { album, session, comments, dialog, comment ->
        AlbumDetailUiState(
            isLoading = false,
            album = album,
            canCreate = session?.permissions?.canCreate == true,
            showAddTrackDialog = dialog.show,
            isAddingTrack = dialog.isAdding,
            trackName = dialog.name,
            trackDuration = dialog.duration,
            addTrackError = dialog.error,
            comments = comments,
            commentDraft = comment.draft,
            commentRating = comment.rating,
            isPostingComment = comment.isPosting,
            commentError = comment.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AlbumDetailUiState(),
    )

    private val _effects = MutableSharedFlow<AlbumDetailUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onAddTrackClick() {
        _dialogState.update { it.copy(show = true, name = "", duration = "", error = null) }
    }

    fun onDismissAddTrack() {
        _dialogState.value = AddTrackDialogState()
    }

    fun onTrackNameChange(value: String) {
        _dialogState.update { it.copy(name = value, error = null) }
    }

    fun onTrackDurationChange(value: String) {
        _dialogState.update { it.copy(duration = value, error = null) }
    }

    fun onConfirmAddTrack() {
        val s = _dialogState.value
        if (s.name.isBlank()) {
            _dialogState.update { it.copy(error = "Track name is required") }
            return
        }
        viewModelScope.launch {
            _dialogState.update { it.copy(isAdding = true, error = null) }
            val ok = addTrackUseCase(albumId, s.name.trim(), s.duration.trim())
            if (ok) {
                _dialogState.value = AddTrackDialogState()
                _effects.emit(AlbumDetailUiEffect.TrackAdded)
            } else {
                _dialogState.update { it.copy(isAdding = false, error = "Could not add track. Please try again.") }
            }
        }
    }

    fun onCommentDraftChange(value: String) {
        _commentForm.update { it.copy(draft = value, error = null) }
    }

    fun onCommentRatingChange(rating: Int) {
        _commentForm.update { it.copy(rating = rating.coerceIn(0, 5), error = null) }
    }

    fun onPostComment() {
        val state = _commentForm.value
        if (state.isPosting) return
        viewModelScope.launch {
            _commentForm.update { it.copy(isPosting = true, error = null) }
            val collectorId = firstCollectorId.value
                ?: withTimeoutOrNull(COLLECTOR_RESOLUTION_TIMEOUT_MS) {
                    firstCollectorId.first { it != null }
                }
            val result = postCommentUseCase(
                albumId = albumId,
                description = state.draft,
                rating = state.rating,
                collectorId = collectorId,
            )
            when (result) {
                is PostCommentUseCase.Result.Success -> {
                    _commentForm.value = CommentFormState()
                    _effects.emit(AlbumDetailUiEffect.CommentPosted)
                }
                PostCommentUseCase.Result.EmptyDescription ->
                    _commentForm.update { it.copy(isPosting = false, error = "Write something before posting.") }
                PostCommentUseCase.Result.InvalidRating ->
                    _commentForm.update { it.copy(isPosting = false, error = "Rating must be between 0 and 5.") }
                PostCommentUseCase.Result.NoCollector ->
                    _commentForm.update { it.copy(isPosting = false, error = "No collector available. Try again in a moment.") }
                PostCommentUseCase.Result.NetworkError ->
                    _commentForm.update { it.copy(isPosting = false, error = "Could not post comment. Please try again.") }
            }
        }
    }
}

private data class AddTrackDialogState(
    val show: Boolean = false,
    val isAdding: Boolean = false,
    val name: String = "",
    val duration: String = "",
    val error: String? = null,
)

private data class CommentFormState(
    val draft: String = "",
    val rating: Int = AlbumDetailUiState.DEFAULT_COMMENT_RATING,
    val isPosting: Boolean = false,
    val error: String? = null,
)

private const val COLLECTOR_RESOLUTION_TIMEOUT_MS = 5_000L
