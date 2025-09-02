package com.ahn.ggrigggri

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.add
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import com.ahn.data.datasource.GroupDataSource
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.local.SessionManagerImpl
import com.ahn.data.remote.firebase.FirestoreGroupDataSourceImpl
import com.ahn.data.remote.firebase.FirestoreUserDataSourceImpl
import com.ahn.data.repository.FirestoreGroupRepositoryImpl
import com.ahn.data.repository.FirestoreUserRepositoryImpl
import com.ahn.domain.common.SessionManager
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModelFactory
import com.ahn.ggriggri.screen.ui.main.viewmodel.HomeViewModelFactory
import com.ahn.ggriggri.screen.ui.setting.viewmodel.factory.MyPageViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.common.KakaoSdk
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

//Default Memory Size = 0.15 ~ 0.2
const val COIL_MEMORY_CACHE_SIZE_PRECENT = 0.1 // 10% 사용량 많으면 25%~30%사용

// Coil Disk Cache Size Setting
const val COIL_DISK_CACHE_DIR_NAME = "coil_file_cache"
const val COIL_DISK_CACHE_MAX_SIZE = 1024 * 1024 * 30

class ggriggriAplication : Application(), SingletonImageLoader.Factory {

    val appContainer by lazy { AppContainer(this) }

    // Moshi 인스턴스 (앱 전체에서 공유)
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // SessionManager 인스턴스 (앱 전체에서 공유)
    val sessionManager: SessionManager by lazy {
        SessionManagerImpl.getInstance(applicationContext, moshi) // SessionManager 생성자에 context와 moshi 전달
    }

    // UserDataSource 구현체
    private val userDataSource: UserDataSource by lazy {
        FirestoreUserDataSourceImpl()
    }

    // UserRepository 구현체 (앱 전체에서 공유 가능)
    val userRepository: UserRepository by lazy {
        FirestoreUserRepositoryImpl(userDataSource)
    }

    private val groupDataSource: GroupDataSource by lazy {
        FirestoreGroupDataSourceImpl()
    }

    val groupRepository: GroupRepository by lazy {
        FirestoreGroupRepositoryImpl(groupDataSource)
    }

    override fun onCreate() {
        super.onCreate()
//        seSacApplication = this
//        sessionManager = SessionManager(applicationContext)

        KakaoSdk.init(this,"fdf59a777205057ffca042069bd2284d")

//        Log.e("TAG", KakaoSdk.keyHash)
    }
//    companion object{
//        private lateinit var seSacApplication: ggriggriAplication
//    }

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

// 별도의 파일 또는 Application 클래스 내부에 정의 가능
class AppContainer(applicationContext: Context) {
    private val application = applicationContext as ggriggriAplication

    // 필요한 의존성들을 여기서 제공
    val moshi: Moshi get() = application.moshi
    val sessionManager: SessionManager get() = application.sessionManager
    val userRepository: UserRepository get() = application.userRepository
    val groupRepository: GroupRepository get() = application.groupRepository
    // ... 기타 필요한 의존성 ...

    // ViewModel Factory 생성 메소드들을 AppContainer에 추가할 수도 있습니다.
    fun provideOAuthViewModelFactory(): OAuthViewModelFactory {
        return OAuthViewModelFactory(application, sessionManager,userRepository)
    }

    fun provideMyPageViewModelFactory(): MyPageViewModelFactory {
        return MyPageViewModelFactory(application, sessionManager, userRepository)
    }

    fun provideHomeViewModelFactory(): HomeViewModelFactory {
        return HomeViewModelFactory(application,sessionManager,userRepository, groupRepository)
    }
}
