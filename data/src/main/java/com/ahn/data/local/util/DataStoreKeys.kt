package com.ahn.data.local.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val USER_TOKEN = stringPreferencesKey("user_auto_login_token")
    val USER_OBJECT_JSON = stringPreferencesKey("user_object_json") // User 객체 저장용 키
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_PROFILE_URL = stringPreferencesKey("user_profile_url")
    val USER_GROUP_ID = stringPreferencesKey("user_group_id")

    val TODAY_QUESTION_ID = stringPreferencesKey("today_question_id")
    val TODAY_QUESTION_CONTENT = stringPreferencesKey("today_question_content")
    val TODAY_QUESTION_URL = stringPreferencesKey("today_question_img_url")
}