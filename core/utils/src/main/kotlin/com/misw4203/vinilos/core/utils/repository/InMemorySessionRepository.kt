package com.misw4203.vinilos.core.utils.repository

import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.permissions.PermissionsPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySessionRepository(
    private val permissionsPolicy: PermissionsPolicy,
) : SessionRepository {

    private val session = MutableStateFlow<UserSession?>(null)

    override fun observeSession(): Flow<UserSession?> = session.asStateFlow()

    override suspend fun saveRole(role: UserRole) {
        session.value = UserSession(
            role = role,
            permissions = permissionsPolicy.permissionsFor(role),
        )
    }

    override suspend fun clearSession() {
        session.value = null
    }
}

