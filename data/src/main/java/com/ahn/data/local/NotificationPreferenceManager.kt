package com.ahn.data.local

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.ahn.data.local.util.DataStoreKeys
import com.ahn.data.local.util.dataStore
import com.ahn.domain.model.NotificationSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class NotificationPreferenceManager @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            NotificationSettings(
                dailyQuestionEnabled = preferences[DataStoreKeys.DAILY_QUESTION_ENABLED] ?: true,
                requestEnabled = preferences[DataStoreKeys.REQUEST_ENABLED] ?: true,
                responseEnabled = preferences[DataStoreKeys.RESPONSE_ENABLED] ?: true,
                allNotificationsEnabled = preferences[DataStoreKeys.ALL_NOTIFICATIONS_ENABLED] ?: true
            )
        }

    suspend fun isNotificationEnabled(type: String?): Boolean {
        return try {
            val settings = notificationSettings.first()

            if (!settings.allNotificationsEnabled) {
                return false
            }

            when (type) {
                "question", "daily_question" -> settings.dailyQuestionEnabled
                "request" -> settings.requestEnabled
                "response" -> settings.responseEnabled
                else -> true
            }
        } catch (e: Exception) {
            true
        }
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.DAILY_QUESTION_ENABLED] = settings.dailyQuestionEnabled
            preferences[DataStoreKeys.REQUEST_ENABLED] = settings.requestEnabled
            preferences[DataStoreKeys.RESPONSE_ENABLED] = settings.responseEnabled
            preferences[DataStoreKeys.ALL_NOTIFICATIONS_ENABLED] = settings.allNotificationsEnabled
        }
    }
}