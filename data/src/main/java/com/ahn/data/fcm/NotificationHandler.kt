package com.ahn.data.fcm

import android.content.Context
import com.ahn.data.local.NotificationPreferenceManager
import com.ahn.domain.notification.NotificationDisplayManager
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class NotificationHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationPreferenceManager: NotificationPreferenceManager,
    private val notificationDisplayManager: NotificationDisplayManager
){
    fun handleNotification(remoteMessage: RemoteMessage) {
        val notificationType = remoteMessage.data["type"]
        val isEnabled = runBlocking {
            notificationPreferenceManager.isNotificationEnabled(notificationType)
        }


        if (isEnabled) {
            notificationDisplayManager.showNotification(
                title = remoteMessage.notification?.title,
                body = remoteMessage.notification?.body,
                data = remoteMessage.data,
            )
        }
    }
}