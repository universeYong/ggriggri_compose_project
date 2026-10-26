package com.ahn.domain.repository

interface FCMRepository {
    suspend fun getCurrentToken(): String?
    suspend fun refreshToken()
    suspend fun saveTokenToServer(token: String)

    // 로그인 직후 호출: 현재 기기 토큰을 가져와 userId 사용자 문서에 저장한다.
    suspend fun sendTokenToServer(userId: String)

    // 로그아웃 시 호출: 현재 사용자 문서에서 이 기기의 토큰을 지우고, 기기의 토큰도 폐기한다.
    // 이렇게 해야 로그아웃한 기기(또는 같은 기기의 다른 계정)로 이전 계정 알림이 가지 않는다.
    suspend fun unregisterCurrentUserToken()
}
