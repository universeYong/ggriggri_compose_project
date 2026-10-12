package com.ahn.data.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GgriggriFirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    // 서비스 소유 scope. onDestroy에서 cancel한다.
    private val serviceScope = CoroutineScope(
        SupervisorJob() +
            Dispatchers.IO +
            CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, "Unhandled exception in FCM service scope", throwable)
            }
    )

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // 포그라운드에서 알림 수신 시 처리
        notificationHandler.handleNotification(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // FCM 토큰 갱신 시 서버에 전송
        serviceScope.launch {
            try {
                fcmTokenManager.saveTokenToServer(token)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save refreshed FCM token", e)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "GgriggriFCMService"
    }
}
