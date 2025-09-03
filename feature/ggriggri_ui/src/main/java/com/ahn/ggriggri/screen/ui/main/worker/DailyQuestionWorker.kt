package com.ahn.ggriggri.screen.ui.main.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.Question
import com.ahn.domain.model.QuestionList
import com.ahn.domain.repository.QuestionListRepository
import com.ahn.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DailyQuestionWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val sessionManager: SessionManager, // 실제로는 의존성 주입 필요
    private val questionListRepository: QuestionListRepository, // 실제로는 의존성 주입 필요
    private val questionRepository: QuestionRepository, // 실제로는 의존성 주입 필요
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "DailyQuestionWorker"
        private val BASE_DATE = LocalDate.of(2025, 8, 15) // ViewModel과 동일한 기준 날짜 사용
    }

    override suspend fun doWork(): ListenableWorker.Result { // 반환 타입 명시
        Log.d(TAG, "DailyQuestionWorker started.")

        // runCatching의 결과는 kotlin.Result<ListenableWorker.Result>가 됩니다.
        // 이를 최종적으로 ListenableWorker.Result로 변환해야 합니다.
        val operationResult: kotlin.Result<ListenableWorker.Result> = runCatching {
            val currentGroupId = sessionManager.currentUserGroupIdFlow.first()
            if (currentGroupId.isNullOrEmpty()) {
                Log.w(TAG, "Current group ID is null or empty. Worker cannot proceed.")
                return@runCatching ListenableWorker.Result.failure() // runCatching 람다를 빠져나감
            }

            val todayStartTimestamp = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // 1. 오늘 날짜와 그룹 ID로 Firestore에서 'Question' 문서를 이미 생성했는지 확인
            val questionRecordResult = questionRepository
                .getQuestionForGroupAndDate(currentGroupId, todayStartTimestamp)
                .filter { it !is DataResourceResult.Loading }
                .first()

            when (questionRecordResult) {
                is DataResourceResult.Success -> {
                    if (questionRecordResult.data != null) {
                        Log.d(TAG, "Question record for today already exists for group $currentGroupId.")
                        return@runCatching ListenableWorker.Result.success()
                    }
                    Log.d(TAG, "No question record for today. Proceeding to select and create.")
                }
                is DataResourceResult.Failure -> {
                    Log.e("DailyQuestionWorker", "Error checking existing question: ${questionRecordResult.exception.message}")
                    // 특정 예외 유형에 따라 retry 또는 failure 결정 가능
                    return@runCatching ListenableWorker.Result.retry()
                }
                DataResourceResult.Loading -> {
                    Log.w(TAG, "Received Loading state unexpectedly when checking existing question.")
                    return@runCatching ListenableWorker.Result.retry()
                }
                DataResourceResult.DummyConstructor -> {
                    Log.w(TAG, "Received DummyConstructor state when checking existing question.")
                    return@runCatching ListenableWorker.Result.failure()
                }
            }

            // 2. 모든 질문 후보 (QuestionList) 가져오기
            val allQuestionListsResult = questionListRepository.read()
                .filter { it !is DataResourceResult.Loading }
                .first()

            val questionLists: List<QuestionList> = when (allQuestionListsResult) {
                is DataResourceResult.Success -> {
                    val data = allQuestionListsResult.data ?: emptyList()
                    if (data.isEmpty()) {
                        Log.w(TAG, "Question list is empty. Cannot select a question.")
                        return@runCatching ListenableWorker.Result.failure()
                    }
                    Log.d(TAG, "Successfully fetched ${data.size} question lists.")
                    data
                }
                is DataResourceResult.Failure -> {
                    Log.e("DailyQuestionWorker", "Error fetching question lists: ${allQuestionListsResult.exception.message}")
                    return@runCatching ListenableWorker.Result.retry()
                }
                DataResourceResult.Loading -> {
                    Log.w(TAG, "Received Loading state unexpectedly when fetching question lists.")
                    return@runCatching ListenableWorker.Result.retry()
                }
                DataResourceResult.DummyConstructor -> {
                    Log.w(TAG, "Received DummyConstructor state when fetching lists.")
                    return@runCatching ListenableWorker.Result.failure()
                }
            }

            // 3. 오늘의 질문 선정
            val selectedQuestionList = selectQuestionForTheDay(questionLists)
            if (selectedQuestionList == null) {
                Log.w(TAG, "Failed to select a question for the day.")
                return@runCatching ListenableWorker.Result.failure()
            }
            Log.d(TAG, "Selected question for today: ${selectedQuestionList.content}")

            // 4. 새로운 Question 레코드 생성
            val newQuestionRecord = Question(
                questionCreatedTime = todayStartTimestamp,
                questionGroupDocumentId = currentGroupId,
                questionListDocumentId = selectedQuestionList.number.toString()
            )

            val creationResult = questionRepository.create(newQuestionRecord)
                .filter { it !is DataResourceResult.Loading }
                .first()

            when (creationResult) {
                is DataResourceResult.Success -> {
                    Log.d(TAG, "Successfully created new question record in Firestore.")
                    ListenableWorker.Result.success() // runCatching의 성공 결과
                }
                is DataResourceResult.Failure -> {
                    Log.e("DailyQuestionWorker", "Error creating new question record: ${creationResult.exception.message}")
                    ListenableWorker.Result.retry() // runCatching의 성공 결과 (실패했음을 나타내는 Result.retry)
                }
                DataResourceResult.Loading -> {
                    Log.w(TAG, "Received Loading state unexpectedly when creating question record.")
                    ListenableWorker.Result.retry()
                }
                DataResourceResult.DummyConstructor -> {
                    Log.w(TAG, "Received DummyConstructor state for creationResult.")
                    ListenableWorker.Result.failure()
                }
            }
        } // runCatching 블록 끝

        // operationResult (kotlin.Result)를 ListenableWorker.Result로 변환
        return operationResult.fold(
            onSuccess = { workerResult ->
                // runCatching 내부에서 이미 ListenableWorker.Result를 반환했으므로 그대로 사용
                workerResult
            },
            onFailure = { exception ->
                // runCatching 블록 내부에서 발생한 예외 처리 (예: Flow가 취소되거나 예상치 못한 예외)
                Log.e(TAG, "Error in DailyQuestionWorker's runCatching block", exception)
                // 기본적으로 실패로 처리, 특정 예외에 따라 retry도 가능
                ListenableWorker.Result.failure()
            }
        )
    }

    private fun selectQuestionForTheDay(lists: List<QuestionList>): QuestionList? {
        if (lists.isEmpty()) return null
        val today = LocalDate.now()
        val daysSinceBase = ChronoUnit.DAYS.between(BASE_DATE, today)
        val questionIndex = (daysSinceBase % lists.size).toInt()
        return lists[questionIndex]
    }
}
