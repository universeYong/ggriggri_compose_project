package com.ahn.domain.common

import com.ahn.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SessionManager {
    val currentUserFlow: Flow<User?>
    val isLoggedInFlow: Flow<Boolean>
    val currentUserGroupIdFlow: Flow<String?> // 예시: 그룹 ID Flow

    suspend fun loginUser(user: User)
    suspend fun logoutUser()
    suspend fun getTokenOnce(): String?
    suspend fun getCurrentUserIdOnce(): String? // 필요한 경우
    suspend fun updateUserProfile(newName: String, newProfileImage: String)
}