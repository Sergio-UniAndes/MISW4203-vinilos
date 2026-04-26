package com.misw4203.vinilos.app

import com.misw4203.vinilos.core.navigation.AppRoute
import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
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
class BootstrapViewModelTest {

    @get:Rule
    val mainDispatcherRule: TestWatcher = BootstrapMainDispatcherRule()

    @Test
    fun initialState_isNotReady_andHasNoTarget() {
        val viewModel = BootstrapViewModel(ObserveSessionUseCase(FakeSessionRepository()))

        val initial = viewModel.uiState.value
        assertFalse(initial.isReady)
        assertNull(initial.targetRoute)
    }

    @Test
    fun whenSessionIsNull_targetIsAuth() = runTest {
        val repository = FakeSessionRepository(initialSession = null)
        val viewModel = BootstrapViewModel(ObserveSessionUseCase(repository))
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isReady)
        assertEquals(AppRoute.Auth, state.targetRoute)

        collectJob.cancel()
    }

    @Test
    fun whenSessionExists_targetIsHome() = runTest {
        val repository = FakeSessionRepository(
            initialSession = UserSession(
                role = UserRole.COLLECTOR,
                permissions = RolePermissions(canView = true, canCreate = true),
            ),
        )
        val viewModel = BootstrapViewModel(ObserveSessionUseCase(repository))
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isReady)
        assertEquals(AppRoute.Home, state.targetRoute)

        collectJob.cancel()
    }

    @Test
    fun targetRoute_updatesWhenSessionChanges() = runTest {
        val repository = FakeSessionRepository(initialSession = null)
        val viewModel = BootstrapViewModel(ObserveSessionUseCase(repository))
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        assertEquals(AppRoute.Auth, viewModel.uiState.value.targetRoute)

        repository.session.value = UserSession(
            role = UserRole.VISITOR,
            permissions = RolePermissions(canView = true),
        )
        advanceUntilIdle()
        assertEquals(AppRoute.Home, viewModel.uiState.value.targetRoute)

        repository.session.value = null
        advanceUntilIdle()
        assertEquals(AppRoute.Auth, viewModel.uiState.value.targetRoute)

        collectJob.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class BootstrapMainDispatcherRule(
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

    override fun observeSession(): Flow<UserSession?> = session

    override suspend fun saveRole(role: UserRole) {
        session.value = UserSession(role = role, permissions = RolePermissions(canView = true))
    }

    override suspend fun clearSession() {
        session.value = null
    }
}
