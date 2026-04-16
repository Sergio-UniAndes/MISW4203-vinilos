package com.misw4203.vinilos.feature.auth.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.permissions.PermissionsPolicy
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSessionRepository(
    private val dataStore: DataStore<Preferences>,
    private val permissionsPolicy: PermissionsPolicy,
) : SessionRepository {

    private val selectedRoleKey = stringPreferencesKey("selected_role")

    override fun observeSession(): Flow<UserSession?> {
        return dataStore.data.map { preferences ->
            val rawRole = preferences[selectedRoleKey] ?: return@map null
            val role = runCatching { UserRole.valueOf(rawRole) }.getOrNull() ?: return@map null
            UserSession(
                role = role,
                permissions = permissionsPolicy.permissionsFor(role),
            )
        }
    }

    override suspend fun saveRole(role: UserRole) {
        dataStore.edit { preferences ->
            preferences[selectedRoleKey] = role.name
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(selectedRoleKey)
        }
    }
}

