package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.core.utils.usecase.ClearSessionUseCase
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveHomeItemsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule: TestWatcher = MainDispatcherRule()

    @Test
    fun initialState_usesSessionAndItems() = runTest {
        val sessionRepository = FakeSessionRepository(
            initialSession = UserSession(
                role = UserRole.COLLECTOR,
                permissions = RolePermissions(canView = true, canCreate = true, canEdit = true, canDelete = true),
            ),
        )
        val homeRepository = FakeHomeRepository(sampleItems())
        val viewModel = buildViewModel(sessionRepository, homeRepository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UserRole.COLLECTOR, state.session?.role)
        assertEquals(HomeFilter.RECENTLY_ADDED, state.selectedFilter)
        assertEquals("Newest", state.featuredItem?.title)
        assertEquals(3, state.totalCount)

        collectJob.cancel()
    }

    @Test
    fun onFilterSelected_appliesGenreFilter() = runTest {
        val sessionRepository = FakeSessionRepository()
        val homeRepository = FakeHomeRepository(sampleItems())
        val viewModel = buildViewModel(sessionRepository, homeRepository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.onFilterSelected(HomeFilter.ROCK)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HomeFilter.ROCK, state.selectedFilter)
        assertEquals(2, state.totalCount)
        assertEquals("Newest", state.featuredItem?.title)
        assertEquals(1, state.gridItems.size)

        collectJob.cancel()
    }

    @Test
    fun onTabSelected_updatesSelectedTab() = runTest {
        val viewModel = buildViewModel(FakeSessionRepository(), FakeHomeRepository(sampleItems()))
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.onTabSelected(HomeTab.ARTISTS)
        advanceUntilIdle()

        assertEquals(HomeTab.ARTISTS, viewModel.uiState.value.selectedTab)

        collectJob.cancel()
    }

    @Test
    fun onChangeProfile_clearsSessionAndEmitsNavigateAuth() = runTest {
        val sessionRepository = FakeSessionRepository(
            initialSession = UserSession(UserRole.VISITOR, RolePermissions(canView = true)),
        )
        val viewModel = buildViewModel(sessionRepository, FakeHomeRepository(sampleItems()))
        val collectStateJob = backgroundScope.launch { viewModel.uiState.collect { } }
        val effectDeferred = async { viewModel.effects.first() }

        viewModel.onChangeProfile()
        advanceUntilIdle()

        assertNull(sessionRepository.session.value)
        assertEquals(1, sessionRepository.clearCalls)
        assertEquals(HomeUiEffect.NavigateAuth, effectDeferred.await())

        collectStateJob.cancel()
    }

    @Test
    fun onSearchClick_emitsMessageEffect() = runTest {
        val viewModel = buildViewModel(FakeSessionRepository(), FakeHomeRepository(sampleItems()))
        val effectDeferred = async { viewModel.effects.first() }

        viewModel.onSearchClick()
        advanceUntilIdle()

        assertEquals(HomeUiEffect.ShowMessage("Search coming soon"), effectDeferred.await())
    }

    private fun buildViewModel(
        sessionRepository: FakeSessionRepository,
        homeRepository: FakeHomeRepository,
    ): HomeViewModel {
        return HomeViewModel(
            observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
            observeHomeItemsUseCase = ObserveHomeItemsUseCase(homeRepository),
            clearSessionUseCase = ClearSessionUseCase(sessionRepository),
        )
    }

    private fun sampleItems(): List<HomeItem> = listOf(
        HomeItem(id = "1", title = "Classic Jazz", artist = "Blue Trio", year = 1998, genre = "Jazz"),
        HomeItem(id = "2", title = "Old Rock", artist = "Red Stone", year = 1980, genre = "Rock"),
        HomeItem(id = "3", title = "Newest", artist = "Future Band", year = 2024, genre = "Rock"),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
private class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeSessionRepository(
    initialSession: UserSession? = null,
) : SessionRepository {

    val session = MutableStateFlow(initialSession)
    var clearCalls: Int = 0

    override fun observeSession(): Flow<UserSession?> = session

    override suspend fun saveRole(role: UserRole) {
        session.value = UserSession(role = role, permissions = RolePermissions(canView = true))
    }

    override suspend fun clearSession() {
        clearCalls += 1
        session.value = null
    }
}

private class FakeHomeRepository(
    items: List<HomeItem>,
) : HomeRepository {

    private val data = MutableStateFlow(items)

    override fun observeItems(): Flow<List<HomeItem>> = data
}

