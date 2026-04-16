package com.misw4203.vinilos.feature.auth.ui

sealed interface AuthUiEffect {
    data object NavigateHome : AuthUiEffect
}

