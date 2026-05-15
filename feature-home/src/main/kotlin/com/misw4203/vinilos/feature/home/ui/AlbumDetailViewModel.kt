package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.AddTrackUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    private val albumId: String,
    observeAlbumDetailUseCase: ObserveAlbumDetailUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    private val addTrackUseCase: AddTrackUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(AddTrackDialogState())

    val uiState: StateFlow<AlbumDetailUiState> = combine(
        observeAlbumDetailUseCase(albumId),
        observeSessionUseCase(),
        _dialogState,
    ) { album, session, dialog ->
        AlbumDetailUiState(
            isLoading = false,
            album = album,
            canCreate = session?.permissions?.canCreate == true,
            showAddTrackDialog = dialog.show,
            isAddingTrack = dialog.isAdding,
            trackName = dialog.name,
            trackDuration = dialog.duration,
            addTrackError = dialog.error,
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
}

private data class AddTrackDialogState(
    val show: Boolean = false,
    val isAdding: Boolean = false,
    val name: String = "",
    val duration: String = "",
    val error: String? = null,
)
