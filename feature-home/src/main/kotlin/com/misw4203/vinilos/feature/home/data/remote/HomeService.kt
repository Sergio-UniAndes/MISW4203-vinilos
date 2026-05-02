package com.misw4203.vinilos.feature.home.data.remote

import android.content.ContentResolver
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto

interface HomeService {
    suspend fun getAlbums(): List<AlbumDto>
    suspend fun getAlbum(id: String): AlbumDto?
    suspend fun createAlbum(album: AlbumDto): Boolean
    suspend fun uploadCover(contentResolver: ContentResolver, uriString: String): String?
}
