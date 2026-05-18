package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository
import kotlinx.coroutines.flow.Flow

class ObserveCommentsUseCase(
    private val repository: CommentsRepository,
) {
    operator fun invoke(albumId: String): Flow<List<AlbumComment>> =
        repository.observeComments(albumId)
}
