package com.misw4203.vinilos.core.utils.model

data class RolePermissions(
    val canView: Boolean = true,
    val canCreate: Boolean = false,
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
)

