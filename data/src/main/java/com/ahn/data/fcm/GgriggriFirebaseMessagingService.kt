package com.ahn.data.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jakarta.inject.Inject

class GgriggriFirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // 포그라운드에서 알림 수신 시 처리
        notificationHandler.handleNotification(remoteMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // FCM 토큰 갱신 시 서버에 전송
        fcmTokenManager.saveTokenToServer(token)
    }
}