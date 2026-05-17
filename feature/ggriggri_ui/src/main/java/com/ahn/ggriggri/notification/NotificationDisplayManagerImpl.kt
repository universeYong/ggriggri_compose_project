package com.ahn.ggriggri.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ahn.domain.notification.NotificationChannelManager
import com.ahn.domain.notification.NotificationDisplayManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NotificationDisplayManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationChannelManager: NotificationChannelManager
) : NotificationDisplayManager {

    private val notificationManager = NotificationManagerCompat.from(context)
    private var notificationId = 0

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    override fun showNotification(
        title: String?,
        body: String?,
        data: Map<String, String>
    ) {
        if (!hasNotificationPermission()) {
            Log.w("NotificationDisplayManager", "Notification permission not granted")
            return
        }

        val id = notificationId++
        val intent = createNotificationIntent(data)
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getChannelId(data["type"])

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            Log.e("NotificationDisplayManager", "Failed to show notification", e)
        }
    }

    private fun createNotificationIntent(data: Map<String, String>): Intent {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                data.forEach { (key, value) ->
                    putExtra(key, value)
                }
            }

        return launchIntent ?: Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
    }

    private fun getChannelId(type: String?): String {
        return when (type) {
            "question", "daily_question" -> NotificationChannelManagerImpl.DAILY_QUESTION_CHANNEL_ID
            "request" -> NotificationChannelManagerImpl.REQUEST_CHANNEL_ID
            "response" -> NotificationChannelManagerImpl.RESPONSE_CHANNEL_ID
            else -> NotificationChannelManagerImpl.DAILY_QUESTION_CHANNEL_ID
        }
    }
}
