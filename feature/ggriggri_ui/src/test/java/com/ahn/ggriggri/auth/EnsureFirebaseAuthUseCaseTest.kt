package com.ahn.ggriggri.auth

import com.ahn.domain.repository.KakaoTokenRejectedException
import com.ahn.ggriggri.testutil.FakeAuthRepository
import com.ahn.ggriggri.testutil.FakeKakaoTokenProvider
import com.ahn.ggriggri.testutil.FakeSessionManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class EnsureFirebaseAuthUseCaseTest {

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var sessionManager: FakeSessionManager
    private lateinit var tokenProvider: FakeKakaoTokenProvider
    private lateinit var useCase: EnsureFirebaseAuthUseCase

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository()
        sessionManager = FakeSessionManager()
        tokenProvider = FakeKakaoTokenProvider()
        useCase = EnsureFirebaseAuthUseCase(authRepository, sessionManager, tokenProvider)
    }

    @Test
    fun `이미 Auth에 로그인되어 있으면 토큰 갱신이나 로그인을 하지 않는다`() = runTest {
        authRepository.signedIn = true

        assertEquals(EnsureAuthResult.AlreadySignedIn, useCase())

        assertEquals(0, tokenProvider.refreshCalls)
        assertTrue(authRepository.signInTokens.isEmpty())
    }

    @Test
    fun `Auth 로그아웃 상태면 SDK에서 갱신한 토큰으로 조용히 로그인한다`() = runTest {
        tokenProvider.token = "fresh-token"
        sessionManager.token = "stale-local-token"

        assertEquals(EnsureAuthResult.SignedIn, useCase())

        assertEquals(listOf("fresh-token"), authRepository.signInTokens)
    }

    @Test
    fun `SDK 갱신을 못 하면 로컬 세션 토큰을 마지막으로 시도한다`() = runTest {
        sessionManager.token = "local-token"

        assertEquals(EnsureAuthResult.SignedIn, useCase())

        assertEquals(listOf("local-token"), authRepository.signInTokens)
    }

    @Test
    fun `토큰이 어디에도 없으면 재로그인이 필요하다`() = runTest {
        assertEquals(EnsureAuthResult.NeedsRelogin, useCase())
        assertTrue(authRepository.signInTokens.isEmpty())
    }

    @Test
    fun `서버가 토큰을 거부하면 재로그인이 필요하다`() = runTest {
        tokenProvider.token = "expired"
        authRepository.signInResult = Result.failure(KakaoTokenRejectedException())

        assertEquals(EnsureAuthResult.NeedsRelogin, useCase())
    }

    @Test
    fun `네트워크 오류는 일시 실패로 취급한다`() = runTest {
        tokenProvider.token = "token"
        authRepository.signInResult = Result.failure(IOException("offline"))

        assertEquals(EnsureAuthResult.TemporaryFailure, useCase())
    }
}
