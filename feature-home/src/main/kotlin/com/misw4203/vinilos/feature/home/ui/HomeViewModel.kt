package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.usecase.ClearSessionUseCase
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveHomeItemsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
    observeHomeItemsUseCase: ObserveHomeItemsUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        observeSessionUseCase(),
        observeHomeItemsUseCase(),
    ) { session: UserSession?, items: List<HomeItem> ->
        HomeUiState(
            isLoading = false,
            session = session,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private val _effects = MutableSharedFlow<HomeUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onEditItem(item: HomeItem) {
        emitMessage("Editar ${item.title}")
    }

    fun onDeleteItem(item: HomeItem) {
        emitMessage("Eliminar ${item.title}")
    }

    fun onCreateItem() {
        emitMessage("Crear nuevo elemento")
    }

    fun onChangeProfile() {
        viewModelScope.launch {
            clearSessionUseCase()
            _effects.emit(HomeUiEffect.NavigateAuth)
        }
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _effects.emit(HomeUiEffect.ShowMessage(message))
        }
    }
}

