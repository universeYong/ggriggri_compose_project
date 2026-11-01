package com.ahn.ggriggri.screen.ui.auth.viewmodel

import com.ahn.domain.common.NotificationPermissionChecker
import com.ahn.domain.model.User
import com.ahn.domain.repository.FCMRepository
import com.ahn.domain.repository.KakaoTokenRejectedException
import com.ahn.ggriggri.auth.EnsureFirebaseAuthUseCase
import com.ahn.ggriggri.testutil.FakeAuthRepository
import com.ahn.ggriggri.testutil.FakeGroupRepository
import com.ahn.ggriggri.testutil.FakeKakaoTokenProvider
import com.ahn.ggriggri.testutil.FakeSessionManager
import com.ahn.ggriggri.testutil.FakeUserRepository
import com.ahn.ggriggri.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class OAuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeFcmRepository : FCMRepository {
        val sentFor = mutableListOf<String>()
        override suspend fun getCurrentToken(): String? = null
        override suspend fun refreshToken() = Unit
        override suspend fun saveTokenToServer(token: String) = Unit
        override suspend fun sendTokenToServer(userId: String) {
            sentFor += userId
        }
        override suspend fun unregisterCurrentUserToken() = Unit
    }

    private val permissionChecker = object : NotificationPermissionChecker {
        override fun hasNotificationPermission() = true
        override fun shouldRequestNotificationPermission() = false
    }

    private lateinit var sessionManager: FakeSessionManager
    private lateinit var userRepository: FakeUserRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var tokenProvider: FakeKakaoTokenProvider
    private lateinit var fcmRepository: FakeFcmRepository

    @Before
    fun setUp() {
        sessionManager = FakeSessionManager()
        userRepository = FakeUserRepository()
        authRepository = FakeAuthRepository()
        tokenProvider = FakeKakaoTokenProvider()
        fcmRepository = FakeFcmRepository()
    }

    private fun createViewModel() = OAuthViewModel(
        sessionManager = sessionManager,
        userRepository = userRepository,
        groupRepository = FakeGroupRepository(),
        notificationPermissionChecker = permissionChecker,
        fcmRepository = fcmRepository,
        authRepository = authRepository,
        ensureFirebaseAuth = EnsureFirebaseAuthUseCase(authRepository, sessionManager, tokenProvider),
    )

    // ---- 카카오 로그인 후속 처리 ----

    @Test
    fun `Auth 로그인 성공 후에 사용자를 만들고 세션을 시작한다`() = runTest {
        val viewModel = createViewModel()

        viewModel.completeKakaoLogin("kakao-token", "12345", "안", "http://img")
        advanceUntilIdle()

        assertEquals(listOf("kakao-token"), authRepository.signInTokens)
        assertEquals(1, userRepository.createCalls.size)
        assertEquals("12345", sessionManager.getCurrentUserIdOnce())
        assertEquals(listOf("12345"), fcmRepository.sentFor)
        assertEquals("회원 정보 생성 성공", viewModel.loginStatus.value)
    }

    @Test
    fun `Auth 로그인에 실패하면 Firestore에 닿지 않고 세션도 만들지 않는다`() = runTest {
        authRepository.signInResult = Result.failure(IOException("offline"))
        val viewModel = createViewModel()

        viewModel.completeKakaoLogin("kakao-token", "12345", "안", null)
        advanceUntilIdle()

        assertTrue(userRepository.getUserByIdSyncCalls.isEmpty())
        assertTrue(userRepository.createCalls.isEmpty())
        assertTrue(userRepository.updateCalls.isEmpty())
        assertNull(sessionManager.getCurrentUserIdOnce())
        assertTrue(fcmRepository.sentFor.isEmpty())
        assertTrue(viewModel.loginStatus.value.startsWith("로그인 실패"))
        assertFalse(viewModel.loginStatus.value.contains("성공"))
    }

    @Test
    fun `서버가 카카오 토큰을 거부해도 같은 방식으로 실패 처리한다`() = runTest {
        authRepository.signInResult = Result.failure(KakaoTokenRejectedException())
        val viewModel = createViewModel()

        viewModel.completeKakaoLogin("bad-token", "12345", null, null)
        advanceUntilIdle()

        assertNull(sessionManager.getCurrentUserIdOnce())
        assertTrue(userRepository.getUserByIdSyncCalls.isEmpty())
        assertTrue(viewModel.loginStatus.value.startsWith("로그인 실패"))
    }

    @Test
    fun `기존 사용자는 본인 문서만 조회하고 copy로 갱신해 필드를 보존한다`() = runTest {
        userRepository.users["12345"] = User(
            userId = "12345", userName = "예전", userJoinTime = 111L, userGroupDocumentId = "g1",
        )
        val viewModel = createViewModel()

        viewModel.completeKakaoLogin("kakao-token", "12345", "새이름", null)
        advanceUntilIdle()

        assertEquals(listOf("12345"), userRepository.getUserByIdSyncCalls)
        assertEquals(0, userRepository.readCalls)
        val updated = userRepository.updateCalls.single()
        assertEquals("새이름", updated.userName)
        assertEquals(111L, updated.userJoinTime)
        assertEquals("g1", updated.userGroupDocumentId)
        assertEquals("회원 정보 업데이트 성공", viewModel.loginStatus.value)
    }

    // ---- 기존 세션 사용자의 조용한 Auth 복구 ----

    @Test
    fun `세션은 있고 Auth는 로그아웃인 기존 사용자는 조용히 로그인한 뒤 이동한다`() = runTest {
        val user = User(userId = "12345", userName = "안", userGroupDocumentId = "g1")
        sessionManager.userState.value = user
        userRepository.users["12345"] = user
        tokenProvider.token = "fresh"
        val viewModel = createViewModel()
        var home = false

        viewModel.checkUserGroupAndNavigate(onNavigationToGroup = {}, onNavigationToHome = { home = true })
        advanceUntilIdle()

        assertEquals(listOf("fresh"), authRepository.signInTokens)
        assertTrue(home)
    }

    @Test
    fun `카카오 토큰이 만료되어 거부되면 세션을 지우고 재로그인을 안내한다`() = runTest {
        val user = User(userId = "12345", userName = "안", userGroupDocumentId = "g1")
        sessionManager.userState.value = user
        sessionManager.token = "expired"
        authRepository.signInResult = Result.failure(KakaoTokenRejectedException())
        val viewModel = createViewModel()
        var navigated = false

        viewModel.checkUserGroupAndNavigate(
            onNavigationToGroup = { navigated = true },
            onNavigationToHome = { navigated = true },
        )
        advanceUntilIdle()

        assertFalse(navigated)
        assertNull(sessionManager.getCurrentUserIdOnce())
        assertEquals("로그인이 만료되었습니다. 다시 로그인해 주세요.", viewModel.loginStatus.value)
    }

    @Test
    fun `일시적인 오류면 세션은 유지하고 이동하지 않는다`() = runTest {
        val user = User(userId = "12345", userName = "안", userGroupDocumentId = "g1")
        sessionManager.userState.value = user
        tokenProvider.token = "token"
        authRepository.signInResult = Result.failure(IOException("offline"))
        val viewModel = createViewModel()
        var navigated = false

        viewModel.checkUserGroupAndNavigate(
            onNavigationToGroup = { navigated = true },
            onNavigationToHome = { navigated = true },
        )
        advanceUntilIdle()

        assertFalse(navigated)
        assertNotNull(sessionManager.getCurrentUserIdOnce())
    }

    @Test
    fun `개발용 로그인 경로는 Auth를 요구하지 않는다`() = runTest {
        val user = User(userId = "user1", userName = "dev", userGroupDocumentId = "")
        sessionManager.userState.value = user
        userRepository.users["user1"] = user
        val viewModel = createViewModel()
        var group = false

        viewModel.checkUserGroupAndNavigate(
            onNavigationToGroup = { group = true },
            onNavigationToHome = {},
            requireFirebaseAuth = false,
        )
        advanceUntilIdle()

        assertTrue(group)
        assertTrue(authRepository.signInTokens.isEmpty())
        assertEquals(0, tokenProvider.refreshCalls)
    }
}
