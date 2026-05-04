package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.cache.AlbumsLocalCache
import com.misw4203.vinilos.feature.home.data.remote.HomeService
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import com.misw4203.vinilos.feature.home.data.remote.dto.TrackDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteHomeRepositoryTest {

    @Test
    fun observeItems_mapsDtosFromService() = runTest {
        val service = FakeHomeService(
            albums = listOf(
                AlbumDto(
                    id = 1L,
                    name = "Buscando America",
                    artist = "Ruben Blades",
                    genre = "Salsa",
                    releaseDate = "1984-08-01",
                    recordLabel = "Elektra",
                    tracks = listOf(TrackDto(id = 10, name = "Decisiones", duration = "5:00")),
                    performers = listOf(PerformerDto(id = 9, name = "Ruben Blades")),
                    comments = listOf(CommentDto(id = 2, description = "Great", rating = 5)),
                ),
                AlbumDto(
                    id = 2L,
                    name = "Poeta del Pueblo",
                    artist = "Hector Lavoe",
                    genre = "Salsa",
                ),
            ),
        )
        val repository = RemoteHomeRepository(service)

        val items = repository.observeItems().first()

        assertEquals(2, items.size)
        assertEquals("1", items[0].id)
        assertEquals("Buscando America", items[0].title)
        assertEquals("Ruben Blades", items[0].artist)
        assertEquals(1984, items[0].year)
        assertEquals(1, items[0].tracks.size)
        assertEquals("Decisiones", items[0].tracks[0].name)
        assertEquals("Hector Lavoe", items[1].artist)
    }

    @Test
    fun observeItems_returnsEmpty_whenServiceReturnsEmpty() = runTest {
        val repository = RemoteHomeRepository(FakeHomeService(albums = emptyList()))

        assertTrue(repository.observeItems().first().isEmpty())
    }

    @Test
    fun observeItem_mapsDtoFromService() = runTest {
        val service = FakeHomeService(
            albumsById = mapOf(
                "7" to AlbumDto(
                    id = 7L,
                    name = "Solo",
                    artist = "Lila Downs",
                    genre = "Folk",
                    releaseDate = "2010-03-01",
                    tracks = listOf(TrackDto(id = 1, name = "Track A", duration = "3:30")),
                ),
            ),
        )
        val repository = RemoteHomeRepository(service)

        val item = repository.observeItem("7").first()

        assertEquals("7", item?.id)
        assertEquals("Solo", item?.title)
        assertEquals("Lila Downs", item?.artist)
        assertEquals(2010, item?.year)
        assertEquals(1, item?.tracks?.size)
    }

    @Test
    fun observeItem_returnsNull_whenServiceReturnsNull() = runTest {
        val repository = RemoteHomeRepository(FakeHomeService())

        assertNull(repository.observeItem("missing").first())
    }

    @Test
    fun observeItems_emitsCachedItems_beforeNetwork() = runTest {
        val cachedAlbum = AlbumDto(id = 99L, name = "From Cache", artist = "Cache Artist", genre = "Jazz")
        val networkAlbum = AlbumDto(id = 1L, name = "From Network", artist = "Network Artist", genre = "Rock")
        val cache = FakeAlbumsLocalCache(initial = listOf(cachedAlbum))
        val service = FakeHomeService(albums = listOf(networkAlbum))
        val repository = RemoteHomeRepository(service = service, localCache = cache)

        val emissions = repository.observeItems().toList()

        assertEquals(2, emissions.size)
        assertEquals("From Cache", emissions[0].single().title)
        assertEquals("From Network", emissions[1].single().title)
    }

    @Test
    fun observeItems_persistsNetworkResponse_intoLocalCache() = runTest {
        val cache = FakeAlbumsLocalCache(initial = null)
        val service = FakeHomeService(
            albums = listOf(AlbumDto(id = 1L, name = "Fresh", artist = "Fresh Artist", genre = "Rock")),
        )
        val repository = RemoteHomeRepository(service = service, localCache = cache)

        repository.observeItems().toList()

        val persisted = cache.snapshot()
        assertEquals(1, persisted.size)
        assertEquals("Fresh", persisted[0].name)
    }

    @Test
    fun observeItems_doesNotEmitDuplicate_whenNetworkMatchesLocalCache() = runTest {
        val album = AlbumDto(id = 1L, name = "Same", artist = "Same", genre = "Same")
        val cache = FakeAlbumsLocalCache(initial = listOf(album))
        val service = FakeHomeService(albums = listOf(album))
        val repository = RemoteHomeRepository(service = service, localCache = cache)

        val emissions = repository.observeItems().toList()

        assertEquals(1, emissions.size)
        assertEquals("Same", emissions[0].single().title)
    }

    private class FakeHomeService(
        private val albums: List<AlbumDto> = emptyList(),
        private val albumsById: Map<String, AlbumDto> = emptyMap(),
    ) : HomeService {
        override suspend fun getAlbums(): List<AlbumDto> = albums
        override suspend fun getAlbum(id: String): AlbumDto? = albumsById[id]
        override suspend fun createAlbum(album: AlbumDto): Boolean = true
        override suspend fun uploadCover(contentResolver: android.content.ContentResolver, uriString: String): String? = null
    }

    private class FakeAlbumsLocalCache(
        initial: List<AlbumDto>?,
    ) : AlbumsLocalCache {
        private var stored: List<AlbumDto>? = initial

        override suspend fun read(): List<AlbumDto>? = stored
        override suspend fun write(albums: List<AlbumDto>) {
            stored = albums
        }

        fun snapshot(): List<AlbumDto> = stored.orEmpty()
    }
}
