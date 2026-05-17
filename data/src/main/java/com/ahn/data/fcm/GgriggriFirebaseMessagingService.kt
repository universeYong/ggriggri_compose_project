package com.ahn.data.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * FCM 메시지/토큰 수신 시 호출되는 서비스.
 * Android가 직접 생성하므로 Hilt 주입이 안 되고, EntryPoint로 의존성을 가져옵니다.
 */
class GgriggriFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun getFcmEntryPoint(): FcmEntryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        FcmEntryPoint::class.java
    )

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val handler = getFcmEntryPoint().notificationHandler()
        serviceScope.launch {
            handler.handleNotification(remoteMessage)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val tokenManager = getFcmEntryPoint().fcmTokenManager()
        serviceScope.launch {
            tokenManager.saveTokenToServer(token)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}