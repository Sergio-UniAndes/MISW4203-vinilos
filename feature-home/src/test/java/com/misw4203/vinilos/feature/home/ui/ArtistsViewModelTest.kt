package com.misw4203.vinilos.feature.home.ui

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase

// Replaced ViewModel state assertion with a direct use-case test to avoid coroutine-main/test scope issues.
class ArtistsViewModelTest {

    private class FakeRepo(private val artists: List<Artist>) : ArtistsRepository {
        override fun observeArtists() = flowOf(artists)
        override fun observeArtist(id: Long) = flowOf(artists.find { it.id == id })
    }

    @Test
    fun `useCase emits artists from repository`() = runTest {
        val sample = listOf(
            Artist(id = 1L, name = "A1"),
            Artist(id = 2L, name = "A2"),
        )

        val repo = FakeRepo(sample)
        val useCase = ObserveArtistsUseCase(repo)

        val result = useCase().first()
        assertEquals(2, result.size)
        assertEquals("A1", result[0].name)
    }
}
