package com.ahn.domain.repository

/**
 * Firebase Auth 로그인 상태를 다루는 계약. Firestore/Storage 규칙이 request.auth.uid를 보므로
 * 모든 Firestore 접근 전에 로그인되어 있어야 한다. uid는 카카오 회원번호 문자열(= user_data 문서 ID)이다.
 */
interface AuthRepository {

    /**
     * 카카오 access token을 서버(Cloud Function)에 보내 Firebase custom token으로 교환하고 로그인한다.
     * 서버가 토큰을 거부하면 [KakaoTokenRejectedException]으로 실패하고, 네트워크 오류 등 그 외 실패는 다른 예외로 돌려준다.
     * 코루틴 취소는 Result로 감싸지 않고 그대로 던진다. 토큰은 로그에 남기지 않는다.
     */
    suspend fun signInWithKakaoToken(kakaoAccessToken: String): Result<Unit>

    suspend fun signOut()

    fun isSignedIn(): Boolean
}

/** 서버가 카카오 access token을 유효하지 않다고 판단했다(만료/위조). 재로그인이 필요하다. */
class KakaoTokenRejectedException(cause: Throwable? = null) :
    Exception("카카오 토큰이 거부되었습니다.", cause)
