package com.ahn.data.di

import android.content.Context
import com.ahn.data.fcm.FCMTokenManager
import com.ahn.data.fcm.NotificationHandler
import com.ahn.data.fcm.PermissionManager
import com.ahn.data.local.NotificationPreferenceManager
import com.ahn.data.local.SessionManagerImpl
import com.ahn.domain.common.SessionManager
import com.ahn.domain.notification.NotificationDisplayManager
import com.ahn.domain.repository.UserRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FCMModule {

    @Provides
    @Singleton
    fun provideFCMTokenManager(
        userRepository: UserRepository,
        sessionManager: SessionManager,
    ): FCMTokenManager {
        return FCMTokenManager(userRepository, sessionManager)
    }

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
    fun provideSessionManager(
        @ApplicationContext context: Context,
        moshi: Moshi,
    ): SessionManager = SessionManagerImpl(context, moshi)

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

}