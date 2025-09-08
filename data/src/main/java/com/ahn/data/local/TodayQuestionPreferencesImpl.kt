package com.ahn.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.ahn.data.local.util.DataStoreKeys
import com.ahn.data.local.util.dataStore
import com.ahn.domain.common.TodayQuestionPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class TodayQuestionPreferencesImpl(
    private val context: Context
): TodayQuestionPreferences {
    override val todayQuestionIdFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            Log.e("TodayQuestionPrefs", "Error reading today_question_id", exception)
            if (exception is IOException){
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map{ preferences ->
            preferences[DataStoreKeys.TODAY_QUESTION_ID]
        }

    override suspend fun saveTodayQuestionId(questionId: String?) {
        runCatching {
            context.dataStore.edit { settings ->
                if (questionId != null) {
                    settings[DataStoreKeys.TODAY_QUESTION_ID] = questionId
                    Log.d("TodayQuestionPrefs", "Saved today_question_id: $questionId")
                } else {
                    settings.remove(DataStoreKeys.TODAY_QUESTION_ID)
                    Log.d("TodayQuestionPrefs", "Cleared today_question_id")
                }
            }
        }.getOrElse { exception ->
            Log.e("TodayQuestionPrefs", "Error saving today_question_id", exception)
        }
    }

    override suspend fun clearTodayQuestionId() {
        saveTodayQuestionId(null)
    }
}