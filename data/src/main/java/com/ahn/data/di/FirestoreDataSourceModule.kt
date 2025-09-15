package com.ahn.data.di

import com.ahn.data.datasource.AnswerDataSource
import com.ahn.data.datasource.GroupDataSource
import com.ahn.data.datasource.QuestionDataSource
import com.ahn.data.datasource.QuestionListDataSource
import com.ahn.data.datasource.RequestDataSource
import com.ahn.data.datasource.StorageDataSource
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.firebase.FirebaseStorageDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreAnswerDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreGroupDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreQuestionDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreQuestionListDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreRequestDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreUserDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreDataSourceModule {

    @Provides
    @Singleton
    fun provideUserDataSource(): UserDataSource = FirestoreUserDataSourceImpl()

    @Provides
    @Singleton
    fun provideGroupDataSource(): GroupDataSource = FirestoreGroupDataSourceImpl()

    @Provides
    @Singleton
    fun provideQuestionDataSource(): QuestionDataSource = FirestoreQuestionDataSourceImpl()

    @Provides
    @Singleton
    fun provideQuestionListDataSource(): QuestionListDataSource =
        FirestoreQuestionListDataSourceImpl()

    @Provides
    @Singleton
    fun provideQuestionAnswerDataSource(): AnswerDataSource = FirestoreAnswerDataSourceImpl()

    @Provides
    @Singleton
    fun provideRequestDataSource(): RequestDataSource = FirestoreRequestDataSourceImpl()

    @Provides
    @Singleton
    fun provideStorageDataSource(): StorageDataSource = FirebaseStorageDataSourceImpl()


}