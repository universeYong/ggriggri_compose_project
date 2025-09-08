package com.ahn.domain.common

import kotlinx.coroutines.flow.Flow

interface TodayQuestionPreferences {
    val todayQuestionIdFlow: Flow<String?>

    suspend fun saveTodayQuestionId(questionId: String?)
    suspend fun clearTodayQuestionId()
}