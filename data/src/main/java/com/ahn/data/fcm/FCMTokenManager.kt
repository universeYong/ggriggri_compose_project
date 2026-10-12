package com.ahn.data.fcm

import android.util.Log
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

@Singleton
class FCMTokenManager @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) {
    suspend fun getCurrentToken(): String? = runCatching {
        FirebaseMessaging.getInstance().token.await()
    }.getOrNull()

    // 토큰을 서버에 전송하는 함수 (Flow 사용)
    suspend fun sendTokenToServer() {
        val token = getCurrentToken()
        if (token != null) {
            val currentUser = sessionManager.currentUserFlow.first()
            currentUser?.let { user ->
                userRepository.updateFcmToken(user.userId, token)
                    .collect { result ->
                        when (result) {
                            is DataResourceResult.Success -> {
                                Log.d("FCM", "Token sent to server successfully")
                            }
                            is DataResourceResult.Failure -> {
                                Log.e("FCM", "Failed to send token:")
                            }
                            else -> {
                                Log.d("FCM", "Sending token to server...")
                            }
                        }
                    }
            }
        }
    }

    // 로그인 직후처럼 userId를 이미 알고 있는 경우 타이밍 이슈 없이 전송한다.
    suspend fun sendTokenToServer(userId: String) {
        val token = getCurrentToken()
        if (token.isNullOrBlank()) {
            Log.e("FCM", "Failed to get FCM token")
            return
        }

        userRepository.updateFcmToken(userId, token)
            .collect { result ->
                when (result) {
                    is DataResourceResult.Success -> {
                        Log.d("FCM", "Token sent to server successfully for userId=$userId")
                    }
                    is DataResourceResult.Failure -> {
                        Log.e("FCM", "Failed to send token for userId=$userId", result.exception)
                    }
                    else -> {
                        Log.d("FCM", "Sending token to server for userId=$userId...")
                    }
                }
            }
    }

    suspend fun saveTokenToServer(token: String) {
        val currentUser = sessionManager.currentUserFlow.first()
        currentUser?.let { user ->
            userRepository.updateFcmToken(user.userId, token)
                .collect { result ->
                    when (result) {
                        is DataResourceResult.Success -> {
                            Log.d("FCM", "Token saved successfully")
                        }
                        is DataResourceResult.Failure -> {
                            Log.e("FCM", "Failed to save token: ")
                        }
                        else -> {
                            Log.d("FCM", "Saving token...")
                        }
                    }
                }
        }
    }

    suspend fun refreshToken() {
        val token = getCurrentToken()
        token?.let { saveTokenToServer(it) }
    }
}

