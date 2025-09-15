package com.ahn.ggriggri.screen.ui.setting.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Group
import com.ahn.domain.model.User
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingGroupViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val currentUser: StateFlow<User?> = sessionManager.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // currentUser를 관찰하여 사용자 정보가 변경될 때마다 그룹 정보 로드
        viewModelScope.launch {
            currentUser.collect { user ->
                Log.d("SettingGroupViewModel", "currentUser 변경 감지: $user")
                if (user != null) {
                    loadCurrentGroupForUser(user)
                } else {
                    _currentGroup.value = null
                    _isLoading.value = false
                    _errorMessage.value = null
                }
            }
        }
    }

    private fun loadCurrentGroupForUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("SettingGroupViewModel", "loadCurrentGroupForUser 시작 - user: ${user.userId}")
                
                val groupId = user.userGroupDocumentId
                Log.d("SettingGroupViewModel", "그룹 ID: $groupId")
                
                if (groupId.isNullOrEmpty()) {
                    _errorMessage.value = "그룹에 가입되지 않았습니다."
                    _isLoading.value = false
                    Log.e("SettingGroupViewModel", "그룹 ID가 null 또는 비어있음")
                    return@launch
                }

                Log.d("SettingGroupViewModel", "Repository 호출 시작 - groupId: $groupId")
                
                // Loading 상태를 제외하고 첫 번째 결과만 받기
                val result = groupRepository.getGroupById(groupId)
                    .filter { it !is DataResourceResult.Loading }
                    .first()
                    
                Log.d("SettingGroupViewModel", "Repository 결과: $result")
                Log.d("SettingGroupViewModel", "Repository 결과 타입: ${result::class.simpleName}")
                if (result is DataResourceResult.Success) {
                    Log.d("SettingGroupViewModel", "Success 데이터: ${result.data}")
                } else if (result is DataResourceResult.Failure) {
                    Log.d("SettingGroupViewModel", "Failure 예외: ${result.exception}")
                }
                
                when (result) {
                    is DataResourceResult.Success -> {
                        _currentGroup.value = result.data
                        _isLoading.value = false
                        Log.d("SettingGroupViewModel", "그룹 정보 로드 성공: ${result.data}")
                        Log.d("SettingGroupViewModel", "그룹 코드: ${result.data?.groupCode}")
                        Log.d("SettingGroupViewModel", "그룹명: ${result.data?.groupName}")
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "그룹 정보를 불러오는데 실패했습니다: ${result.exception.message}"
                        _isLoading.value = false
                        Log.e("SettingGroupViewModel", "그룹 정보 로드 실패", result.exception)
                    }
                    else -> {
                        _isLoading.value = false
                        Log.w("SettingGroupViewModel", "예상치 못한 결과: $result")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "그룹 정보를 불러오는 중 오류가 발생했습니다: ${e.message}"
                _isLoading.value = false
                Log.e("SettingGroupViewModel", "그룹 정보 로드 중 예외 발생", e)
            }
        }
    }

    fun updateGroupName(newGroupName: String) {
        viewModelScope.launch {
            val currentGroup = _currentGroup.value
            if (currentGroup == null) {
                _errorMessage.value = "그룹 정보가 없습니다."
                return@launch
            }

            if (newGroupName.isBlank()) {
                _errorMessage.value = "그룹명을 입력해주세요."
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val updatedGroup = currentGroup.copy(groupName = newGroupName)
                val result = groupRepository.update(updatedGroup)
                    .filter { it !is DataResourceResult.Loading }
                    .first()
                when (result) {
                    is DataResourceResult.Success -> {
                        _currentGroup.value = updatedGroup
                        _successMessage.value = "그룹명이 성공적으로 변경되었습니다."
                        _isLoading.value = false
                        Log.d("SettingGroupViewModel", "그룹명 변경 성공: $newGroupName")
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "그룹명 변경에 실패했습니다: ${result.exception.message}"
                        _isLoading.value = false
                        Log.e("SettingGroupViewModel", "그룹명 변경 실패", result.exception)
                    }
                    else -> {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "그룹명 변경 중 오류가 발생했습니다: ${e.message}"
                _isLoading.value = false
                Log.e("SettingGroupViewModel", "그룹명 변경 중 예외 발생", e)
            }
        }
    }


    fun leaveGroup(onNavigateToGroup: () -> Unit = {}) {
        viewModelScope.launch {
            val currentUser = currentUser.first()
            val currentGroup = _currentGroup.value

            if (currentUser == null) {
                _errorMessage.value = "사용자 정보를 찾을 수 없습니다."
                return@launch
            }

            if (currentGroup == null) {
                _errorMessage.value = "그룹 정보가 없습니다."
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            try {
                // 1. 그룹에서 사용자 제거
                val groupResult = groupRepository.removeUserFromGroup(currentGroup.groupDocumentId, currentUser.userId)
                    .filter { it !is DataResourceResult.Loading }
                    .first()
                
                when (groupResult) {
                    is DataResourceResult.Success -> {
                        // 2. 사용자의 userGroupDocumentId 비우기
                        val userResult = userRepository.updateUserGroupDocumentId(currentUser.userId, "")
                            .filter { it !is DataResourceResult.Loading }
                            .first()
                        
                        when (userResult) {
                            is DataResourceResult.Success -> {
                                _successMessage.value = "그룹에서 성공적으로 나갔습니다."
                                _isLoading.value = false
                                Log.d("SettingGroupViewModel", "그룹 나가기 성공 - 그룹과 사용자 정보 모두 업데이트됨")
                                onNavigateToGroup()
                            }
                            is DataResourceResult.Failure -> {
                                _errorMessage.value = "사용자 정보 업데이트에 실패했습니다: ${userResult.exception.message}"
                                _isLoading.value = false
                                Log.e("SettingGroupViewModel", "사용자 정보 업데이트 실패", userResult.exception)
                            }
                            else -> {
                                _isLoading.value = false
                            }
                        }
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "그룹 나가기에 실패했습니다: ${groupResult.exception.message}"
                        _isLoading.value = false
                        Log.e("SettingGroupViewModel", "그룹 나가기 실패", groupResult.exception)
                    }
                    else -> {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "그룹 나가기 중 오류가 발생했습니다: ${e.message}"
                _isLoading.value = false
                Log.e("SettingGroupViewModel", "그룹 나가기 중 예외 발생", e)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun refreshGroupInfo() {
        val currentUser = currentUser.value
        if (currentUser != null) {
            loadCurrentGroupForUser(currentUser)
        }
    }

    fun updateGroupPassword(newPassword: String) {
        viewModelScope.launch {
            val currentGroup = _currentGroup.value
            if (currentGroup == null) {
                _errorMessage.value = "그룹 정보를 찾을 수 없습니다."
                return@launch
            }

            if (newPassword.isBlank()) {
                _errorMessage.value = "새 비밀번호를 입력해주세요."
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                Log.d("SettingGroupViewModel", "그룹 비밀번호 변경 시작 - 그룹 ID: ${currentGroup.groupDocumentId}")

                val updatedGroup = currentGroup.copy(groupPw = newPassword)
                
                val result = groupRepository.update(updatedGroup)
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                when (result) {
                    is DataResourceResult.Success -> {
                        _currentGroup.value = updatedGroup
                        _successMessage.value = "그룹 비밀번호가 성공적으로 변경되었습니다."
                        Log.d("SettingGroupViewModel", "그룹 비밀번호 변경 성공")
                    }
                    is DataResourceResult.Failure -> {
                        _errorMessage.value = "그룹 비밀번호 변경에 실패했습니다: ${result.exception.message}"
                        Log.e("SettingGroupViewModel", "그룹 비밀번호 변경 실패", result.exception)
                    }
                    else -> {
                        _errorMessage.value = "예상치 못한 오류가 발생했습니다."
                        Log.w("SettingGroupViewModel", "예상치 못한 결과: $result")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "그룹 비밀번호 변경 중 오류가 발생했습니다: ${e.message}"
                Log.e("SettingGroupViewModel", "그룹 비밀번호 변경 중 예외 발생", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}