package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.remote.ArtistsService
import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteArtistsRepositoryTest {

    private fun fakeService(performers: List<PerformerDto>) = object : ArtistsService {
        override suspend fun getMusicians(): List<PerformerDto> = performers
    }

    @Test
    fun observeArtists_mapsServiceDtosToArtists() = runTest {
        val service = fakeService(
            listOf(
                PerformerDto(
                    id = 1L,
                    name = "Ruben Blades",
                    image = "img",
                    description = "Salsa",
                    birthDate = "1948-07-16",
                ),
                PerformerDto(id = null, name = null),
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
    fun observeArtist_returnsMatchingArtist() = runTest {
        val service = fakeService(
            listOf(
                PerformerDto(id = 1L, name = "A"),
                PerformerDto(id = 2L, name = "B"),
            ),
        )
        val repo = RemoteArtistsRepository(service)

        assertEquals("B", repo.observeArtist(2L).first()?.name)
        assertNull(repo.observeArtist(99L).first())
    }
}
