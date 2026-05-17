package com.ahn.data.local.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val USER_TOKEN = stringPreferencesKey("user_auto_login_token")
    val USER_OBJECT_JSON = stringPreferencesKey("user_object_json")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_GROUP_ID = stringPreferencesKey("user_group_id")

    val TODAY_QUESTION_ID = stringPreferencesKey("today_question_id")

    val DAILY_QUESTION_ENABLED = booleanPreferencesKey("daily_question_enabled")
    val REQUEST_ENABLED = booleanPreferencesKey("request_enabled")
    val RESPONSE_ENABLED = booleanPreferencesKey("response_enabled")
    val ALL_NOTIFICATIONS_ENABLED = booleanPreferencesKey("all_notifications_enabled")
}