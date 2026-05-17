package com.ahn.domain.usecase.home

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.repository.ResponseRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import jakarta.inject.Inject

class HasUserRespondedUseCase @Inject constructor(
    private val responseRepository: ResponseRepository,
) {
    suspend operator fun invoke(requestId: String, userId: String): Boolean {
        val responsesResult = responseRepository.read(requestId)
            .filter { it !is DataResourceResult.Loading }
            .first()

        return when (responsesResult) {
            is DataResourceResult.Success -> {
                responsesResult.data.any { it.responseUserDocumentID == userId }
            }
            else -> false
        }
    }
}
