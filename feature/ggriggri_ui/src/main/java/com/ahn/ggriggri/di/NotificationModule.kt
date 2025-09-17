package com.ahn.ggriggri.di

import android.content.Context
import com.ahn.domain.notification.NotificationChannelManager
import com.ahn.domain.notification.NotificationDisplayManager
import com.ahn.ggriggri.notification.NotificationChannelManagerImpl
import com.ahn.ggriggri.notification.NotificationDisplayManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationChannelManager(
        @ApplicationContext context: Context
    ): NotificationChannelManager = NotificationChannelManagerImpl(context)

    @Provides
    @Singleton
    fun provideNotificationDisplayManager(
        @ApplicationContext context: Context,
        notificationChannelManager: NotificationChannelManager
    ): NotificationDisplayManager =
        NotificationDisplayManagerImpl(context, notificationChannelManager)
}