package com.ahn.data.di

import android.content.Context
import com.ahn.data.fcm.NotificationHandler
import com.ahn.data.fcm.PermissionManager
import com.ahn.data.local.NotificationPreferenceManager
import com.ahn.domain.common.NotificationPermissionChecker
import com.ahn.domain.notification.NotificationDisplayManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FCMModule {

    // FCMTokenManager는 @Inject 생성자(+ @Singleton)로 Hilt가 직접 만든다(FcmTokenLocalStore도 주입).

    @Provides
    @Singleton
    fun provideNotificationHandler(
        @ApplicationContext context: Context,
        notificationPreferenceManager: NotificationPreferenceManager,
        notificationDisplayManager: NotificationDisplayManager
    ): NotificationHandler {
        return NotificationHandler(context, notificationPreferenceManager,notificationDisplayManager)
    }

    @Provides
    @Singleton
    fun provideNotificationPreferenceManager(
        @ApplicationContext context: Context,
    ): NotificationPreferenceManager = NotificationPreferenceManager(context)

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager = PermissionManager(context)

    @Provides
    @Singleton
    fun provideNotificationPermissionChecker(
        permissionManager: PermissionManager
    ): NotificationPermissionChecker = permissionManager

}