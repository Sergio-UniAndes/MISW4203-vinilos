package com.misw4203.vinilos.feature.home.domain.repository

import android.content.ContentResolver
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeItems(): Flow<List<HomeItem>>
    fun observeItem(id: String): Flow<HomeItem?>
    suspend fun createAlbum(album: AlbumDto): Boolean
    suspend fun uploadCover(contentResolver: ContentResolver, uriString: String): String?
    suspend fun addTrack(albumId: String, name: String, duration: String): Boolean
}

