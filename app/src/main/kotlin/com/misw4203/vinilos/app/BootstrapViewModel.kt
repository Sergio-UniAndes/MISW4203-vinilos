package com.misw4203.vinilos.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.core.navigation.AppRoute
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class BootstrapUiState(
    val targetRoute: String? = null,
    val isReady: Boolean = false,
)

class BootstrapViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {

    val uiState: StateFlow<BootstrapUiState> = observeSessionUseCase()
        .map { session ->
            BootstrapUiState(
                targetRoute = if (session == null) AppRoute.Auth else AppRoute.Home,
                isReady = true,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BootstrapUiState(),
        )
}

