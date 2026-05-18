package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCommentUseCaseTest {

    @Test
    fun invoke_returnsEmptyDescription_whenBlank() = runTest {
        val repository = FakeCommentsRepository()
        val result = PostCommentUseCase(repository)(
            albumId = "1",
            description = "   ",
            rating = 4,
            collectorId = 1L,
        )

        assertEquals(PostCommentUseCase.Result.EmptyDescription, result)
        assertNull(repository.lastDescription)
    }

    @Test
    fun invoke_returnsInvalidRating_whenBelowZero() = runTest {
        val repository = FakeCommentsRepository()
        val result = PostCommentUseCase(repository)(
            albumId = "1",
            description = "ok",
            rating = -1,
            collectorId = 1L,
        )

        assertEquals(PostCommentUseCase.Result.InvalidRating, result)
        assertNull(repository.lastDescription)
    }

    @Test
    fun invoke_returnsInvalidRating_whenAboveFive() = runTest {
        val repository = FakeCommentsRepository()
        val result = PostCommentUseCase(repository)(
            albumId = "1",
            description = "ok",
            rating = 6,
            collectorId = 1L,
        )

        assertEquals(PostCommentUseCase.Result.InvalidRating, result)
    }

    @Test
    fun invoke_returnsNoCollector_whenCollectorIdIsNull() = runTest {
        val repository = FakeCommentsRepository()
        val result = PostCommentUseCase(repository)(
            albumId = "1",
            description = "ok",
            rating = 3,
            collectorId = null,
        )

        assertEquals(PostCommentUseCase.Result.NoCollector, result)
        assertNull(repository.lastDescription)
    }

    @Test
    fun invoke_returnsNetworkError_whenRepositoryReturnsNull() = runTest {
        val repository = FakeCommentsRepository(postResult = null)
        val result = PostCommentUseCase(repository)(
            albumId = "1",
            description = "Solid record",
            rating = 4,
            collectorId = 7L,
        )

        assertEquals(PostCommentUseCase.Result.NetworkError, result)
    }

    @Test
    fun invoke_returnsSuccess_andTrimsDescription() = runTest {
        val created = AlbumComment(id = 1L, description = "Solid record", rating = 4)
        val repository = FakeCommentsRepository(postResult = created)

        val result = PostCommentUseCase(repository)(
            albumId = "10",
            description = "  Solid record  ",
            rating = 4,
            collectorId = 7L,
        )

        assertTrue(result is PostCommentUseCase.Result.Success)
        assertEquals(created, (result as PostCommentUseCase.Result.Success).comment)
        assertEquals("Solid record", repository.lastDescription)
        assertEquals(4, repository.lastRating)
        assertEquals(7L, repository.lastCollectorId)
        assertEquals("10", repository.lastAlbumId)
    }

    @Test
    fun invoke_acceptsRatingZero() = runTest {
        val repository = FakeCommentsRepository(postResult = AlbumComment(id = 1L, description = "x", rating = 0))
        val result = PostCommentUseCase(repository)("1", "ok", rating = 0, collectorId = 1L)
        assertTrue(result is PostCommentUseCase.Result.Success)
    }

    @Test
    fun invoke_acceptsRatingFive() = runTest {
        val repository = FakeCommentsRepository(postResult = AlbumComment(id = 1L, description = "x", rating = 5))
        val result = PostCommentUseCase(repository)("1", "ok", rating = 5, collectorId = 1L)
        assertTrue(result is PostCommentUseCase.Result.Success)
    }

    private class FakeCommentsRepository(
        private val postResult: AlbumComment? = AlbumComment(id = 99L, description = "Solid record", rating = 4),
    ) : CommentsRepository {
        var lastAlbumId: String? = null
        var lastDescription: String? = null
        var lastRating: Int? = null
        var lastCollectorId: Long? = null

        override fun observeComments(albumId: String): Flow<List<AlbumComment>> = flowOf(emptyList())

        override suspend fun postComment(
            albumId: String,
            description: String,
            rating: Int,
            collectorId: Long,
        ): AlbumComment? {
            lastAlbumId = albumId
            lastDescription = description
            lastRating = rating
            lastCollectorId = collectorId
            return postResult
        }
    }
}
