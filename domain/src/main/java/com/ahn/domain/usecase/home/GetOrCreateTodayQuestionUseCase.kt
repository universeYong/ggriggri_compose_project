package com.ahn.domain.usecase.home

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.TodayQuestionPreferences
import com.ahn.domain.model.Question
import com.ahn.domain.model.QuestionList
import com.ahn.domain.repository.QuestionRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import jakarta.inject.Inject

class GetOrCreateTodayQuestionUseCase @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val todayQuestionPreferences: TodayQuestionPreferences,
) {
    private val baseDate: LocalDate = LocalDate.of(2025, 8, 15)

    suspend operator fun invoke(
        groupId: String,
        questionLists: List<QuestionList>,
    ): DataResourceResult<Question?> {
        val todayStartTimestamp =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val existingResult = questionRepository
            .getQuestionForGroupAndDate(groupId, todayStartTimestamp)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        val finalQuestion = when (existingResult) {
            is DataResourceResult.Success -> existingResult.data
                ?: createQuestion(groupId, todayStartTimestamp, questionLists)
            is DataResourceResult.Failure -> createQuestion(groupId, todayStartTimestamp, questionLists)
            else -> createQuestion(groupId, todayStartTimestamp, questionLists)
        }

        finalQuestion?.questionId
            ?.takeIf { it.isNotBlank() }
            ?.let { todayQuestionPreferences.saveTodayQuestionId(it) }

        return DataResourceResult.Success(finalQuestion)
    }

    private suspend fun createQuestion(
        groupId: String,
        timestamp: Long,
        questionLists: List<QuestionList>,
    ): Question? {
        if (questionLists.isEmpty()) return null

        val selectedQuestion = selectQuestionForTheDay(questionLists) ?: return null
        val newQuestion = Question(
            questionCreatedTime = timestamp,
            questionGroupDocumentId = groupId,
            questionListDocumentId = selectedQuestion.number.toString()
        )

        val createResult = questionRepository.create(newQuestion)
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        return when (createResult) {
            is DataResourceResult.Success -> createResult.data
            else -> null
        }
    }

    private fun selectQuestionForTheDay(lists: List<QuestionList>): QuestionList? {
        if (lists.isEmpty()) return null
        val today = LocalDate.now()
        val daysSinceBase = ChronoUnit.DAYS.between(baseDate, today)
        val questionIndex = (daysSinceBase % lists.size).toInt()
        return lists[questionIndex]
    }
}
