package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.mapper.toAlbumComment
import com.misw4203.vinilos.feature.home.data.mapper.toAlbumComments
import com.misw4203.vinilos.feature.home.data.remote.CommentsService
import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class RemoteCommentsRepository(
    private val service: CommentsService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CommentsRepository {

    private val cache = MutableStateFlow<Map<String, List<AlbumComment>>>(emptyMap())

    override fun observeComments(albumId: String): Flow<List<AlbumComment>> = cache
        .map { it[albumId].orEmpty() }
        .onStart { refresh(albumId) }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    override suspend fun postComment(
        albumId: String,
        description: String,
        rating: Int,
        collectorId: Long,
    ): AlbumComment? = withContext(ioDispatcher) {
        val created = runCatching {
            service.postComment(albumId, description, rating, collectorId)?.toAlbumComment()
        }.getOrNull() ?: return@withContext null

        cache.update { current ->
            val existing = current[albumId].orEmpty()
            current + (albumId to (existing + created))
        }
        created
    }

    private suspend fun refresh(albumId: String) {
        val fresh = runCatching { service.getComments(albumId).toAlbumComments() }
            .getOrNull() ?: return

        cache.update { current ->
            val existing = current[albumId].orEmpty()
            val merged = mergePreservingLocalAppends(existing, fresh)
            if (merged != existing) current + (albumId to merged) else current
        }
    }

    private fun mergePreservingLocalAppends(
        cached: List<AlbumComment>,
        fresh: List<AlbumComment>,
    ): List<AlbumComment> {
        if (fresh == cached) return cached
        val freshIds = fresh.map(AlbumComment::id).toSet()
        val locallyAppended = cached.filter { it.id !in freshIds }
        return fresh + locallyAppended
    }
}
