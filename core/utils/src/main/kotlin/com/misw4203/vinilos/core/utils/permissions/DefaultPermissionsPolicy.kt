package com.misw4203.vinilos.core.utils.permissions

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole

class DefaultPermissionsPolicy : PermissionsPolicy {
    override fun permissionsFor(role: UserRole): RolePermissions {
        return when (role) {
            UserRole.VISITOR -> RolePermissions(
                canView = true,
                canCreate = false,
                canEdit = false,
                canDelete = false,
            )

            UserRole.COLLECTOR -> RolePermissions(
                canView = true,
                canCreate = true,
                canEdit = true,
                canDelete = true,
            )
        }
    }
}

