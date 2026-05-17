package com.ahn.ggriggri.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import com.ahn.domain.notification.NotificationChannelManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NotificationChannelManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationChannelManager {

    companion object {
        const val DAILY_QUESTION_CHANNEL_ID = "daily_question_channel"
        const val REQUEST_CHANNEL_ID = "request_channel"
        const val RESPONSE_CHANNEL_ID = "response_channel"
    }

    override fun createNotificationChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val dailyQuestionChannel = NotificationChannel(
            DAILY_QUESTION_CHANNEL_ID,
            "오늘의 질문",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "매일 새로운 질문이 도착했을 때 알림"
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)
        }

        val requestChannel = NotificationChannel(
            REQUEST_CHANNEL_ID,
            "새로운 요청",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "그룹 멤버가 요청을 보냈을 때 알림"
            enableLights(true)
            lightColor = Color.GREEN
        }

        val responseChannel = NotificationChannel(
            RESPONSE_CHANNEL_ID,
            "응답 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "요청에 대한 응답이 있을 때 알림"
            enableLights(true)
            lightColor = Color.YELLOW
        }

        notificationManager.createNotificationChannels(
            listOf(dailyQuestionChannel, requestChannel, responseChannel)
        )
    }
}