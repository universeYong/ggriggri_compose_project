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
            // 사용자 JSON에는 세션 토큰이 들어 있으므로 로그로 남기지 않는다.
            val userJson = preferences[DataStoreKeys.USER_OBJECT_JSON]
            if (userJson != null) {
                runCatching {
                    userJsonAdapter.fromJson(userJson)
                }.getOrElse { exception ->
                    Log.e("SessionManager", "JSON 파싱 실패", exception)
                    null
                }
            } else {
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
            val groupId = preferences[DataStoreKeys.USER_GROUP_ID]
            Log.d("SessionManager", "currentUserGroupIdFlow emitted: $groupId")
            groupId
        }

    // 토큰과 사용자 JSON(토큰 포함)은 로그에 남기지 않는다.
    override suspend fun loginUser(user: User) {
        val tokenToSave = user.userAutoLoginToken
        if (tokenToSave.isBlank()) {
            Log.w("SessionManager", "loginUser: 토큰이 비어 있어 세션을 지우고 로그아웃 상태로 둔다.")
            context.dataStore.edit { settings ->
                settings[DataStoreKeys.IS_LOGGED_IN] = false
                settings.remove(DataStoreKeys.USER_OBJECT_JSON)
                settings.remove(DataStoreKeys.USER_TOKEN)
                settings.remove(DataStoreKeys.USER_GROUP_ID)
            }
            return
        }
        runCatching {
            context.dataStore.edit { settings ->
                settings[DataStoreKeys.USER_OBJECT_JSON] = userJsonAdapter.toJson(user)
                settings[DataStoreKeys.USER_TOKEN] = tokenToSave
                settings[DataStoreKeys.IS_LOGGED_IN] = true
                if (!user.userGroupDocumentId.isNullOrBlank()) {
                    settings[DataStoreKeys.USER_GROUP_ID] = user.userGroupDocumentId
                }
            }
        }.getOrElse { exception ->
            Log.e("SessionManager", "Failed to save user session to DataStore", exception)
            throw exception
        }
    }

    override suspend fun updateUser(user: User) {
        runCatching {
            context.dataStore.edit { settings ->
                val existingToken = settings[DataStoreKeys.USER_TOKEN]
                if (existingToken.isNullOrBlank() || settings[DataStoreKeys.IS_LOGGED_IN] != true) {
                    Log.w("SessionManager", "updateUser: 로그인 세션이 없어 갱신하지 않는다.")
                    return@edit
                }
                // 서버에서 읽은 User에는 토큰이 없으므로 로컬 세션 토큰을 그대로 유지한다.
                val merged = user.copy(userAutoLoginToken = existingToken)
                settings[DataStoreKeys.USER_OBJECT_JSON] = userJsonAdapter.toJson(merged)
                // 그룹 탈퇴처럼 그룹 ID가 비면 세션의 그룹 ID도 지운다.
                if (user.userGroupDocumentId.isNullOrBlank()) {
                    settings.remove(DataStoreKeys.USER_GROUP_ID)
                } else {
                    settings[DataStoreKeys.USER_GROUP_ID] = user.userGroupDocumentId
                }
            }
        }.getOrElse { exception ->
            Log.e("SessionManager", "Failed to update user session in DataStore", exception)
            throw exception
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

    override suspend fun refreshUserInfo() {
        // 사용자 정보를 다시 로드하여 currentUserFlow를 업데이트
        // 이 함수는 currentUserFlow를 관찰하는 모든 ViewModel에서 그룹 정보를 새로고침하게 함
        context.dataStore.edit { settings ->
            // DataStore를 다시 읽어서 currentUserFlow를 트리거
            val currentUserJson = settings[DataStoreKeys.USER_OBJECT_JSON]
            if (currentUserJson != null) {
                // 사용자 정보를 다시 파싱해서 저장하여 Flow를 트리거
                runCatching {
                    val user = userJsonAdapter.fromJson(currentUserJson)
                    if (user != null) {
                        // 사용자 정보를 다시 저장하여 Flow 업데이트 트리거
                        settings[DataStoreKeys.USER_OBJECT_JSON] = userJsonAdapter.toJson(user)
                    }
                }.getOrElse { exception ->
                    Log.e("SessionManager", "refreshUserInfo() - 사용자 정보 재저장 실패", exception)
                }
            }
        }
    }
}