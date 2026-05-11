package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.cache.ArtistsLocalCache
import com.misw4203.vinilos.feature.home.data.remote.ArtistsService
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteArtistsRepositoryTest {

    private fun fakeService(
        musicians: List<MusicianDto>,
        byId: Map<Long, MusicianDto> = musicians.associateBy { it.id ?: -1L },
    ) = object : ArtistsService {
        override suspend fun getMusicians(): List<MusicianDto> = musicians
        override suspend fun getMusician(id: Long): MusicianDto? = byId[id]
    }

    @Test
    fun observeArtists_mapsServiceDtosToArtists() = runTest {
        val service = fakeService(
            listOf(
                MusicianDto(
                    id = 1L,
                    name = "Ruben Blades",
                    image = "img",
                    description = "Salsa",
                    birthDate = "1948-07-16",
                ),
                MusicianDto(id = null, name = null),
            ),
        )

        val artists = RemoteArtistsRepository(service).observeArtists().first()

        assertEquals(2, artists.size)
        assertEquals(1L, artists[0].id)
        assertEquals("Ruben Blades", artists[0].name)
        assertEquals("Salsa", artists[0].description)
        assertEquals(0L, artists[1].id)
        assertEquals("Unknown", artists[1].name)
    }

    @Test
    fun observeArtist_returnsArtistWithAlbums_fromDetailEndpoint() = runTest {
        val target = MusicianDto(
            id = 5L,
            name = "Lila Downs",
            description = "Folk",
            albums = listOf(
                AlbumDto(id = 10L, name = "Pecados", genre = "Folk", releaseDate = "2010-03-01"),
                AlbumDto(id = 11L, name = "Salón", cover = "https://x/y.jpg"),
            ),
        )
        val repo = RemoteArtistsRepository(fakeService(listOf(target)))

        val artist = repo.observeArtist(5L).first()

        assertNotNull(artist)
        assertEquals("Lila Downs", artist!!.name)
        assertEquals(2, artist.albums.size)
        assertEquals("Pecados", artist.albums[0].name)
        assertEquals("Folk", artist.albums[0].genre)
        assertEquals("https://x/y.jpg", artist.albums[1].cover)
    }

    @Test
    fun observeArtist_returnsNull_whenServiceReturnsNull() = runTest {
        val repo = RemoteArtistsRepository(fakeService(emptyList()))
        assertNull(repo.observeArtist(99L).first())
    }

    @Test
    fun observeArtists_emitsCachedArtists_beforeNetwork() = runTest {
        val cached = MusicianDto(id = 99L, name = "From Cache")
        val network = MusicianDto(id = 1L, name = "From Network")
        val cache = FakeArtistsLocalCache(initial = listOf(cached))
        val service = fakeService(listOf(network))
        val repository = RemoteArtistsRepository(service = service, localCache = cache)

        val emissions = repository.observeArtists().toList()

        assertEquals(2, emissions.size)
        assertEquals("From Cache", emissions[0].single().name)
        assertEquals("From Network", emissions[1].single().name)
    }

    @Test
    fun observeArtists_persistsNetworkResponse_intoLocalCache() = runTest {
        val cache = FakeArtistsLocalCache(initial = null)
        val service = fakeService(listOf(MusicianDto(id = 1L, name = "Fresh")))
        val repository = RemoteArtistsRepository(service = service, localCache = cache)

        repository.observeArtists().toList()

        val persisted = cache.snapshot()
        assertEquals(1, persisted.size)
        assertEquals("Fresh", persisted[0].name)
    }

    @Test
    fun observeArtists_fallsBackToStaleCache_whenNetworkFails() = runTest {
        val cache = FakeArtistsLocalCache(
            initial = null,
            stale = listOf(MusicianDto(id = 7L, name = "Stale Artist")),
        )
        val service = object : ArtistsService {
            override suspend fun getMusicians(): List<MusicianDto> =
                throw java.io.IOException("offline")
            override suspend fun getMusician(id: Long): MusicianDto? = null
        }
        val repository = RemoteArtistsRepository(service = service, localCache = cache)

        val emissions = repository.observeArtists().toList()

        assertEquals(1, emissions.size)
        assertEquals("Stale Artist", emissions[0].single().name)
    }

    @Test
    fun observeArtists_emitsEmpty_whenNetworkFailsAndCacheIsEmpty() = runTest {
        val cache = FakeArtistsLocalCache(initial = null, stale = null)
        val service = object : ArtistsService {
            override suspend fun getMusicians(): List<MusicianDto> =
                throw java.io.IOException("offline")
            override suspend fun getMusician(id: Long): MusicianDto? = null
        }
        val repository = RemoteArtistsRepository(service = service, localCache = cache)

        val emissions = repository.observeArtists().toList()

        assertEquals(1, emissions.size)
        assertTrue(emissions[0].isEmpty())
    }

    @Test
    fun observeArtists_keepsCache_whenServiceSilentlyReturnsEmpty() = runTest {
        val cachedArtist = MusicianDto(id = 7L, name = "Cached")
        val cache = FakeArtistsLocalCache(initial = listOf(cachedArtist))
        val service = fakeService(emptyList())
        val repository = RemoteArtistsRepository(service = service, localCache = cache)

        val emissions = repository.observeArtists().toList()

        assertEquals(1, emissions.size)
        assertEquals("Cached", emissions[0].single().name)
    }

    private class FakeArtistsLocalCache(
        initial: List<MusicianDto>?,
        private val stale: List<MusicianDto>? = initial,
    ) : ArtistsLocalCache {
        private var stored: List<MusicianDto>? = initial

        override suspend fun read(): List<MusicianDto>? = stored
        override suspend fun readStale(): List<MusicianDto>? = stored ?: stale
        override suspend fun write(artists: List<MusicianDto>) {
            stored = artists
        }

        fun snapshot(): List<MusicianDto> = stored.orEmpty()
    }
}
