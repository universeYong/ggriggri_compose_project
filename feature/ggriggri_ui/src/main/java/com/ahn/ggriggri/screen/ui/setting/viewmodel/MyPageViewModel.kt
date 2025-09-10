package com.ahn.ggriggri.screen.ui.setting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// UI 상태를 나타내는 데이터 클래스 (필요에 따라 더 상세하게 정의)
data class MyPageUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val sessionManager: SessionManager,
): ViewModel(){

    val uiState: StateFlow<MyPageUiState> = sessionManager.currentUserFlow
        .map{ user ->
            MyPageUiState(user = user, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MyPageUiState(isLoading = true)
        )

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

    fun logout() {
        viewModelScope.launch {
            sessionManager.logoutUser()
            // 로그아웃 후 필요한 화면으로 이동하는 로직 (Navigation)
        }
    }
}