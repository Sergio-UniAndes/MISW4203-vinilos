package com.misw4203.vinilos.feature.home.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
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
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

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

    fun onPickImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true)
            val processedUri = processCoverImage(context, uri)
            val uploaded = processedUri?.let { uploadCoverUseCase(context.contentResolver, it) }
            _state.value = _state.value.copy(isSubmitting = false)
            if (uploaded != null) {
                onCoverChange(uploaded)
            } else {
                _effects.emit(CreateAlbumUiEffect.ShowMessage("Failed to process cover image"))
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

    private fun processCoverImage(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) return null

        val maxSize = 1024
        val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxSize)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        val square = centerCropSquare(decoded)
        val resized = if (square.width > maxSize) {
            square.scale(maxSize, maxSize)
        } else {
            square
        }

        val outputFile = File(context.cacheDir, "cover_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { out ->
            resized.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return android.net.Uri.fromFile(outputFile).toString()
    }

    private fun centerCropSquare(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        return Bitmap.createBitmap(source, x, y, size, size)
    }

    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var inSampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2
        while (halfWidth / inSampleSize >= maxSize && halfHeight / inSampleSize >= maxSize) {
            inSampleSize *= 2
        }
        return inSampleSize
    }
}
