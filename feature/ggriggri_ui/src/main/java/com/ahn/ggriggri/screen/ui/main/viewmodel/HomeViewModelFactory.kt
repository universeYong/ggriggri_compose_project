package com.ahn.ggriggri.screen.ui.main.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository

class HomeViewModelFactory(
    private val application: Application, // AndroidViewModel을 위해 필요할 수 있음
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository, // ggriggriAplication에서 주입받음
    private val groupRepository: GroupRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, sessionManager, userRepository, groupRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}