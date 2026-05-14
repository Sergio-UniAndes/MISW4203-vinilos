package com.misw4203.vinilos.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.feature.home.domain.model.Collector
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCollectorsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CollectorsUiState(
    val isLoading: Boolean = true,
    val collectors: List<Collector> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
)

class CollectorsViewModel(
    observeCollectorsUseCase: ObserveCollectorsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<CollectorsUiState> = combine(
        observeCollectorsUseCase(),
        query,
    ) { all, currentQuery ->
        val trimmed = currentQuery.trim()
        val filtered = if (trimmed.isEmpty()) {
            all
        } else {
            all.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
        CollectorsUiState(
            isLoading = false,
            collectors = filtered,
            totalCount = all.size,
            query = currentQuery,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectorsUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }
}
