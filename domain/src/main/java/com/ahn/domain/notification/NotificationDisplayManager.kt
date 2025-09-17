package com.ahn.domain.notification

interface NotificationDisplayManager {
    fun showNotification(
        title: String?,
        body: String?,
        data: Map<String, String>
    )
}