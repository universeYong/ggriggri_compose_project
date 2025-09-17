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

