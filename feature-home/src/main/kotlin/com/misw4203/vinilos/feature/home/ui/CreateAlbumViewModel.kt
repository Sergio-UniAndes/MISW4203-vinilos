package com.misw4203.vinilos.feature.home.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.usecase.CreateAlbumUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.UploadCoverUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateAlbumUiState(
    val name: String = "",
    val cover: String = "",
    val releaseDate: String = "",
    val description: String = "",
    val genre: String = "",
    val recordLabel: String = "",
    val isSubmitting: Boolean = false,
)

sealed interface CreateAlbumUiEffect {
    object Created : CreateAlbumUiEffect
    data class ShowMessage(val message: String) : CreateAlbumUiEffect
}

class CreateAlbumViewModel(
    private val createAlbumUseCase: CreateAlbumUseCase,
    private val uploadCoverUseCase: UploadCoverUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateAlbumUiState())
    val state: StateFlow<CreateAlbumUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CreateAlbumUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onCoverChange(value: String) {
        _state.value = _state.value.copy(cover = value)
    }

    fun onPickImageUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val uploaded = uploadCoverUseCase(contentResolver, uri.toString())
            _state.value = _state.value.copy(isSubmitting = false)
            if (uploaded != null) {
                onCoverChange(uploaded)
            } else {
                _effects.emit(CreateAlbumUiEffect.ShowMessage("Failed to upload cover"))
            }
        }
    }

    fun onReleaseDateChange(value: String) {
        _state.value = _state.value.copy(releaseDate = value)
    }

    fun onDescriptionChange(value: String) {
        _state.value = _state.value.copy(description = value)
    }

    fun onGenreChange(value: String) {
        _state.value = _state.value.copy(genre = value)
    }

    fun onRecordLabelChange(value: String) {
        _state.value = _state.value.copy(recordLabel = value)
    }

    fun submit() {
        val current = _state.value
        validateCreateAlbumForm(current)?.let { message ->
            viewModelScope.launch { _effects.emit(CreateAlbumUiEffect.ShowMessage(message)) }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val dto = AlbumDto(
                name = current.name,
                title = current.name,
                cover = current.cover.ifBlank { null },
                releaseDate = current.releaseDate.ifBlank { null },
                description = current.description.ifBlank { null },
                genre = current.genre.ifBlank { null },
                recordLabel = current.recordLabel.ifBlank { null },
            )
            val success = createAlbumUseCase(dto)
            _state.value = _state.value.copy(isSubmitting = false)
            if (success) {
                _effects.emit(CreateAlbumUiEffect.Created)
            } else {
                _effects.emit(CreateAlbumUiEffect.ShowMessage("Failed to create album"))
            }
        }
    }
}

