package com.ahn.domain.common

import com.ahn.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SessionManager {
    val currentUserFlow: Flow<User?>
    val isLoggedInFlow: Flow<Boolean>
    val currentUserGroupIdFlow: Flow<String?>

    /**
     * 로그인 직후(카카오/개발용 로그인)에만 쓴다. user.userAutoLoginToken이 세션 토큰으로 저장되며,
     * 비어 있으면 세션을 지우고 로그아웃 상태가 된다.
     */
    suspend fun loginUser(user: User)

    /**
     * 이미 로그인된 세션의 사용자 정보만 갱신한다(그룹 참여/생성/탈퇴 후 서버에서 다시 읽은 User 등).
     * 로컬 세션 토큰은 그대로 유지하고 user.userAutoLoginToken 값은 무시한다.
     * 서버(Firestore)에는 토큰을 저장하지 않으므로, 서버에서 읽은 User는 반드시 이 메서드로 반영해야 한다.
     * 로그인 상태가 아니면 아무것도 하지 않는다.
     */
    suspend fun updateUser(user: User)

    suspend fun logoutUser()
    suspend fun getTokenOnce(): String?
    suspend fun getCurrentUserIdOnce(): String?
    suspend fun updateUserProfile(newName: String, newProfileImage: String)
    suspend fun refreshUserInfo()
}