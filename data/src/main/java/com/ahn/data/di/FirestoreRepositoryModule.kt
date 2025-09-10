package com.ahn.data.di

import com.ahn.data.repository.FirestoreAnswerRepositoryImpl
import com.ahn.data.repository.FirestoreGroupRepositoryImpl
import com.ahn.data.repository.FirestoreQuestionListRepositoryImpl
import com.ahn.data.repository.FirestoreQuestionRepositoryImpl
import com.ahn.data.repository.FirestoreUserRepositoryImpl
import com.ahn.domain.repository.AnswerRepository
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        firestoreUserRepositoryImpl: FirestoreUserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        firestoreGroupRepositoryImpl: FirestoreGroupRepositoryImpl
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        firestoreQuestionRepositoryImpl: FirestoreQuestionRepositoryImpl
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindQuestionListRepository(
        firestoreQuestionListRepositoryImpl: FirestoreQuestionListRepositoryImpl
    ): QuestionListRepository

    @Binds
    @Singleton
    abstract fun bindAnswerRepository(
        firestoreAnswerRepositoryImpl: FirestoreAnswerRepositoryImpl
    ): AnswerRepository

}