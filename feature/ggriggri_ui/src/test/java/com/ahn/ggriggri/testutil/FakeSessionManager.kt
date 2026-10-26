package com.ahn.ggriggri.testutil

import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionManager(initialUser: User? = null) : SessionManager {
    val userState = MutableStateFlow(initialUser)
    val updatedUsers = mutableListOf<User>()

    override val currentUserFlow: Flow<User?> = userState
    override val isLoggedInFlow: Flow<Boolean> = userState.map { it != null }
    override val currentUserGroupIdFlow: Flow<String?> = userState.map { it?.userGroupDocumentId }

    override suspend fun loginUser(user: User) {
        userState.value = user
    }

    override suspend fun updateUser(user: User) {
        if (userState.value == null) return
        updatedUsers += user
        userState.value = user
    }

    override suspend fun logoutUser() {
        userState.value = null
    }

    override suspend fun getTokenOnce(): String? = null
    override suspend fun getCurrentUserIdOnce(): String? = userState.value?.userId
    override suspend fun updateUserProfile(newName: String, newProfileImage: String) = Unit
    override suspend fun refreshUserInfo() = Unit
}
