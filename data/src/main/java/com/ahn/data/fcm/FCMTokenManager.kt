package com.ahn.data.fcm

import android.util.Log
import com.ahn.data.local.FcmTokenLocalStore
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

@Singleton
class FCMTokenManager @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val tokenStore: FcmTokenLocalStore,
) {
    suspend fun getCurrentToken(): String? = runCatching {
        FirebaseMessaging.getInstance().token.await()
    }.getOrNull()

    // 세션의 현재 사용자로 현재 토큰을 전송한다.
    suspend fun sendTokenToServer() {
        val token = getCurrentToken() ?: return
        val currentUser = sessionManager.currentUserFlow.first() ?: return
        registerToken(currentUser.userId, token)
    }

    // 로그인 직후처럼 userId를 이미 알고 있는 경우 타이밍 이슈 없이 전송한다.
    suspend fun sendTokenToServer(userId: String) {
        val token = getCurrentToken()
        if (token.isNullOrBlank()) {
            Log.e(TAG, "Failed to get FCM token")
            return
        }
        registerToken(userId, token)
    }

    // onNewToken 경로. 세션이 없을 때 받은 토큰은 따로 대기열에 두지 않는다:
    // 로그인 직후 OAuthViewModel이 sendTokenToServer(userId)로 그 시점의 현재 토큰을 보내기 때문이다.
    suspend fun saveTokenToServer(token: String) {
        val currentUser = sessionManager.currentUserFlow.first()
        if (currentUser == null) {
            Log.d(TAG, "No session; refreshed token will be sent after login")
            return
        }
        registerToken(currentUser.userId, token)
    }

    suspend fun refreshToken() {
        val token = getCurrentToken()
        token?.let { saveTokenToServer(it) }
    }

    // 로그아웃 직전에 호출한다(세션에 사용자가 남아 있어야 문서를 찾을 수 있음).
    // 서버 문서에서 토큰을 지운 뒤 기기 토큰을 폐기한다. 실패해도 로그아웃은 계속되어야 하므로 예외는 삼킨다.
    suspend fun unregisterToken() {
        val currentUser = sessionManager.currentUserFlow.first()
        val token = getCurrentToken()
        if (currentUser != null && !token.isNullOrBlank()) {
            val result = userRepository.removeFcmToken(currentUser.userId, token)
                .first { it is DataResourceResult.Success || it is DataResourceResult.Failure }
            if (result is DataResourceResult.Failure) {
                Log.e(TAG, "Failed to remove token on logout", result.exception)
            }
        }
        runCatching { tokenStore.clear() }
            .onFailure { Log.e(TAG, "Failed to clear stored FCM token", it) }
        runCatching { FirebaseMessaging.getInstance().deleteToken().await() }
            .onFailure { Log.e(TAG, "Failed to delete FCM token", it) }
    }

    /**
     * 토큰을 user_data/{userId}._userFcmToken에 추가하고, 이 기기가 전에 등록했던 다른 토큰이 있으면
     * 그 토큰을 등록했던 문서에서 arrayRemove 한다(토큰 갱신 시 이전 토큰 누적 방지).
     * 이전 토큰 제거는 best-effort: 실패해도 Functions가 발송 시 무효 토큰으로 정리한다.
     */
    private suspend fun registerToken(userId: String, token: String) {
        val result = userRepository.updateFcmToken(userId, token)
            .first { it is DataResourceResult.Success || it is DataResourceResult.Failure }
        if (result is DataResourceResult.Failure) {
            Log.e(TAG, "Failed to send token for userId=$userId", result.exception)
            return
        }
        Log.d(TAG, "Token sent to server successfully for userId=$userId")

        try {
            val previous = tokenStore.get()
            if (previous != null && previous.token != token) {
                val removeResult = userRepository.removeFcmToken(previous.userId, previous.token)
                    .first { it is DataResourceResult.Success || it is DataResourceResult.Failure }
                if (removeResult is DataResourceResult.Failure) {
                    Log.w(TAG, "Failed to remove previous token", removeResult.exception)
                }
            }
            tokenStore.save(userId, token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update stored FCM token", e)
        }
    }

    private companion object {
        const val TAG = "FCM"
    }
}
