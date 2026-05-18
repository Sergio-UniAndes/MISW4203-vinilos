package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository

class PostCommentUseCase(
    private val repository: CommentsRepository,
) {
    sealed interface Result {
        data class Success(val comment: AlbumComment) : Result
        data object EmptyDescription : Result
        data object InvalidRating : Result
        data object NoCollector : Result
        data object NetworkError : Result
    }

    suspend operator fun invoke(
        albumId: String,
        description: String,
        rating: Int,
        collectorId: Long?,
    ): Result {
        val trimmed = description.trim()
        if (trimmed.isEmpty()) return Result.EmptyDescription
        if (rating !in VALID_RATING_RANGE) return Result.InvalidRating
        if (collectorId == null) return Result.NoCollector

        val created = repository.postComment(albumId, trimmed, rating, collectorId)
            ?: return Result.NetworkError
        return Result.Success(created)
    }

    private companion object {
        val VALID_RATING_RANGE = 0..5
    }
}
