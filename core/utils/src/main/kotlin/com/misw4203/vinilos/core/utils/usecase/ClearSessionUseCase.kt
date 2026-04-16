package com.misw4203.vinilos.core.utils.usecase

import com.misw4203.vinilos.core.utils.repository.SessionRepository

class ClearSessionUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() {
        sessionRepository.clearSession()
    }
}

