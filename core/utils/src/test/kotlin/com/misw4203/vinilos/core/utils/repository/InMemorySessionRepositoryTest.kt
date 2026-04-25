package com.misw4203.vinilos.core.utils.repository

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.permissions.PermissionsPolicy
import com.misw4203.vinilos.core.utils.usecase.ClearSessionUseCase
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.core.utils.usecase.SaveSessionUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemorySessionRepositoryTest {

    private val visitorPermissions = RolePermissions(canView = true)
    private val collectorPermissions = RolePermissions(
        canView = true,
        canCreate = true,
        canEdit = true,
        canDelete = true,
    )

    private val policy = object : PermissionsPolicy {
        override fun permissionsFor(role: UserRole): RolePermissions = when (role) {
            UserRole.VISITOR -> visitorPermissions
            UserRole.COLLECTOR -> collectorPermissions
        }
    }

    @Test
    fun observeSession_initiallyNull() = runTest {
        val repository = InMemorySessionRepository(policy)

        assertNull(repository.observeSession().first())
    }

    @Test
    fun saveRole_storesSessionWithPolicyPermissions() = runTest {
        val repository = InMemorySessionRepository(policy)

        repository.saveRole(UserRole.COLLECTOR)

        val session = repository.observeSession().first()
        assertEquals(UserRole.COLLECTOR, session?.role)
        assertEquals(collectorPermissions, session?.permissions)
    }

    @Test
    fun saveRole_overwritesPreviousSession() = runTest {
        val repository = InMemorySessionRepository(policy)

        repository.saveRole(UserRole.VISITOR)
        repository.saveRole(UserRole.COLLECTOR)

        val session = repository.observeSession().first()
        assertEquals(UserRole.COLLECTOR, session?.role)
        assertEquals(collectorPermissions, session?.permissions)
    }

    @Test
    fun clearSession_removesActiveSession() = runTest {
        val repository = InMemorySessionRepository(policy)
        repository.saveRole(UserRole.VISITOR)

        repository.clearSession()

        assertNull(repository.observeSession().first())
    }

    @Test
    fun saveSessionUseCase_delegatesToRepository() = runTest {
        val repository = InMemorySessionRepository(policy)
        val useCase = SaveSessionUseCase(repository)

        useCase(UserRole.VISITOR)

        assertEquals(UserRole.VISITOR, repository.observeSession().first()?.role)
    }

    @Test
    fun observeSessionUseCase_emitsRepositoryUpdates() = runTest {
        val repository = InMemorySessionRepository(policy)
        val useCase = ObserveSessionUseCase(repository)
        repository.saveRole(UserRole.COLLECTOR)

        assertEquals(UserRole.COLLECTOR, useCase().first()?.role)
    }

    @Test
    fun clearSessionUseCase_clearsRepository() = runTest {
        val repository = InMemorySessionRepository(policy)
        repository.saveRole(UserRole.COLLECTOR)
        val useCase = ClearSessionUseCase(repository)

        useCase()

        assertNull(repository.observeSession().first())
    }
}
