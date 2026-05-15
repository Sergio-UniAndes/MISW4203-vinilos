package com.misw4203.vinilos.feature.home.domain.repository

import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import kotlinx.coroutines.flow.Flow

interface CommentsRepository {
    fun observeComments(albumId: String): Flow<List<AlbumComment>>
    suspend fun postComment(
        albumId: String,
        description: String,
        rating: Int,
        collectorId: Long,
    ): AlbumComment?
}
