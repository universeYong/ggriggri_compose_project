package com.ahn.domain.usecase.home

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Request
import com.ahn.domain.repository.RequestRepository
import kotlinx.coroutines.flow.Flow
import jakarta.inject.Inject

class LoadRequestsUseCase @Inject constructor(
    private val requestRepository: RequestRepository,
) {
    suspend operator fun invoke(groupId: String): Flow<DataResourceResult<List<Request>>> {
        return requestRepository.read(groupId)
    }
}
