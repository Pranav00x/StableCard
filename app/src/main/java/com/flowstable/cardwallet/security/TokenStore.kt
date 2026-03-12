package com.flowstable.cardwallet.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flowstable.cardwallet.model.AuthTokens
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("session")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val expiryKey = longPreferencesKey("access_expiry")

    suspend fun saveTokens(tokens: AuthTokens) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = tokens.accessToken
            prefs[refreshTokenKey] = tokens.refreshToken
            prefs[expiryKey] = tokens.expiresAtEpochSeconds
        }
    }

    suspend fun getAccessToken(): String? {
        val prefs = context.dataStore.data.first()
        val expiry = prefs[expiryKey] ?: return null
        if (Instant.now().epochSecond >= expiry) return null
        return prefs[accessTokenKey]
    }

    suspend fun getRefreshToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[refreshTokenKey]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

