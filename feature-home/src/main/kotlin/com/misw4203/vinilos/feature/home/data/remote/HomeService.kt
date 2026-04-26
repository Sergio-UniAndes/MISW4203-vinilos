package com.misw4203.vinilos.feature.home.data.remote

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto

interface HomeService {
    suspend fun getAlbums(): List<AlbumDto>
    suspend fun getAlbum(id: String): AlbumDto?
}
