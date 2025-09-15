package com.ahn.ggriggri.screen.ui.setting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.ahn.domain.model.Group
import com.ahn.domain.repository.UserRepository
import com.ahn.domain.repository.GroupRepository
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
    private val groupRepository: GroupRepository
): ViewModel(){

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    val uiState: StateFlow<MyPageUiState> = combine(
        sessionManager.currentUserFlow,
        _currentGroup
    ) { user, group ->
        MyPageUiState(user = user, group = group, isLoading = false)
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
            } catch (e: Exception) {
                _currentGroup.value = null
            }
        }
    }

    fun updateUserProfileData(newName: String, newProfileImageUrl: String) {
        viewModelScope.launch {
            val currentUser = uiState.value.user
            if (currentUser != null) {
                sessionManager.updateUserProfile(newName, newProfileImageUrl)
            }
            val updateUserForFirestore = currentUser!!.copy(
                userName = newName,
                userProfileImage = newProfileImageUrl
            )
        }
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
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