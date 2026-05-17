package com.ahn.domain.usecase.home

import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.Profile
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import jakarta.inject.Inject

class LoadProfilesUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(groupId: String): DataResourceResult<List<Profile>> = coroutineScope {
        val groupMemberResult = groupRepository
            .getGroupMembers(groupId)
            .filter { it !is DataResourceResult.Loading }
            .first()

        when (groupMemberResult) {
            is DataResourceResult.Success -> {
                val userIds = groupMemberResult.data
                if (userIds.isNullOrEmpty()) {
                    DataResourceResult.Success(emptyList())
                } else {
                    val profiles = userIds.map { userId ->
                        async {
                            val userResult = userRepository
                                .getUserById(userId)
                                .filter { it !is DataResourceResult.Loading }
                                .first()

                            when (userResult) {
                                is DataResourceResult.Success -> {
                                    userResult.data?.let { user ->
                                        Profile(
                                            id = user.userId.ifBlank { userId },
                                            name = user.userName,
                                            profileImageUrl = user.userProfileImage
                                        )
                                    }
                                }
                                else -> null
                            }
                        }
                    }.awaitAll().filterNotNull()

                    DataResourceResult.Success(profiles)
                }
            }
            is DataResourceResult.Failure -> DataResourceResult.Failure(groupMemberResult.exception)
            else -> DataResourceResult.Success(emptyList())
        }
    }
}
