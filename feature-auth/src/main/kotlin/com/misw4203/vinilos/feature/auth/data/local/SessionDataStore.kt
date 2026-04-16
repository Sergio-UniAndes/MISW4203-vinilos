package com.misw4203.vinilos.feature.auth.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.sessionDataStore by preferencesDataStore(name = "session_preferences")

