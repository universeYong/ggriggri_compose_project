package com.ahn.ggriggri.auth

import com.ahn.domain.common.KakaoTokenProvider
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.AuthRepository
import com.ahn.domain.repository.KakaoTokenRejectedException
import jakarta.inject.Inject

/** [EnsureFirebaseAuthUseCase]의 결과. */
sealed interface EnsureAuthResult {
    /** 이미 Firebase Auth에 로그인되어 있다(새로 로그인하지 않음). */
    data object AlreadySignedIn : EnsureAuthResult

    /** 로컬에 남아 있던 카카오 세션으로 조용히 Firebase Auth 로그인에 성공했다. */
    data object SignedIn : EnsureAuthResult

    /** 카카오 토큰이 없거나 서버가 거부했다(만료). 사용자가 다시 로그인해야 한다. */
    data object NeedsRelogin : EnsureAuthResult

    /** 네트워크/서버 일시 오류. 세션은 유지하고 나중에 다시 시도할 수 있다. */
    data object TemporaryFailure : EnsureAuthResult
}

/**
 * 카카오 세션은 있는데 Firebase Auth는 로그아웃 상태인 경우(이 기능이 들어가기 전에 로그인한 기존 사용자 등)를
 * 조용히 복구한다. Firestore 접근 전에 한 번 호출한다.
 *
 * 토큰 선택: 카카오 SDK의 갱신된 access token을 우선 쓰고(로그인 시점에 저장해 둔 로컬 토큰은 대개 만료됨),
 * 갱신을 못 하면 로컬 세션 토큰을 마지막 수단으로 시도한다. 둘 다 없으면 [EnsureAuthResult.NeedsRelogin].
 * 개발용 로그인 세션에는 쓰지 않는다(카카오 토큰이 없어 항상 NeedsRelogin이 된다).
 */
class EnsureFirebaseAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val kakaoTokenProvider: KakaoTokenProvider,
) {
    suspend operator fun invoke(): EnsureAuthResult {
        if (authRepository.isSignedIn()) return EnsureAuthResult.AlreadySignedIn

        val token = kakaoTokenProvider.refreshAccessToken()?.takeIf { it.isNotBlank() }
            ?: sessionManager.getTokenOnce()?.takeIf { it.isNotBlank() }
            ?: return EnsureAuthResult.NeedsRelogin

        val result = authRepository.signInWithKakaoToken(token)
        return when {
            result.isSuccess -> EnsureAuthResult.SignedIn
            result.exceptionOrNull() is KakaoTokenRejectedException -> EnsureAuthResult.NeedsRelogin
            else -> EnsureAuthResult.TemporaryFailure
        }
    }
}
