package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.remote.CommentsService
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteCommentsRepositoryTest {

    // -- observeComments ------------------------------------------------------

    @Test
    fun observeComments_emitsParsedDomainModels_fromNetwork() = runTest {
        val service = FakeCommentsService(
            getResponse = mapOf(
                "1" to listOf(
                    CommentDto(id = 1L, description = "Great", rating = 5),
                    CommentDto(id = 2L, description = "Average", rating = 3),
                ),
            ),
        )
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        val comments = repository.observeComments("1").first()

        assertEquals(2, comments.size)
        assertEquals("Great", comments[0].description)
        assertEquals(5, comments[0].rating)
        assertEquals("Average", comments[1].description)
        assertEquals(3, comments[1].rating)
    }

    @Test
    fun observeComments_skipsCommentsWithBlankDescription() = runTest {
        val service = FakeCommentsService(
            getResponse = mapOf(
                "1" to listOf(
                    CommentDto(id = 1L, description = "ok", rating = 4),
                    CommentDto(id = 2L, description = "", rating = 3),
                    CommentDto(id = 3L, description = null, rating = 5),
                ),
            ),
        )
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        val comments = repository.observeComments("1").first()

        assertEquals(1, comments.size)
        assertEquals("ok", comments[0].description)
    }

    @Test
    fun observeComments_emitsEmpty_whenServiceFails() = runTest {
        val service = FakeCommentsService(throwOnGet = true)
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        assertTrue(repository.observeComments("1").first().isEmpty())
    }

    // -- postComment ----------------------------------------------------------

    @Test
    fun postComment_appendsCreatedCommentToCache_andEmitsToObservers() = runTest {
        val service = FakeCommentsService(
            getResponse = mapOf("1" to listOf(CommentDto(id = 1L, description = "First", rating = 4))),
            postResponse = CommentDto(id = 2L, description = "Just posted", rating = 5),
        )
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        val initial = repository.observeComments("1").first()
        assertEquals(1, initial.size)

        val created = repository.postComment("1", "Just posted", rating = 5, collectorId = 1L)

        assertNotNull(created)
        assertEquals("Just posted", created?.description)

        val afterPost = repository.observeComments("1").first()
        assertEquals(2, afterPost.size)
        assertEquals("Just posted", afterPost[1].description)
    }

    @Test
    fun postComment_preservesLocalAppend_whenSubsequentRefreshDoesNotIncludeIt() = runTest {
        // The fake service does not echo posted comments on subsequent GETs.
        // The merge in RemoteCommentsRepository must preserve the local append.
        val service = FakeCommentsService(
            getResponse = mapOf("1" to listOf(CommentDto(id = 1L, description = "First", rating = 4))),
            postResponse = CommentDto(id = 2L, description = "Just posted", rating = 5),
        )
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        repository.observeComments("1").first()
        repository.postComment("1", "Just posted", rating = 5, collectorId = 1L)
        // Force a third observe call that triggers another refresh.
        val finalState = repository.observeComments("1").first()

        assertEquals(2, finalState.size)
        assertEquals(1L, finalState[0].id)
        assertEquals(2L, finalState[1].id)
    }

    @Test
    fun postComment_returnsNull_whenServiceReturnsNull() = runTest {
        val service = FakeCommentsService(postResponse = null)
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        val created = repository.postComment("1", "x", rating = 3, collectorId = 1L)
        assertNull(created)
    }

    @Test
    fun postComment_returnsNull_whenServiceThrows() = runTest {
        val service = FakeCommentsService(throwOnPost = true)
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        val created = repository.postComment("1", "x", rating = 3, collectorId = 1L)
        assertNull(created)
    }

    @Test
    fun postComment_forwardsAllArgumentsToService() = runTest {
        val service = FakeCommentsService(
            postResponse = CommentDto(id = 99L, description = "x", rating = 2),
        )
        val repository = RemoteCommentsRepository(service, ioDispatcher = UnconfinedTestDispatcher(testScheduler))

        repository.postComment(albumId = "55", description = "Detailed take", rating = 2, collectorId = 7L)

        assertEquals("55", service.lastPostAlbumId)
        assertEquals("Detailed take", service.lastPostDescription)
        assertEquals(2, service.lastPostRating)
        assertEquals(7L, service.lastPostCollectorId)
    }

    // -- Fakes ----------------------------------------------------------------

    private class FakeCommentsService(
        private val getResponse: Map<String, List<CommentDto>> = emptyMap(),
        private val postResponse: CommentDto? = null,
        private val throwOnGet: Boolean = false,
        private val throwOnPost: Boolean = false,
    ) : CommentsService {
        var lastPostAlbumId: String? = null
        var lastPostDescription: String? = null
        var lastPostRating: Int? = null
        var lastPostCollectorId: Long? = null

        override suspend fun getComments(albumId: String): List<CommentDto> {
            if (throwOnGet) throw java.io.IOException("offline")
            return getResponse[albumId].orEmpty()
        }

        override suspend fun postComment(
            albumId: String,
            description: String,
            rating: Int,
            collectorId: Long,
        ): CommentDto? {
            if (throwOnPost) throw java.io.IOException("offline")
            lastPostAlbumId = albumId
            lastPostDescription = description
            lastPostRating = rating
            lastPostCollectorId = collectorId
            return postResponse
        }
    }
}
