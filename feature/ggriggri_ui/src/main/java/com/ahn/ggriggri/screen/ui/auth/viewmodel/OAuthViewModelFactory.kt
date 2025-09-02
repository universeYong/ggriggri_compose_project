package com.ahn.ggriggri.screen.ui.auth.viewmodel

import android.app.Application
import androidx.compose.foundation.layout.add
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.UserRepository


class OAuthViewModelFactory(
    private val application: Application, // AndroidViewModel을 위해 필요할 수 있음
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository // ggriggriAplication에서 주입받음
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OAuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 주입받은 userRepository와 sessionManager를 사용
            // OAuthViewModel의 생성자가 application, userRepository, sessionManager를 받는다고 가정
            return OAuthViewModel(application, sessionManager, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}