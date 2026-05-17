package com.ahn.domain.usecase.home

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.repository.UserRepository
import jakarta.inject.Inject

class GetUserNameUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: String): String {
        return when (val result = userRepository.getUserByIdSync(userId)) {
            is DataResourceResult.Success -> result.data?.userName ?: "알 수 없음"
            else -> "알 수 없음"
        }
    }
}
