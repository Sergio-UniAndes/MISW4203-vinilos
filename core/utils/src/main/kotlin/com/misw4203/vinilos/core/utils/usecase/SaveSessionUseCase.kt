package com.misw4203.vinilos.core.utils.usecase

import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.repository.SessionRepository

class SaveSessionUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(role: UserRole) {
        sessionRepository.saveRole(role)
    }
}

