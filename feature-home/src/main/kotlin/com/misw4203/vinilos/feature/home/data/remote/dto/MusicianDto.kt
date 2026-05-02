package com.misw4203.vinilos.feature.home.data.remote.dto

data class MusicianDto(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
    val description: String? = null,
    val birthDate: String? = null,
    val albums: List<AlbumDto> = emptyList(),
)
