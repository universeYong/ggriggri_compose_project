package com.ahn.domain.usecase.home

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.QuestionList
import com.ahn.domain.repository.QuestionListRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import jakarta.inject.Inject

class LoadAllQuestionListsUseCase @Inject constructor(
    private val questionListRepository: QuestionListRepository,
) {
    suspend operator fun invoke(): DataResourceResult<List<QuestionList>> {
        val result = questionListRepository.read()
            .filter { it !is DataResourceResult.Loading }
            .firstOrNull()

        return when (result) {
            is DataResourceResult.Success -> DataResourceResult.Success(result.data ?: emptyList())
            is DataResourceResult.Failure -> DataResourceResult.Failure(result.exception)
            else -> DataResourceResult.Success(emptyList())
        }
    }
}
