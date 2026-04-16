package com.misw4203.vinilos.core.utils.repository

import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSession(): Flow<UserSession?>
    suspend fun saveRole(role: UserRole)
    suspend fun clearSession()
}

