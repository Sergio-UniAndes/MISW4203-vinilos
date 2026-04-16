package com.misw4203.vinilos.core.utils.model

data class UserSession(
    val role: UserRole,
    val permissions: RolePermissions,
)

