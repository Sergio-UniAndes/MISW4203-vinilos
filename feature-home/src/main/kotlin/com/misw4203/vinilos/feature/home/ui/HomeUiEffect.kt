package com.misw4203.vinilos.feature.home.ui

sealed interface HomeUiEffect {
    data class ShowMessage(val message: String) : HomeUiEffect
    data object NavigateAuth : HomeUiEffect
}

