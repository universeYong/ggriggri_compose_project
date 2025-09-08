package com.ahn.ggriggri.screen.ui.archive.viewmodel.questionlist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository


class QuestionListViewModelFactory(
    private val application: Application,
    private val sessionManager: SessionManager,
    private val questionRepository: QuestionRepository,
    private val questionListRepository: QuestionListRepository,
    ): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionListViewModel(application, sessionManager,questionRepository,
                questionListRepository,) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}