package com.ahn.ggrigggri

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.v2.common.BuildConfig

//Default Memory Size = 0.15 ~ 0.2
const val COIL_MEMORY_CACHE_SIZE_PRECENT = 0.1 // 10% 사용량 많으면 25%~30%사용

// Coil Disk Cache Size Setting
const val COIL_DISK_CACHE_DIR_NAME = "coil_file_cache"
const val COIL_DISK_CACHE_MAX_SIZE = 1024 * 1024 * 30

class ggriggriAplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        seSacApplication = this

        KakaoSdk.init(this,"fdf59a777205057ffca042069bd2284d")

//        Log.e("TAG", KakaoSdk.keyHash)
    }
    companion object{
        private lateinit var seSacApplication: ggriggriAplication
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