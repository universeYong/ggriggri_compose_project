package com.ahn.ggriggri.screen.ui.main.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Singleton
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class WorkSchedulerImpl @AssistedInject constructor(
    @ApplicationContext private val context: Context
) : AppWorkScheduler {

    companion object {
        private const val TAG = "WorkSchedulerImpl"
    }

    override fun scheduleDailyTasks() {
        scheduleDailyQuestionWorker()
    }

    private fun scheduleDailyQuestionWorker() {
        val workManager = WorkManager.getInstance(context)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyQuestionWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(DailyQuestionWorker.TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DailyQuestionWorker.TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
        Log.d(TAG, "DailyQuestionWorker scheduled with initial delay: $initialDelay ms")
    }
}