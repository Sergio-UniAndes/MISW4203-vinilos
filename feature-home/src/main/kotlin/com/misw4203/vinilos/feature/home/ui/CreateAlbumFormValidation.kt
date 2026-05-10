package com.misw4203.vinilos.feature.home.ui

import java.net.URI
import java.time.OffsetDateTime

internal const val MIN_DESCRIPTION_LENGTH = 20

internal val AllowedGenres = setOf("Classical", "Salsa", "Rock", "Folk")
internal val AllowedRecordLabels = setOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")

internal fun isValidCoverUrl(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return false

    return runCatching {
        val uri = URI(trimmed)
        when (uri.scheme) {
            "http", "https" -> !uri.host.isNullOrBlank()
            "content", "file" -> !uri.path.isNullOrBlank()
            else -> false
        }
    }.getOrDefault(false)
}

internal fun isValidReleaseDate(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return false

    return runCatching { OffsetDateTime.parse(trimmed) }.isSuccess
}

internal fun isValidDescription(value: String): Boolean = value.trim().length >= MIN_DESCRIPTION_LENGTH

internal fun coverUrlError(value: String): String? = when {
    value.isBlank() -> "Cover image is required"
    !isValidCoverUrl(value) -> "Cover image must be a valid image reference"
    else -> null
}

internal fun releaseDateError(value: String): String? = when {
    value.isBlank() -> "Release date is required"
    !isValidReleaseDate(value) -> "Enter a valid date"
    else -> null
}

internal fun descriptionError(value: String): String? = when {
    value.isBlank() -> "Description is required"
    !isValidDescription(value) -> "Minimum $MIN_DESCRIPTION_LENGTH characters"
    else -> null
}

internal fun nameError(value: String): String? = when {
    value.isBlank() -> "Name is required"
    else -> null
}

internal fun genreError(value: String): String? = when {
    value.isBlank() -> "Genre is required"
    value !in AllowedGenres -> "Genre must be one of: Classical, Salsa, Rock, Folk"
    else -> null
}

internal fun recordLabelError(value: String): String? = when {
    value.isBlank() -> "Record label is required"
    value !in AllowedRecordLabels -> "Record label must be one of: Sony Music, EMI, Discos Fuentes, Elektra, Fania Records"
    else -> null
}

internal fun validateCreateAlbumForm(state: CreateAlbumUiState): String? =
    listOfNotNull(
        nameError(state.name),
        releaseDateError(state.releaseDate),
        coverUrlError(state.cover),
        descriptionError(state.description),
        genreError(state.genre),
        recordLabelError(state.recordLabel),
    ).firstOrNull()
