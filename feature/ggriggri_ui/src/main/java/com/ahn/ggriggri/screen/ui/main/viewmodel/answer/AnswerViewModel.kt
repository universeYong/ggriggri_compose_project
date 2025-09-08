package com.ahn.ggriggri.screen.ui.main.viewmodel.answer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import com.ahn.domain.repository.UserRepository

class AnswerViewModel(
    application: Application,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val questionRepository: QuestionRepository,
) : AndroidViewModel(application) {

}