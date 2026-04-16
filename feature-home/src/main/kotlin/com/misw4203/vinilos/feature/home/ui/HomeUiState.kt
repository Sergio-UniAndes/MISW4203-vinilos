package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.feature.home.domain.model.HomeItem

data class HomeUiState(
    val isLoading: Boolean = true,
    val session: UserSession? = null,
    val items: List<HomeItem> = emptyList(),
) {
    val permissions: RolePermissions
        get() = session?.permissions ?: RolePermissions()
}

