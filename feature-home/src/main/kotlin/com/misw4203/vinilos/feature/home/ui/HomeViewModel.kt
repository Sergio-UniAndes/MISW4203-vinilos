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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    observeSessionUseCase: ObserveSessionUseCase,
    observeHomeItemsUseCase: ObserveHomeItemsUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(HomeFilter.RECENTLY_ADDED)
    private val selectedTab = MutableStateFlow(HomeTab.ALBUMS)

    val uiState: StateFlow<HomeUiState> = combine(
        observeSessionUseCase(),
        observeHomeItemsUseCase(),
        selectedFilter,
        selectedTab,
    ) { session: UserSession?, items: List<HomeItem>, filter: HomeFilter, tab: HomeTab ->
        HomeUiState(
            isLoading = false,
            session = session,
            items = items,
            selectedFilter = filter,
            selectedTab = tab,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private val _effects = MutableSharedFlow<HomeUiEffect>()
    val effects = _effects.asSharedFlow()

    fun onFilterSelected(filter: HomeFilter) {
        selectedFilter.value = filter
    }

    fun onTabSelected(tab: HomeTab) {
        selectedTab.value = tab
    }

    fun onEditItem(item: HomeItem) {
        emitMessage("Edit ${item.title}")
    }

    fun onDeleteItem(item: HomeItem) {
        emitMessage("Delete ${item.title}")
    }

    fun onCreateItem() {
        emitMessage("Create new album")
    }

    fun onSearchClick() {
        emitMessage("Search coming soon")
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
