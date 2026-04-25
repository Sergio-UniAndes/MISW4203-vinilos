package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule: TestWatcher = AlbumDetailMainDispatcherRule()

    @Test
    fun initialState_isLoadingWithNoAlbum() {
        val repository = FakeAlbumRepository(MutableStateFlow(null))
        val viewModel = AlbumDetailViewModel("1", ObserveAlbumDetailUseCase(repository))

        val initial = viewModel.uiState.value
        assertTrue(initial.isLoading)
        assertNull(initial.album)
    }

    @Test
    fun whenAlbumEmits_stateExposesAlbumAndStopsLoading() = runTest {
        val flow = MutableStateFlow<HomeItem?>(null)
        val repository = FakeAlbumRepository(flow)
        val viewModel = AlbumDetailViewModel("42", ObserveAlbumDetailUseCase(repository))
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        flow.value = sampleAlbum()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("42", state.album?.id)
        assertEquals("Sample", state.album?.title)
        assertEquals("42", repository.requestedIds.last())

        collectJob.cancel()
    }

    @Test
    fun whenAlbumIsMissing_stateExposesNullAlbum() = runTest {
        val repository = FakeAlbumRepository(MutableStateFlow<HomeItem?>(null))
        val viewModel = AlbumDetailViewModel("missing", ObserveAlbumDetailUseCase(repository))
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.album)

        collectJob.cancel()
    }

    private fun sampleAlbum(): HomeItem = HomeItem(
        id = "42",
        title = "Sample",
        artist = "Artist",
        year = 2020,
        genre = "Rock",
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
private class AlbumDetailMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeAlbumRepository(
    private val flow: MutableStateFlow<HomeItem?>,
) : HomeRepository {
    val requestedIds = mutableListOf<String>()

    override fun observeItems(): Flow<List<HomeItem>> = MutableStateFlow(emptyList())

    override fun observeItem(id: String): Flow<HomeItem?> {
        requestedIds += id
        return flow
    }
}
