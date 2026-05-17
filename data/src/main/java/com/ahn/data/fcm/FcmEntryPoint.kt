package com.ahn.data.fcm

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * FirebaseMessagingService는 Android 시스템이 생성하므로 Hilt가 @Inject를 해주지 않습니다.
 * 이 EntryPoint로 Application에서 FCM 관련 의존성을 가져와 사용합니다.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun notificationHandler(): NotificationHandler
    fun fcmTokenManager(): FCMTokenManager
}
