package com.ahn.ggriggri.screen.ui.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.NotificationPermissionChecker
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.ahn.domain.repository.AuthRepository
import com.ahn.domain.repository.FCMRepository
import com.ahn.domain.repository.UserRepository
import com.ahn.domain.repository.GroupRepository
import com.ahn.ggriggri.BuildConfig
import com.ahn.ggriggri.auth.EnsureAuthResult
import com.ahn.ggriggri.auth.EnsureFirebaseAuthUseCase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val notificationPermissionChecker: NotificationPermissionChecker,
    private val fcmRepository: FCMRepository,
    private val authRepository: AuthRepository,
    private val ensureFirebaseAuth: EnsureFirebaseAuthUseCase,
) : ViewModel() {

    private val _loginStatus = MutableStateFlow("")
    val loginStatus: StateFlow<String> = _loginStatus

    val currentUserId: StateFlow<String?> = sessionManager.currentUserFlow
        .map { it?.userId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _notificationPermissionState = MutableStateFlow(false)
    val notificationPermissionState: StateFlow<Boolean> = _notificationPermissionState.asStateFlow()


    init {
        viewModelScope.launch {
            // 자동로그인 로직
            currentUserId.collect { value ->
                Log.d("OAuthViewModel", "currentUserId updated: $value")
            }
        }
        _notificationPermissionState.value = notificationPermissionChecker.hasNotificationPermission()
    }

    /**
     * 알림 권한을 요청해야 하는지(Android 13+ 이고 아직 미허용).
     * 실제 요청은 Activity가 필요하므로 화면에서 ActivityResult 런처로 수행한다.
     */
    fun shouldRequestNotificationPermission(): Boolean =
        notificationPermissionChecker.shouldRequestNotificationPermission()

    fun handlePermissionResult(isGranted: Boolean) {
        _notificationPermissionState.value = isGranted
        if (isGranted) {
            Log.d("LoginViewModel", "Notification permission granted")
            // 권한이 허용되었을 때 처리
        } else {
            Log.d("LoginViewModel", "Notification permission denied")
            // 권한이 거부되었을 때 처리
        }
    }

    fun handleKakaoLogin(context: Context) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = kakaoLoginCallback)
        } else {
            loginKakaoLoginWithAccount(context)
        }
    }

    private fun loginKakaoLoginWithAccount(context: Context) {
        UserApiClient.instance.loginWithKakaoAccount(context, callback = kakaoLoginCallback)
    }

    /**
     * 카카오 로그인 콜백
     */
    private val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            _loginStatus.value = "로그인 실패 ${error.message}"
            Log.e("oauth", "Fail")
        } else if (token != null) {
            // 여기서는 "성공"을 알리지 않는다. Firebase Auth 로그인과 사용자 저장이 끝나야 성공이다.
            _loginStatus.value = "로그인 처리 중..."
            fetchKakaoUserInfoAndProcessLogin(token)
            Log.d("oauth", "Kakao token received")
        }
    }


    private fun fetchKakaoUserInfoAndProcessLogin(kakaoOAuthToken: OAuthToken) {
        UserApiClient.instance.me { kakaoUser, error ->
            if (error != null) {
                Log.e("oauth", "사용자 정보 요청 실패: ${error.message}")
                _loginStatus.value = "사용자 정보 요청 실패"
                return@me
            }
            if (kakaoUser == null) {
                Log.e("oauth", "사용자 정보가 null")
                _loginStatus.value = "사용자 정보가 null"
                return@me
            }

            val profile = kakaoUser.kakaoAccount?.profile

            viewModelScope.launch {
                completeKakaoLogin(
                    accessToken = kakaoOAuthToken.accessToken,
                    userId = kakaoUser.id.toString(),
                    nickname = profile?.nickname,
                    profileImageUrl = profile?.profileImageUrl,
                )
            }
        }
    }

    /**
     * 카카오 로그인 후속 처리. Firestore(사용자 조회/생성)에 닿기 전에 반드시 Firebase Auth 로그인부터 한다.
     * Auth에 실패하면 로그인 실패 메시지만 남기고 Firestore 접근도, 세션 생성도 하지 않는다.
     * (Kakao SDK 싱글턴에 의존하지 않는 부분이라 단위 테스트가 가능하다.)
     */
    internal suspend fun completeKakaoLogin(
        accessToken: String,
        userId: String,
        nickname: String?,
        profileImageUrl: String?,
    ) {
        if (!signInToFirebase(accessToken)) return

        // 전체 사용자 목록이 아니라 본인 문서만 읽는다(보안 규칙상 본인 문서만 조회 가능).
        val userReadResult = userRepository.getUserByIdSync(userId)

        // 사용자 객체(토큰 포함)는 로그로 남기지 않는다.
        val existingUser = when (userReadResult) {
            is DataResourceResult.Success -> userReadResult.data

            is DataResourceResult.Failure -> {
                // 조회에 실패했는데 "없는 사용자"로 보고 create()하면 기존 문서를 덮어쓸 수 있으므로 중단한다.
                Log.e("OAuthViewModel", "Firestore read failed", userReadResult.exception)
                _loginStatus.value = "회원 정보 조회 실패: ${userReadResult.exception.message}"
                return
            }

            else -> null
        }
        // update()는 User의 전 필드를 Firestore에 덮어쓴다.
        // 그래서 기존 사용자는 반드시 copy()로 만들어야 userState/userJoinTime/userFcmCode가 보존된다.
        // 새로 생성할 때만 가입 시각을 기록한다.
        val appUser = existingUser?.copy(
            userName = nickname ?: existingUser.userName,
            userProfileImage = profileImageUrl ?: existingUser.userProfileImage,
            userAutoLoginToken = accessToken
        ) ?: User(
            userId = userId,
            userName = nickname ?: "UnKnown",
            userProfileImage = profileImageUrl ?: "",
            userJoinTime = System.currentTimeMillis(),
            userGroupDocumentId = "",
            userAutoLoginToken = accessToken
        )
        // 토큰은 toFirestoreUserDTO에서 빠지므로 서버에는 저장되지 않고, 아래 loginUser로 로컬 세션에만 저장된다.
        val dbOperationResultFlow = if (existingUser != null) {
            userRepository.update(appUser)
        } else {
            userRepository.create(appUser)
        }

        val dbOperationResult = dbOperationResultFlow
            .filter { it !is DataResourceResult.Loading }
            .first()

        when (dbOperationResult) {
            is DataResourceResult.Success -> {
                sessionManager.loginUser(appUser)
                fcmRepository.sendTokenToServer(appUser.userId)
                _loginStatus.value =
                    if (existingUser != null) "회원 정보 업데이트 성공" else "회원 정보 생성 성공"
                Log.d("oauth", _loginStatus.value) // 생성 또는 업데이트 로그 확인
            }

            is DataResourceResult.Failure -> {
                val failMsg = "회원 정보 저장/업데이트 실패: ${dbOperationResult.exception.message}"
                _loginStatus.value = failMsg
                Log.e("oauth", failMsg, dbOperationResult.exception)
            }

            is DataResourceResult.Loading -> { /* 이미 first()로 처리됨 */
            }

            else -> {}
        }
    }

    /** 카카오 access token으로 Firebase Auth에 로그인한다. 실패하면 로그인 실패 메시지를 남기고 false. */
    private suspend fun signInToFirebase(accessToken: String): Boolean {
        val result = authRepository.signInWithKakaoToken(accessToken)
        if (result.isSuccess) return true
        // 토큰/ID가 섞일 수 있는 메시지는 화면과 로그에 그대로 쓰지 않는다.
        Log.e("oauth", "Firebase Auth 로그인 실패: ${result.exceptionOrNull()?.javaClass?.simpleName}")
        _loginStatus.value = "로그인 실패: 인증 서버 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요."
        return false
    }

    /**
     * @param requireFirebaseAuth true면 Firestore를 읽기 전에 Firebase Auth 로그인 상태를 보장한다.
     *  카카오 세션만 있고 Auth는 로그아웃된 기존 사용자는 여기서 조용히 로그인된다.
     *  개발용 로그인(DevLogin, 디버그 빌드 전용)은 카카오 토큰이 없어 false로 부른다.
     */
    fun checkUserGroupAndNavigate(
        onNavigationToGroup: () -> Unit,
        onNavigationToHome: () -> Unit,
        requireFirebaseAuth: Boolean = true,
    ) {
        viewModelScope.launch {
            val currentUser = sessionManager.currentUserFlow.first()
            if (currentUser == null) {
                Log.d("OAuthViewModel", "No user logged in, cannot check group.")
                return@launch
            }

            if (requireFirebaseAuth && !ensureFirebaseAuthOrReport()) return@launch

            val latestUserResult = userRepository.getUserById(currentUser.userId)
                .filter { it !is DataResourceResult.Loading }
                .first()


            when (latestUserResult) {
                is DataResourceResult.Success -> {
                    val latestUser = latestUserResult.data
                    if (latestUser != null) {
                        // 서버의 User에는 토큰이 없으므로 로컬 세션 토큰을 유지한 채 사용자 정보만 갱신한다.
                        sessionManager.updateUser(latestUser)
                        val groupId = latestUser.userGroupDocumentId
                        if (groupId.isNullOrEmpty()) {
                            onNavigationToGroup()
                        } else {
                            onNavigationToHome()
                        }
                    }
                }
                else -> {
                    val groupId = sessionManager.currentUserGroupIdFlow.first()
                    if (groupId.isNullOrBlank()) {
                        onNavigationToGroup()
                    } else {
                        onNavigationToHome()
                    }
                }
            }
        }
    }

    /**
     * 세션이 있는 사용자의 Firebase Auth 로그인을 보장한다. 진행해도 되면 true.
     * - 카카오 토큰이 없거나 서버가 거부(만료): 로컬 세션을 지우고 재로그인을 안내한다(Firestore는 어차피 거부된다).
     * - 일시 오류(네트워크/서버): 세션은 유지하고 안내만 한 뒤 중단한다. 로그인 화면에서 다시 시도하면 된다.
     */
    internal suspend fun ensureFirebaseAuthOrReport(): Boolean =
        when (ensureFirebaseAuth()) {
            EnsureAuthResult.AlreadySignedIn, EnsureAuthResult.SignedIn -> true
            EnsureAuthResult.NeedsRelogin -> {
                sessionManager.logoutUser()
                _loginStatus.value = "로그인이 만료되었습니다. 다시 로그인해 주세요."
                false
            }
            EnsureAuthResult.TemporaryFailure -> {
                _loginStatus.value = "인증 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요."
                false
            }
        }

    // 개발용 로그인 함수.
    // 카카오 토큰이 없어 Firebase Auth 로그인을 만들 수 없으므로 Auth 없이 진행한다. 따라서 Firestore 규칙이
    // 인증을 요구하는 환경에서는 동작하지 않으며, 규칙이 열려 있는 개발 환경에서만 쓸 수 있다.
    // 프로덕션에서 규칙을 우회하는 경로가 되지 않도록 디버그 빌드에서만 실행한다(릴리스에서는 즉시 거절).
    fun handleDevLogin(
        userId: String,
        userName: String,
        userProfileImage: String,
        userGroupDocumentId: String,
        userAutoLoginToken: String,
    ) {
        if (!BuildConfig.DEBUG) {
            _loginStatus.value = "개발용 로그인은 디버그 빌드에서만 사용할 수 있습니다."
            return
        }
        viewModelScope.launch {
            _loginStatus.value = "개발용 로그인 처리 중..."

            try {
                // 기존 사용자 확인
                val existingUser = userRepository.getUserByIdSync(userId)

                val existingUserData =
                    (existingUser as? DataResourceResult.Success)?.data

                // 카카오 로그인과 동일하게, 기존 사용자는 copy()로 만들어 필드 유실을 막는다.
                val devUser = existingUserData?.copy(
                    userName = userName,
                    userProfileImage = userProfileImage,
                    userGroupDocumentId = userGroupDocumentId,
                    userAutoLoginToken = userAutoLoginToken
                ) ?: User(
                    userId = userId,
                    userName = userName,
                    userProfileImage = userProfileImage,
                    userJoinTime = System.currentTimeMillis(),
                    userGroupDocumentId = userGroupDocumentId,
                    userAutoLoginToken = userAutoLoginToken
                )

                // 카카오 로그인과 마찬가지로 토큰은 서버에 저장되지 않고 loginUser로 로컬 세션에만 저장된다.
                val dbOperationResultFlow =
                    if (existingUserData != null) {
                        userRepository.update(devUser)
                    } else {
                        userRepository.create(devUser)
                    }

                val dbOperationResult = dbOperationResultFlow
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                Log.d("OAuthViewModel", "개발용 로그인 DB 결과: $dbOperationResult")

                when (dbOperationResult) {
                    is DataResourceResult.Success -> {
                        Log.d("OAuthViewModel", "개발용 로그인 성공")

                        // 그룹 ID가 있으면 그룹에 사용자 추가
                        if (userGroupDocumentId.isNotEmpty()) {
                            Log.d(
                                "OAuthViewModel",
                                "그룹에 사용자 추가 시도 - 그룹ID: $userGroupDocumentId, 사용자ID: $userId"
                            )
                            val groupResult =
                                groupRepository.addUserToGroup(userGroupDocumentId, userId)
                                    .filter { it !is DataResourceResult.Loading }
                                    .first()

                            when (groupResult) {
                                is DataResourceResult.Success -> {
                                    Log.d("OAuthViewModel", "그룹에 사용자 추가 성공")
                                }

                                is DataResourceResult.Failure -> {
                                    Log.e("OAuthViewModel", "그룹에 사용자 추가 실패", groupResult.exception)
                                    _loginStatus.value =
                                        "그룹 추가 실패: ${groupResult.exception.message}"
                                    return@launch
                                }

                                else -> {
                                    Log.w("OAuthViewModel", "그룹에 사용자 추가 결과: $groupResult")
                                }
                            }
                        }

                        sessionManager.loginUser(devUser)
                        fcmRepository.sendTokenToServer(devUser.userId)
                        _loginStatus.value = "개발용 로그인 성공"
                        Log.d("OAuthViewModel", "개발용 로그인 완료 - userId: ${devUser.userId}")
                    }

                    is DataResourceResult.Failure -> {
                        val failMsg = "개발용 로그인 실패: ${dbOperationResult.exception.message}"
                        _loginStatus.value = failMsg
                        Log.e("OAuthViewModel", failMsg, dbOperationResult.exception)
                    }

                    else -> {
                        _loginStatus.value = "개발용 로그인 처리 중..."
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = "개발용 로그인 중 오류 발생: ${e.message}"
                _loginStatus.value = errorMsg
                Log.e("OAuthViewModel", errorMsg, e)
            }
        }
    }
}
