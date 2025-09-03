package com.ahn.ggriggri.screen.ui.main.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository

class CustomWorkerFactory(
    // 이 의존성들은 Application 클래스에서 생성하여 전달받게 됩니다.
    private val sessionManager: SessionManager,
    private val questionListRepository: QuestionListRepository,
    private val questionRepository: QuestionRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            DailyQuestionWorker::class.java.name ->
                DailyQuestionWorker(
                    appContext,
                    workerParameters,
                    sessionManager,
                    questionListRepository,
                    questionRepository
                )
            else ->
                null
        }
    }
}