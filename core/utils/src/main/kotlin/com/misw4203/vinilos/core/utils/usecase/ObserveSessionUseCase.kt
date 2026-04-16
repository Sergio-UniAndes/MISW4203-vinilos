package com.misw4203.vinilos.core.utils.usecase

import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<UserSession?> = sessionRepository.observeSession()
}

