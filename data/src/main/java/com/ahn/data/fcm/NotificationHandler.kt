package com.ahn.data.fcm

import android.content.Context
import android.util.Log
import com.ahn.data.local.NotificationPreferenceManager
import com.ahn.domain.notification.NotificationDisplayManager
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class NotificationHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationPreferenceManager: NotificationPreferenceManager,
    private val notificationDisplayManager: NotificationDisplayManager
){
    // 프로세스 수명 동안 유지되는 Singleton 소유 scope.
    // limitedParallelism(1): 이전(runBlocking, FCM 단일 워커 스레드)처럼 메시지를 수신 순서대로 하나씩 처리한다.
    private val scope = CoroutineScope(
        SupervisorJob() +
            Dispatchers.IO.limitedParallelism(1) +
            CoroutineExceptionHandler { _, throwable ->
                Log.e(TAG, "Failed to handle notification", throwable)
            }
    )

    fun handleNotification(remoteMessage: RemoteMessage) {
        val notificationType = remoteMessage.data["type"]
        // 서버는 data 전용 메시지로 보내며 title/body를 data에 담는다(백그라운드에서도 여기를 거쳐
        // 알림 설정과 채널이 적용되도록). 예전 서버 형식(notification 페이로드)도 계속 읽는다.
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body
        val data = remoteMessage.data

        scope.launch {
            val isEnabled = notificationPreferenceManager.isNotificationEnabled(notificationType)

            if (isEnabled) {
                notificationDisplayManager.showNotification(
                    title = title,
                    body = body,
                    data = data,
                )
            }
        }
    }

    private companion object {
        const val TAG = "NotificationHandler"
    }
}
