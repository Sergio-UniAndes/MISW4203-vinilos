package com.misw4203.vinilos.feature.auth.domain

import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.repository.SessionRepository

class SelectRoleUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(role: UserRole) {
        sessionRepository.saveRole(role)
    }
}

