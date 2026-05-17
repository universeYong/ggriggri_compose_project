package com.ahn.data.fcm

import com.ahn.data.local.NotificationPreferenceManager
import com.ahn.domain.notification.NotificationDisplayManager
import com.google.firebase.messaging.RemoteMessage
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NotificationHandler @Inject constructor(
    private val notificationPreferenceManager: NotificationPreferenceManager,
    private val notificationDisplayManager: NotificationDisplayManager
){
    suspend fun handleNotification(remoteMessage: RemoteMessage) {
        val notificationType = remoteMessage.data["type"]

        val isEnabled = notificationPreferenceManager.isNotificationEnabled(notificationType)

        if (isEnabled) {
            notificationDisplayManager.showNotification(
                title = remoteMessage.notification?.title,
                body = remoteMessage.notification?.body,
                data = remoteMessage.data,
            )
        }
    }
}
