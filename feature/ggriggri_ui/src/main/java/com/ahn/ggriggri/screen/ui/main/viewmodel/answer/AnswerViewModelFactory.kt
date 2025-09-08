package com.ahn.ggriggri.screen.ui.main.viewmodel.answer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahn.data.local.TodayQuestionPreferencesImpl
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.AnswerRepository
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.UserRepository

class AnswerViewModelFactory(
    private val application: Application,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val questionRepository: QuestionRepository,
    private val questionListRepository: QuestionListRepository,
    private val answerRepository: AnswerRepository,
    private val todayQuestionPreferencesImpl: TodayQuestionPreferencesImpl
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnswerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnswerViewModel(application, sessionManager, userRepository,
                groupRepository,questionRepository,questionListRepository,answerRepository,
                todayQuestionPreferencesImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}