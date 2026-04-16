package com.misw4203.vinilos.core.utils.permissions

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole

interface PermissionsPolicy {
    fun permissionsFor(role: UserRole): RolePermissions
}

