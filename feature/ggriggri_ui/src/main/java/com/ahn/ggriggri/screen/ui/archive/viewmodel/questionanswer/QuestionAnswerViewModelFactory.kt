package com.ahn.ggriggri.screen.ui.archive.viewmodel.questionanswer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.ahn.domain.repository.AnswerRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.UserRepository

class QuestionAnswerViewModelFactory(
    private val application: Application,
    private val questionRepository: QuestionRepository,
    private val questionListRepository: QuestionListRepository,
    private val answerRepository: AnswerRepository,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(QuestionAnswerViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return QuestionAnswerViewModel(
                application, savedStateHandle, questionRepository, questionListRepository,
                answerRepository, userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}