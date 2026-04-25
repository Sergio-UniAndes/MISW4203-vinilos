package com.misw4203.vinilos.core.utils.permissions

import com.misw4203.vinilos.core.utils.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultPermissionsPolicyTest {

    private val policy = DefaultPermissionsPolicy()

    @Test
    fun visitor_hasReadOnlyPermissions() {
        val permissions = policy.permissionsFor(UserRole.VISITOR)

        assertTrue(permissions.canView)
        assertFalse(permissions.canCreate)
        assertFalse(permissions.canEdit)
        assertFalse(permissions.canDelete)
    }

    @Test
    fun collector_hasFullPermissions() {
        val permissions = policy.permissionsFor(UserRole.COLLECTOR)

        assertTrue(permissions.canView)
        assertTrue(permissions.canCreate)
        assertTrue(permissions.canEdit)
        assertTrue(permissions.canDelete)
    }

    @Test
    fun permissionsFor_isStable_acrossInvocations() {
        assertEquals(
            policy.permissionsFor(UserRole.VISITOR),
            policy.permissionsFor(UserRole.VISITOR),
        )
        assertEquals(
            policy.permissionsFor(UserRole.COLLECTOR),
            policy.permissionsFor(UserRole.COLLECTOR),
        )
    }
}
