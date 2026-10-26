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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    // 그룹 나가기 성공 이벤트(일회성). 값은 화면에 보여줄 안내 메시지.
    private val _leaveGroupSuccess = Channel<String>(Channel.BUFFERED)
    val leaveGroupSuccess: Flow<String> = _leaveGroupSuccess.receiveAsFlow()

    // 그룹을 나간 뒤에는 세션의 그룹 ID가 비는 것이 정상이므로 "그룹에 가입되지 않았습니다" 오류를 띄우지 않는다.
    // 또한 이동 전 중복 탭으로 다시 나가기를 시도하지 않게 막는다.
    private var hasLeftGroup = false

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
                Log.d("SettingGroupViewModel", "currentUser 변경 감지: ${user?.userId}")
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
                    _isLoading.value = false
                    if (!hasLeftGroup) {
                        _errorMessage.value = "그룹에 가입되지 않았습니다."
                        Log.e("SettingGroupViewModel", "그룹 ID가 null 또는 비어있음")
                    }
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = "그룹명 변경 중 오류가 발생했습니다: ${e.message}"
                _isLoading.value = false
                Log.e("SettingGroupViewModel", "그룹명 변경 중 예외 발생", e)
            }
        }
    }


    /**
     * 그룹을 나간다. 성공하면 [leaveGroupSuccess]로 안내 메시지를 한 번 내보내며,
     * 화면이 그 메시지를 보여준 뒤 그룹 선택 화면으로 이동한다.
     */
    fun leaveGroup() {
        if (hasLeftGroup) return
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
                                // 세션 갱신으로 그룹 ID가 비면 currentUser 관찰이 "그룹에 가입되지 않았습니다"
                                // 오류를 띄우지 않도록 먼저 표시해 둔다.
                                hasLeftGroup = true
                                refreshSessionUser(currentUser.userId)
                                _isLoading.value = false
                                _leaveGroupSuccess.send("그룹에서 성공적으로 나갔습니다.")
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = "그룹 나가기 중 오류가 발생했습니다: ${e.message}"
                _isLoading.value = false
                Log.e("SettingGroupViewModel", "그룹 나가기 중 예외 발생", e)
            }
        }
    }

    /**
     * Firebase에서 최신 사용자 정보를 가져와 세션을 갱신한다. 실패해도 흐름은 계속된다.
     * 서버의 User에는 로그인 토큰이 없으므로 loginUser가 아니라 updateUser(토큰 유지)를 쓴다.
     */
    private suspend fun refreshSessionUser(userId: String) {
        try {
            val updatedUserResult = userRepository.getUserById(userId)
                .filter { it !is DataResourceResult.Loading }
                .first()
            when (updatedUserResult) {
                is DataResourceResult.Success -> {
                    updatedUserResult.data?.let { sessionManager.updateUser(it) }
                }
                is DataResourceResult.Failure -> {
                    Log.e("SettingGroupViewModel", "최신 사용자 정보 가져오기 실패", updatedUserResult.exception)
                }
                else -> Unit
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SettingGroupViewModel", "사용자 정보 업데이트 실패", e)
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

    /**
     * 그룹 비밀번호를 변경한다.
     *
     * 그룹 비밀번호는 개인 자격증명이 아니라 구성원이 공유하는 참여 관문이며,
     * 현재 값은 그룹 설정 화면에 그대로 표시된다.
     * 따라서 변경 시 현재 비밀번호를 다시 입력받지 않는다 — 화면에 보이는 값을
     * 옮겨 적게 하는 것은 방어가 아니라 마찰일 뿐이고, 잊었을 때 막다른 길이 된다.
     * 실수로 인한 변경은 화면의 확인 다이얼로그가 막는다.
     *
     * 외부인의 무단 변경은 클라이언트가 아니라 Firestore 보안 규칙으로 막아야 한다.
     */
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

            // 같은 값으로의 변경은 사용자의 착각이므로 불필요한 쓰기를 막는다.
            if (newPassword == currentGroup.groupPw) {
                _errorMessage.value = "새 비밀번호가 현재 비밀번호와 같습니다."
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = "그룹 비밀번호 변경 중 오류가 발생했습니다: ${e.message}"
                Log.e("SettingGroupViewModel", "그룹 비밀번호 변경 중 예외 발생", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}