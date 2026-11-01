package com.ahn.ggriggri.testutil

import com.ahn.domain.common.KakaoTokenProvider
import com.ahn.domain.repository.AuthRepository

/** Firebase 없이 로그인 결과와 호출을 기록하는 AuthRepository. */
class FakeAuthRepository(var signedIn: Boolean = false) : AuthRepository {
    /** signInWithKakaoToken의 결과. 성공이면 signedIn이 true가 된다. */
    var signInResult: Result<Unit> = Result.success(Unit)

    val signInTokens = mutableListOf<String>()
    var signOutCalls = 0

    override suspend fun signInWithKakaoToken(kakaoAccessToken: String): Result<Unit> {
        signInTokens += kakaoAccessToken
        if (signInResult.isSuccess) signedIn = true
        return signInResult
    }

    override suspend fun signOut() {
        signOutCalls++
        signedIn = false
    }

    override fun isSignedIn(): Boolean = signedIn
}

class FakeKakaoTokenProvider(var token: String? = null) : KakaoTokenProvider {
    var refreshCalls = 0

    override suspend fun refreshAccessToken(): String? {
        refreshCalls++
        return token
    }
}
