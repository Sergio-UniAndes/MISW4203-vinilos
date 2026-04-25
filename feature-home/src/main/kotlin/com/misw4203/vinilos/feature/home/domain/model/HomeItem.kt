package com.misw4203.vinilos.feature.home.domain.model

data class HomeItem(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int,
    val genre: String,
    val recordLabel: String? = null,
    val format: String? = null,
    val coverUrl: String? = null,
    val description: String? = null,
    val releaseDate: String? = null,
    val tracks: List<AlbumTrack> = emptyList(),
    val performers: List<AlbumPerformer> = emptyList(),
    val comments: List<AlbumComment> = emptyList(),
)

data class AlbumTrack(
    val id: Long,
    val name: String,
    val duration: String,
)

data class AlbumPerformer(
    val id: Long,
    val name: String,
    val image: String? = null,
    val description: String? = null,
    val birthDate: String? = null,
)

data class AlbumComment(
    val id: Long,
    val description: String,
    val rating: Int,
)
