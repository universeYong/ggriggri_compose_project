package com.ahn.data.di

import android.content.Context
import com.ahn.data.local.SessionManagerImpl
import com.ahn.data.local.TodayQuestionPreferencesImpl
import com.ahn.domain.common.SessionManager
import com.ahn.domain.common.TodayQuestionPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        moshi: Moshi
    ): SessionManager = SessionManagerImpl(context, moshi)

    @Provides
    @Singleton
    fun provideTodayQuestionPreferences(
        @ApplicationContext context: Context
    ): TodayQuestionPreferences = TodayQuestionPreferencesImpl(context)
}