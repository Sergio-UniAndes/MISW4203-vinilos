package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRepo(private val artists: List<Artist>) : ArtistsRepository {
        override fun observeArtists(): Flow<List<Artist>> = flowOf(artists)
        override fun observeArtist(id: Long) = flowOf(artists.find { it.id == id })
    }

    private val sample = listOf(
        Artist(id = 1L, name = "Ruben Blades", description = "Salsa"),
        Artist(id = 2L, name = "Joan Manuel Serrat", description = "Cantautor"),
        Artist(id = 3L, name = "Lila Downs", description = "Folk"),
    )

    @Test
    fun uiState_emitsArtists_afterLoad() = runTest {
        val viewModel = ArtistsViewModel(ObserveArtistsUseCase(FakeRepo(sample)))

        val states = mutableListOf<ArtistsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        val loaded = states.last()
        assertEquals(false, loaded.isLoading)
        assertEquals(3, loaded.artists.size)
        assertEquals(3, loaded.totalCount)
        assertEquals("", loaded.query)

        job.cancel()
    }

    @Test
    fun onQueryChange_filtersArtistsByName_caseInsensitive() = runTest {
        val viewModel = ArtistsViewModel(ObserveArtistsUseCase(FakeRepo(sample)))

        val states = mutableListOf<ArtistsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()
        viewModel.onQueryChange("ruben")
        testScheduler.advanceUntilIdle()

        val filtered = states.last()
        assertEquals("ruben", filtered.query)
        assertEquals(1, filtered.artists.size)
        assertEquals("Ruben Blades", filtered.artists[0].name)
        assertEquals(3, filtered.totalCount)

        job.cancel()
    }

    @Test
    fun onQueryChange_emptyQuery_restoresAllArtists() = runTest {
        val viewModel = ArtistsViewModel(ObserveArtistsUseCase(FakeRepo(sample)))

        val states = mutableListOf<ArtistsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()
        viewModel.onQueryChange("Lila")
        testScheduler.advanceUntilIdle()
        assertEquals(1, states.last().artists.size)

        viewModel.onQueryChange("")
        testScheduler.advanceUntilIdle()
        assertEquals(3, states.last().artists.size)

        job.cancel()
    }

    @Test
    fun onQueryChange_trimsWhitespace_andStillFinds() = runTest {
        val viewModel = ArtistsViewModel(ObserveArtistsUseCase(FakeRepo(sample)))

        val states = mutableListOf<ArtistsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()
        viewModel.onQueryChange("  serrat  ")
        testScheduler.advanceUntilIdle()

        assertEquals(1, states.last().artists.size)
        assertEquals("Joan Manuel Serrat", states.last().artists[0].name)

        job.cancel()
    }
}
