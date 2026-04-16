package com.misw4203.vinilos.feature.auth.data.repository

import android.content.Context
import com.misw4203.vinilos.core.utils.permissions.PermissionsPolicy
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.feature.auth.data.local.sessionDataStore

fun provideSessionRepository(
    context: Context,
    permissionsPolicy: PermissionsPolicy,
): SessionRepository {
    return LocalSessionRepository(
        dataStore = context.sessionDataStore,
        permissionsPolicy = permissionsPolicy,
    )
}

