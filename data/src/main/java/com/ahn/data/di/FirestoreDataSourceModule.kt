package com.ahn.data.di

import com.ahn.data.datasource.AnswerDataSource
import com.ahn.data.datasource.GroupDataSource
import com.ahn.data.datasource.QuestionDataSource
import com.ahn.data.datasource.QuestionListDataSource
import com.ahn.data.datasource.RequestDataSource
import com.ahn.data.datasource.ResponseDataSource
import com.ahn.data.datasource.StorageDataSource
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.firebase.FirebaseStorageDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreAnswerDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreGroupDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreQuestionDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreQuestionListDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreRequestDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreResponseDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreUserDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class FirestoreDataSourceModule {

    @Binds
    abstract fun bindUserDataSource(
        firestoreUserDataSourceImpl: FirestoreUserDataSourceImpl
    ): UserDataSource

    @Binds
    abstract fun bindGroupDataSource(
        firestoreGroupDataSourceImpl: FirestoreGroupDataSourceImpl
    ): GroupDataSource

    @Binds
    abstract fun bindQuestionDataSource(
        firestoreQuestionDataSourceImpl: FirestoreQuestionDataSourceImpl
    ): QuestionDataSource

    @Binds
    abstract fun bindQuestionListDataSource(
        firestoreQuestionListDataSourceImpl: FirestoreQuestionListDataSourceImpl
    ): QuestionListDataSource

    @Binds
    abstract fun bindAnswerDataSource(
        firestoreAnswerDataSourceImpl: FirestoreAnswerDataSourceImpl
    ): AnswerDataSource

    @Binds
    abstract fun bindRequestDataSource(
        firestoreRequestDataSourceImpl: FirestoreRequestDataSourceImpl
    ): RequestDataSource

    @Binds
    abstract fun bindStorageDataSource(
        firebaseStorageDataSourceImpl: FirebaseStorageDataSourceImpl
    ): StorageDataSource

    @Binds
    abstract fun bindResponseDataSource(
        firestoreResponseDataSourceImpl: FirestoreResponseDataSourceImpl
    ): ResponseDataSource

}