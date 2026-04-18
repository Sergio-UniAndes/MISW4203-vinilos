package com.misw4203.vinilos.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.feature.auth.domain.SelectRoleUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val selectRoleUseCase: SelectRoleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AuthUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AuthUiEffect> = _effects.asSharedFlow()

    fun onRoleSelected(role: UserRole) {
        _uiState.update { currentState ->
            currentState.copy(selectedRole = role)
        }
    }

    fun onRoleSelectedAndContinue(role: UserRole) {
        onRoleSelected(role)
        onGetStarted()
    }

    fun onGetStarted() {
        if (_uiState.value.isSubmitting) return
        val selectedRole = _uiState.value.selectedRole ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                selectRoleUseCase(selectedRole)
                _effects.tryEmit(AuthUiEffect.NavigateHome)
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}

