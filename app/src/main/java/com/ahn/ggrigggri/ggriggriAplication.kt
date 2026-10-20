package com.ahn.ggrigggri

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import com.ahn.domain.notification.NotificationChannelManager
import com.ahn.ggriggri.screen.ui.main.worker.AppWorkScheduler
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject


//Default Memory Size = 0.15 ~ 0.2
const val COIL_MEMORY_CACHE_SIZE_PRECENT = 0.1 // 10% 사용량 많으면 25%~30%사용

// Coil Disk Cache Size Setting
const val COIL_DISK_CACHE_DIR_NAME = "coil_file_cache"
const val COIL_DISK_CACHE_MAX_SIZE = 1024 * 1024 * 30

@HiltAndroidApp
class ggriggriAplication : Application(), SingletonImageLoader.Factory,
    Configuration.Provider{

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appWorkScheduler: AppWorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory) // 주입받은 HiltWorkerFactory 사용
            .setMinimumLoggingLevel(Log.DEBUG) // 최소 로깅 레벨 설정 유지
            .build()

    override fun onCreate() {
        super.onCreate()

        notificationChannelManager.createNotificationChannels()

        // 카카오 SDK는 로그인 API를 쓰기 전에 반드시 초기화되어야 한다.
        // 키는 secret.properties → BuildConfig 경로로 주입된다.
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 매일 자정에 오늘의 질문을 만드는 Worker 예약(KEEP이라 이미 예약돼 있으면 그대로 둔다).
        // 질문 문서 ID가 {groupId}_{yyyyMMdd}로 고정돼 있어 그룹원 기기마다 실행돼도 질문과 푸시는 한 번만 생긴다.
        appWorkScheduler.scheduleDailyTasks()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, COIL_MEMORY_CACHE_SIZE_PRECENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(filesDir.resolve(COIL_DISK_CACHE_DIR_NAME))
                    .maximumMaxSizeBytes(COIL_DISK_CACHE_MAX_SIZE.toLong())
                    .build()
            }.build()
}
