package com.ahn.domain.model

data class NotificationSettings(
    val dailyQuestionEnabled: Boolean = true,
    val requestEnabled: Boolean = true,
    val responseEnabled: Boolean = true,
    val allNotificationsEnabled: Boolean = true
)
