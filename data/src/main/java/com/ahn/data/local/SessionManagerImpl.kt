package com.ahn.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.ahn.data.local.util.DataStoreKeys
import com.ahn.data.local.util.dataStore
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class SessionManagerImpl(
    private val context: Context,
    private val moshi: Moshi,
) : SessionManager {
    companion object {

        // Singleton 인스턴스 관리 (수동)
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context, moshi: Moshi): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManagerImpl(context.applicationContext, moshi).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val userJsonAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)

    override val currentUserFlow: Flow<User?> = context.dataStore.data
        .catch { exception ->
            Log.e("SessionManager", "Error reading from DataStore for currentUserFlow", exception)
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val userJson = preferences[DataStoreKeys.USER_OBJECT_JSON]
            if (userJson != null) {
                runCatching {
                    val user = userJsonAdapter.fromJson(userJson)
                    Log.d(
                        "SessionManager",
                        "currentUserFlow emitted user: ${user?.userId}"
                    ) // User 객체 emit 로그
                    user
                }.getOrNull()
            } else {
                Log.d("SessionManager", "currentUserFlow emitted null user") // null User 객체 emit 로그
                null
            }
        }

    override val isLoggedInFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DataStoreKeys.IS_LOGGED_IN] ?: false
        }

    override val currentUserGroupIdFlow: Flow<String?> = context.dataStore.data // 그룹 ID Flow
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DataStoreKeys.USER_GROUP_ID]
        }

    override suspend fun loginUser(user: User) {
        Log.d(
            "SessionManager",
            "loginUser called with user: ${user.userId}, token: ${user.userAutoLoginToken}"
        ) // 함수 진입 로그
        val tokenToSave = user.userAutoLoginToken
        if (tokenToSave.isBlank()) {
            Log.w(
                "SessionManager",
                "Token is blank, loginUser will not save session."
            ) // 토큰 비어있을 경우 로그
            // 예외 처리 로직
            return
        }
        runCatching {
            context.dataStore.edit { settings ->
                Log.d("SessionManager", "DataStore.edit block ENTERED for user: ${user.userId}") // <--- (2) edit 블록 진입 확인
                val userJson = userJsonAdapter.toJson(user)
                Log.d("SessionManager", "User JSON to save: $userJson") // <--- (3) 생성된 JSON 확인
                settings[DataStoreKeys.USER_OBJECT_JSON] = userJson
                settings[DataStoreKeys.USER_TOKEN] = tokenToSave
                settings[DataStoreKeys.IS_LOGGED_IN] = true
                if (!user.userGroupDocumentId.isNullOrBlank()) {
                    settings[DataStoreKeys.USER_GROUP_ID] = user.userGroupDocumentId
                }
                Log.d(
                    "SessionManager",
                    "User session saved to DataStore for user: ${user.userId}"
                ) // DataStore 저장 성공 로그
            }
            Log.d("SessionManager", "DataStore.edit block EXITED NORMALLY for user: ${user.userId}") // <--- (6) edit 블록 정상 종료 확인
        }.getOrElse { exception -> throw exception
            Log.e("SessionManager", "Failed to save user session to DataStore", exception) // <--- (7) 예외 발생 시 확인
        }
    }

    override suspend fun logoutUser() {
        context.dataStore.edit { settings ->
            settings.remove(DataStoreKeys.USER_OBJECT_JSON)
            settings.remove(DataStoreKeys.USER_TOKEN)
            settings.remove(DataStoreKeys.USER_GROUP_ID)
            settings[DataStoreKeys.IS_LOGGED_IN] = false
        }
    }

    override suspend fun getTokenOnce(): String? {
        return runCatching {
            context.dataStore.data
                .map { preferences -> preferences[DataStoreKeys.USER_TOKEN] }
                .first()
        }.getOrNull()
    }

    override suspend fun getCurrentUserIdOnce(): String? {
        return runCatching {
            currentUserFlow.first()?.userId
        }.getOrNull()
    }

    override suspend fun updateUserProfile(newName: String, newProfileImage: String) {
        context.dataStore.edit { settings ->
            val currentUserJson = settings[DataStoreKeys.USER_OBJECT_JSON]
            if (currentUserJson != null) {
                runCatching {
                    val currentUser = userJsonAdapter.fromJson(currentUserJson)
                    if (currentUser != null) {
                        val updateUser = currentUser.copy(
                            userName = newName,
                            userProfileImage = newProfileImage
                        )
                        settings[DataStoreKeys.USER_OBJECT_JSON] =
                            userJsonAdapter.toJson(updateUser)
                    }
                }.getOrElse { exception ->
                    throw exception
                    // TODO: 오류 로깅
                }
            } // else: 현재 사용자 정보가 없는 경우 (로그아웃 상태 등) 프로필 업데이트 시도.
            // 이 경우에 대한 정책을 정해야 함 (오류로 처리할지, 무시할지 등)
        }
    }
}