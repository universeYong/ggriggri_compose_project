package com.ahn.ggriggri.screen.ui.setting.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.ahn.domain.model.Group
import com.ahn.domain.repository.UserRepository
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.FCMRepository
import kotlinx.coroutines.withTimeoutOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.ahn.domain.common.DataResourceResult


data class MyPageUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val group: Group? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val fcmRepository: FCMRepository,
): ViewModel(){

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MyPageUiState> = combine(
        sessionManager.currentUserFlow,
        _currentGroup,
        _errorMessage
    ) { user, group, error ->
        MyPageUiState(user = user, group = group, isLoading = false, errorMessage = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyPageUiState(isLoading = true)
    )

    init {
        // currentUser를 관찰하여 사용자 정보가 변경될 때마다 그룹 정보 로드
        viewModelScope.launch {
            sessionManager.currentUserFlow.collect { user ->
                if (user != null) {
                    loadCurrentGroupForUser(user)
                } else {
                    _currentGroup.value = null
                }
            }
        }
    }

    // 외부에서 그룹 정보를 직접 설정할 수 있는 함수
    fun updateGroupInfo(group: Group?) {
        _currentGroup.value = group
    }

    private fun loadCurrentGroupForUser(user: User) {
        viewModelScope.launch {
            try {
                val groupId = user.userGroupDocumentId
                if (groupId.isNullOrEmpty()) {
                    _currentGroup.value = null
                    return@launch
                }

                val result = groupRepository.getGroupById(groupId)
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                when (result) {
                    is DataResourceResult.Success -> {
                        _currentGroup.value = result.data
                    }
                    is DataResourceResult.Failure -> {
                        _currentGroup.value = null
                    }
                    else -> {
                        _currentGroup.value = null
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _currentGroup.value = null
            }
        }
    }

    fun updateUserProfileData(newName: String, newProfileImageUrl: String) {
        viewModelScope.launch {
            val currentUser = sessionManager.currentUserFlow.first()
            if (currentUser == null) {
                _errorMessage.value = "로그인이 필요합니다."
                return@launch
            }
            val updatedUser = currentUser.copy(
                userName = newName,
                userProfileImage = newProfileImageUrl
            )
            // 서버(Firestore)에 먼저 저장하고, 성공했을 때만 로컬 세션을 갱신한다.
            val result = userRepository.update(updatedUser)
                .first { it is DataResourceResult.Success || it is DataResourceResult.Failure }
            when (result) {
                is DataResourceResult.Success -> {
                    sessionManager.updateUserProfile(newName, newProfileImageUrl)
                    _errorMessage.value = null
                }
                is DataResourceResult.Failure -> {
                    _errorMessage.value = "프로필 저장에 실패했습니다: ${result.exception.message}"
                }
                else -> { }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            // 세션을 지우기 전에 이 기기의 FCM 토큰을 서버에서 정리한다.
            // 네트워크가 느려도 로그아웃이 막히지 않도록 시간 제한을 두고, 실패해도 계속 진행한다.
            runCatching {
                withTimeoutOrNull(3_000) { fcmRepository.unregisterCurrentUserToken() }
            }.onFailure {
                // 타임아웃은 withTimeoutOrNull 안에서 처리된다. 외부 취소까지 삼키면 안 되므로 다시 던진다.
                if (it is kotlinx.coroutines.CancellationException) throw it
                Log.e("MyPageViewModel", "FCM 토큰 정리 실패", it)
            }
            sessionManager.logoutUser()
            onNavigateToLogin()
        }
    }

    fun refreshGroupInfo() {
        viewModelScope.launch {
            val currentUser = sessionManager.currentUserFlow.first()
            if (currentUser != null) {
                loadCurrentGroupForUser(currentUser)
            }
        }
    }
}