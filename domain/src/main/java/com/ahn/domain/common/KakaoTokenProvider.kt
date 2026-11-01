package com.ahn.domain.common

/**
 * 카카오 SDK가 보관 중인 로그인 토큰으로 새 access token을 얻는 계약(구현은 SDK를 쓰는 feature 모듈).
 * 로컬 세션(SessionManager)의 토큰은 로그인 시점 값이라 몇 시간 뒤에는 만료되므로, 조용한 재인증에는 이쪽을 우선 쓴다.
 */
interface KakaoTokenProvider {
    /** SDK에 토큰이 없거나 갱신에 실패하면 null. 코루틴 취소는 그대로 던진다. */
    suspend fun refreshAccessToken(): String?
}
