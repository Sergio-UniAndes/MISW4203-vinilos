package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.model.ArtistAlbum
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveArtistDetailUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRepo(private val byId: Map<Long, Artist>) : ArtistsRepository {
        override fun observeArtists(): Flow<List<Artist>> = flowOf(byId.values.toList())
        override fun observeArtist(id: Long) = flowOf(byId[id])
    }

    @Test
    fun uiState_emitsArtistWithAlbums_afterLoad() = runTest {
        val target = Artist(
            id = 1L,
            name = "Lila Downs",
            description = "Folk",
            albums = listOf(
                ArtistAlbum(id = 10L, name = "Pecados", genre = "Folk"),
                ArtistAlbum(id = 11L, name = "Salón", cover = "x"),
            ),
        )
        val viewModel = ArtistDetailViewModel(
            artistId = 1L,
            observeArtistDetailUseCase = ObserveArtistDetailUseCase(FakeRepo(mapOf(1L to target))),
        )

        val states = mutableListOf<ArtistDetailUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        val loaded = states.last()
        assertEquals(false, loaded.isLoading)
        assertNotNull(loaded.artist)
        assertEquals("Lila Downs", loaded.artist!!.name)
        assertEquals(2, loaded.artist!!.albums.size)

        job.cancel()
    }

    @Test
    fun uiState_setsArtistNull_whenRepositoryReturnsNull() = runTest {
        val viewModel = ArtistDetailViewModel(
            artistId = 99L,
            observeArtistDetailUseCase = ObserveArtistDetailUseCase(FakeRepo(emptyMap())),
        )

        val states = mutableListOf<ArtistDetailUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }

        testScheduler.advanceUntilIdle()

        val loaded = states.last()
        assertEquals(false, loaded.isLoading)
        assertNull(loaded.artist)

        job.cancel()
    }

    @Test
    fun uiState_initialState_isLoading() = runTest {
        val viewModel = ArtistDetailViewModel(
            artistId = 1L,
            observeArtistDetailUseCase = ObserveArtistDetailUseCase(FakeRepo(emptyMap())),
        )

        // value is the initial state (no subscriber has triggered upstream yet)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.artist)
    }
}
