package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class Profile(
    val id: String, // 사용자 ID
    val name: String,
    val profileImageUrl: String?
)

class HomeViewModel(
    application: Application,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
): AndroidViewModel(application) {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init{
        viewModelScope.launch {
            val initialGroupId = sessionManager.currentUserGroupIdFlow.first()
            Log.d("HomeViewModel", "Initial Group ID from session: $initialGroupId") // 로그 추가
            if (initialGroupId != null && initialGroupId.isNotEmpty()) {
                loadProfiles(initialGroupId)
            } else {
                Log.w("HomeViewModel", "Initial Group ID is null or empty. loadProfiles not called from init.")
            }
        }
    }

    fun loadProfiles(groupId: String) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "loadProfiles called with groupId: $groupId") // 로그 추가
            _error.value = null

            val groupMemberResult = groupRepository
                    .getGroupMembers(groupId)
                    .filter{it !is DataResourceResult.Loading}
                    .first()

            Log.d("HomeViewModel", "groupMemberResult: $groupMemberResult") // 로그 추가

            when(groupMemberResult) {
                is DataResourceResult.Success -> {
                    Log.d("HomeViewModel", "getGroupMembers SUCCESS")
                    val userIds = groupMemberResult.data
                    Log.d("HomeViewModel", "User IDs: $userIds") // 로그 추가
                    if (userIds.isNullOrEmpty()) {
                        Log.w("HomeViewModel", "User IDs list is null or empty. Returning from loadProfiles.")
                        _profiles.value = emptyList()
                        return@launch
                    }
                    Log.d("HomeViewModel", "Entering runCatching block to fetch user details.")
                    runCatching {
                        val profileListDeferred = userIds.map { userId ->
                            Log.d("HomeViewModel", "Async call for userId: $userId")
                            async {
                                Log.d("HomeViewModel", "Calling getUserById for userId: $userId")

                                val userResult = userRepository
                                    .getUserById(userId)
                                    .filter { it !is DataResourceResult.Loading }
                                    .first()
                                Log.d("HomeViewModel", "getUserById result for $userId: $userResult")
                                when (userResult) {
                                    is DataResourceResult.Success -> {
                                        userResult.data?.let { user ->
                                            val imageUrl = user.userProfileImage
                                            Log.d("HomeViewModel", "User: ${user.userName}, Image URL: $imageUrl") // 로그 추가
                                            Profile(
                                                id = user.userId ?: userId,
                                                name = user.userName,
                                                profileImageUrl = user.userProfileImage
                                            )
                                        }
                                    }
                                    is DataResourceResult.Failure -> {
                                        Log.e("HomeViewModel", "Failed to get user info for $userId")
                                        null
                                    }
                                    else -> {null}
                                }
                            }
                        }
                        val fetchedProfiles = profileListDeferred.awaitAll().filterNotNull()
                        _profiles.value = fetchedProfiles
                    }
                }
                is DataResourceResult.Failure -> {
                    _error.value = "그룹 멤버 정보를 가져오는데 실패했습니다: ${groupMemberResult}"
                }
                else -> {null}
            }
        }
    }


    fun setCurrentGroupIdAndLoad(newGroupId: String) {
        loadProfiles(newGroupId)
    }


    fun onSeeAllProfilesClicked() {
        // 전체보기 화면으로 이동하거나 관련 로직을 수행합니다.
    }

    fun clearError() {
        _error.value = null
    }
}