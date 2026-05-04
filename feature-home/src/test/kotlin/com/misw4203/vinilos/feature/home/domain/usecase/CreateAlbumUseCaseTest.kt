package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateAlbumUseCaseTest {

    @Test
    fun invoke_delegatesToRepository_andReturnsTrueOnSuccess() = runTest {
        val repository = FakeHomeRepository(result = true)
        val useCase = CreateAlbumUseCase(repository)
        val album = sampleAlbum()

        val result = useCase(album)

        assertTrue(result)
        assertTrue(repository.createAlbumCalled)
        assertEquals(album, repository.lastAlbum)
    }

    @Test
    fun invoke_delegatesToRepository_andReturnsFalseOnFailure() = runTest {
        val repository = FakeHomeRepository(result = false)
        val useCase = CreateAlbumUseCase(repository)
        val album = sampleAlbum()

        val result = useCase(album)

        assertFalse(result)
        assertTrue(repository.createAlbumCalled)
        assertEquals(album, repository.lastAlbum)
    }

    private fun sampleAlbum(): AlbumDto = AlbumDto(
        name = "Buscando América",
        cover = "https://example.com/cover.jpg",
        releaseDate = "1984-08-01T00:00:00.000Z",
        description = "A classic salsa album with a rich sound and story.",
        genre = "Salsa",
        recordLabel = "Elektra",
    )

    private class FakeHomeRepository(
        private val result: Boolean,
    ) : HomeRepository {
        var createAlbumCalled: Boolean = false
        var lastAlbum: AlbumDto? = null

        override fun observeItems(): Flow<List<HomeItem>> = flowOf(emptyList())

        override fun observeItem(id: String): Flow<HomeItem?> = flowOf(null)

        override suspend fun createAlbum(album: AlbumDto): Boolean {
            createAlbumCalled = true
            lastAlbum = album
            return result
        }

        override suspend fun uploadCover(contentResolver: android.content.ContentResolver, uriString: String): String? = null
    }
}



