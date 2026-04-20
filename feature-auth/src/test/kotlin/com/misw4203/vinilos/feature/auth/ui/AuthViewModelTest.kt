package com.misw4203.vinilos.feature.auth.ui

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.feature.auth.domain.SelectRoleUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule: TestWatcher = MainDispatcherRule()

    @Test
    fun onRoleSelected_updatesState() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = AuthViewModel(SelectRoleUseCase(repository))

        viewModel.onRoleSelected(UserRole.VISITOR)

        assertEquals(UserRole.VISITOR, viewModel.uiState.value.selectedRole)
        assertTrue(viewModel.uiState.value.canContinue)
    }

    @Test
    fun onGetStarted_withoutRole_doesNothing() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = AuthViewModel(SelectRoleUseCase(repository))

        viewModel.onGetStarted()
        advanceUntilIdle()

        assertTrue(repository.savedRoles.isEmpty())
        assertNull(repository.session.value)
    }

    @Test
    fun onGetStarted_withRole_savesRoleAndEmitsNavigateHome() = runTest {
        val repository = FakeSessionRepository()
        val viewModel = AuthViewModel(SelectRoleUseCase(repository))
        val effectDeferred = async { viewModel.effects.first() }

        viewModel.onRoleSelected(UserRole.COLLECTOR)
        viewModel.onGetStarted()
        advanceUntilIdle()

        assertEquals(listOf(UserRole.COLLECTOR), repository.savedRoles)
        assertEquals(AuthUiEffect.NavigateHome, effectDeferred.await())
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun onGetStarted_whileSubmitting_ignoresSecondRequest() = runTest {
        val saveGate = CompletableDeferred<Unit>()
        val repository = FakeSessionRepository(saveGate = saveGate)
        val viewModel = AuthViewModel(SelectRoleUseCase(repository))

        viewModel.onRoleSelected(UserRole.COLLECTOR)
        viewModel.onGetStarted()
        repository.firstSaveStarted.await()

        assertTrue(viewModel.uiState.value.isSubmitting)

        viewModel.onGetStarted()
        advanceUntilIdle()

        assertEquals(1, repository.savedRoles.size)

        saveGate.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
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
    private val saveGate: CompletableDeferred<Unit>? = null,
) : SessionRepository {

    val session = MutableStateFlow<UserSession?>(null)
    val savedRoles = mutableListOf<UserRole>()
    val firstSaveStarted = CompletableDeferred<Unit>()

    override fun observeSession(): Flow<UserSession?> = session

    override suspend fun saveRole(role: UserRole) {
        savedRoles += role
        firstSaveStarted.complete(Unit)
        saveGate?.await()
        session.value = UserSession(role = role, permissions = permissionsFor(role))
    }

    override suspend fun clearSession() {
        session.value = null
    }

    private fun permissionsFor(role: UserRole): RolePermissions = when (role) {
        UserRole.VISITOR -> RolePermissions(canView = true)
        UserRole.COLLECTOR -> RolePermissions(canView = true, canCreate = true, canEdit = true, canDelete = true)
    }
}

