package com.ahn.ggriggri.screen.ui.setting.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.UserRepository
import com.ahn.ggriggri.screen.ui.setting.viewmodel.MyPageViewModel

class MyPageViewModelFactory(
    private val application: Application,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyPageViewModel(application, sessionManager, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}