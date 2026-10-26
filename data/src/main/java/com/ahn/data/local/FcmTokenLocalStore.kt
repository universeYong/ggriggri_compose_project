package com.ahn.data.local

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahn.data.local.util.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

/**
 * 이 기기가 마지막으로 서버(user_data._userFcmToken)에 등록한 FCM 토큰과 그 문서의 userId.
 * 토큰이 갱신되면 이전 토큰을 arrayRemove 하기 위해 기억해 둔다.
 * 세션 키(SessionManagerImpl.logoutUser가 지우는 키)와 분리되어 있어 로그아웃해도 남으므로,
 * 로그아웃 시에는 FCMTokenManager.unregisterToken이 직접 clear()를 호출한다.
 */
@Singleton
class FcmTokenLocalStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    data class RegisteredToken(val userId: String, val token: String)

    suspend fun get(): RegisteredToken? {
        val prefs = context.dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val userId = prefs[KEY_USER_ID]
        val token = prefs[KEY_TOKEN]
        return if (!userId.isNullOrBlank() && !token.isNullOrBlank()) {
            RegisteredToken(userId, token)
        } else {
            null
        }
    }

    suspend fun save(userId: String, token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_TOKEN)
        }
    }

    private companion object {
        val KEY_USER_ID = stringPreferencesKey("fcm_registered_user_id")
        val KEY_TOKEN = stringPreferencesKey("fcm_registered_token")
    }
}
