package com.misw4203.vinilos.feature.home.data.remote.dto

data class AlbumDto(
    val id: Long? = null,
    val name: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val recordLabel: String? = null,
    val label: String? = null,
    val format: String? = null,
    val cover: String? = null,
    val image: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val tracks: List<TrackDto> = emptyList(),
    val performers: List<PerformerDto> = emptyList(),
    val comments: List<CommentDto> = emptyList(),
)

data class TrackDto(
    val id: Long? = null,
    val name: String? = null,
    val duration: String? = null,
)

data class PerformerDto(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
    val description: String? = null,
    val birthDate: String? = null,
)

data class CommentDto(
    val id: Long? = null,
    val description: String? = null,
    val rating: Int? = null,
)

