package com.ahn.data.di

import com.ahn.data.repository.FirebaseStorageRepositoryImpl
import com.ahn.data.repository.FirestoreAnswerRepositoryImpl
import com.ahn.data.repository.FirestoreGroupRepositoryImpl
import com.ahn.data.repository.FirestoreQuestionListRepositoryImpl
import com.ahn.data.repository.FirestoreQuestionRepositoryImpl
import com.ahn.data.repository.FirestoreRequestRepositoryImpl
import com.ahn.data.repository.FirestoreUserRepositoryImpl
import com.ahn.data.repository.StorageRepository
import com.ahn.domain.repository.AnswerRepository
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.RequestRepository
import com.ahn.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class FirestoreRepositoryModule {

    @Binds
    abstract fun bindUserRepository(
        firestoreUserRepositoryImpl: FirestoreUserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindGroupRepository(
        firestoreGroupRepositoryImpl: FirestoreGroupRepositoryImpl
    ): GroupRepository

    @Binds
    abstract fun bindQuestionRepository(
        firestoreQuestionRepositoryImpl: FirestoreQuestionRepositoryImpl
    ): QuestionRepository

    @Binds
    abstract fun bindQuestionListRepository(
        firestoreQuestionListRepositoryImpl: FirestoreQuestionListRepositoryImpl
    ): QuestionListRepository

    @Binds
    abstract fun bindAnswerRepository(
        firestoreAnswerRepositoryImpl: FirestoreAnswerRepositoryImpl
    ): AnswerRepository

    @Binds
    abstract fun bindRequestRepository(
        firestoreRequestRepositoryImpl: FirestoreRequestRepositoryImpl
    ): RequestRepository

    @Binds
    abstract fun bindStorageRepository(
        firebaseStorageRepositoryImpl: FirebaseStorageRepositoryImpl
    ): StorageRepository

}