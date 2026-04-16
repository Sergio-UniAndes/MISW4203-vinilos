package com.misw4203.vinilos.feature.auth.ui

import com.misw4203.vinilos.core.utils.model.UserRole

data class AuthUiState(
    val selectedRole: UserRole? = null,
    val isSubmitting: Boolean = false,
) {
    val canContinue: Boolean
        get() = selectedRole != null && !isSubmitting
}

