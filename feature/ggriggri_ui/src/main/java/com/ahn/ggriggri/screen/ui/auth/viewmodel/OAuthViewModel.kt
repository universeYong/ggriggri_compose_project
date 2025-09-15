package com.ahn.ggriggri.screen.ui.auth.viewmodel

import android.app.Application
import android.content.Context
import android.service.autofill.UserData
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.firebase.FirestoreUserDataSourceImpl
import com.ahn.data.repository.FirestoreUserRepositoryImpl
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import com.ahn.domain.repository.GroupRepository
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {

    private val _loginStatus = MutableStateFlow("")
    val loginStatus: StateFlow<String> = _loginStatus

    val currentUserId: StateFlow<String?> = sessionManager.currentUserFlow
        .map{ it?.userId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            // 자동로그인 로직
            currentUserId.collect { value ->
                Log.d("OAuthViewModel", "currentUserId updated: $value")
            }
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
            _loginStatus.value = "로그인 성공"
            fetchKakaoUserInfoAndProcessLogin(token)
            Log.d("oauth", "Success")
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

            val userIdString = kakaoUser.id.toString()
            val profile = kakaoUser.kakaoAccount?.profile

            viewModelScope.launch {
                val userReadResult =
                    userRepository.read()
                        .filter { it !is DataResourceResult.Loading }
                        .first()
                Log.d(
                    "OAuthViewModel",
                    "Firestore read result (after filtering Loading): $userReadResult"
                )

                val existingUser = when (userReadResult) {
                    is DataResourceResult.Success -> {
                        Log.d(
                            "OAuthViewModel",
                            "Firestore read successful. Data: ${userReadResult.data}"
                        )
                        userReadResult.data.find { it.userId == userIdString }
                    }

                    is DataResourceResult.Failure -> {
                        Log.e(
                            "OAuthViewModel",
                            "Firestore read failed: ${userReadResult.exception}"
                        )
                        null
                    }

                    else -> null
                }
                Log.d(
                    "OAuthViewModel",
                    "existingUser after read: $existingUser, for userIdString: $userIdString"
                )

                val appUser = User(
                    userId = userIdString,
                    userName = profile?.nickname ?: existingUser?.userName ?:"UnKnown",
                    userProfileImage = profile?.profileImageUrl ?: existingUser?.userProfileImage ?: "",
                    userGroupDocumentId = existingUser?.userGroupDocumentId ?: "",
                    userAutoLoginToken = kakaoOAuthToken.accessToken
                )
                Log.d("OAuthViewModel", "Attempting to call userRepository.${if (existingUser != null) "update" else "create"}(appUser).first()")
                val dbOperationResultFlow = if (existingUser != null) {
                    Log.d("OAuthViewModel", "Updating existing user: $appUser")
                    userRepository.update(appUser)
                } else {
                    Log.d("OAuthViewModel", "Creating new user: $appUser")
                    userRepository.create(appUser)
                }

                val dbOperationResult = dbOperationResultFlow
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                Log.d("OAuthViewModel", "dbOperationResult received: $dbOperationResult")

                when (dbOperationResult) {
                    is DataResourceResult.Success -> {
                        Log.d("OAuthViewModel", "Firestore operation successful.")

                        Log.d("OAuthViewModel", "Calling sessionManager.loginUser with user: $appUser") // <--- 로그 추가 권장
                        sessionManager.loginUser(appUser)
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
        }
    }

    fun checkUserGroupAndNavigate(
        onNavigationToGroup: () -> Unit,
        onNavigationToHome: () -> Unit,
    ) {
        viewModelScope.launch {
            val currentUser = sessionManager.currentUserFlow.first()
            if (currentUser == null){
                Log.d("OAuthViewModel", "No user logged in, cannot check group.")
                return@launch
            }

            val groupId = sessionManager.currentUserGroupIdFlow.first()

            if (groupId.isNullOrBlank()) {
                Log.d("OAuthViewModel", "User ${currentUser.userId} has no group ID. Navigating to group setup.")
                onNavigationToGroup()
            } else {
                Log.d("OAuthViewModel", "User ${currentUser.userId} has group ID: $groupId. Navigating to home.")
                onNavigationToHome()
            }
        }
    }

    // 개발용 로그인 함수
    fun handleDevLogin(
        userId: String,
        userName: String,
        userProfileImage: String,
        userGroupDocumentId: String,
        userAutoLoginToken: String
    ) {
        viewModelScope.launch {
            _loginStatus.value = "개발용 로그인 처리 중..."

            try {
                val devUser = User(
                    userId = userId,
                    userName = userName,
                    userProfileImage = userProfileImage,
                    userGroupDocumentId = userGroupDocumentId,
                    userAutoLoginToken = userAutoLoginToken
                )

                Log.d("OAuthViewModel", "개발용 로그인 시도 - 사용자: $devUser")

                // 기존 사용자 확인
                val existingUser = userRepository.getUserByIdSync(userId)
                Log.d("OAuthViewModel", "기존 사용자 확인 결과: $existingUser")

                val dbOperationResultFlow = if (existingUser is DataResourceResult.Success && existingUser.data != null) {
                    Log.d("OAuthViewModel", "기존 사용자 업데이트: $devUser")
                    userRepository.update(devUser)
                } else {
                    Log.d("OAuthViewModel", "새 사용자 생성: $devUser")
                    Log.d("OAuthViewModel", "사용자 생성 시작 - userId: $userId")
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
                            Log.d("OAuthViewModel", "그룹에 사용자 추가 시도 - 그룹ID: $userGroupDocumentId, 사용자ID: $userId")
                            val groupResult = groupRepository.addUserToGroup(userGroupDocumentId, userId)
                                .filter { it !is DataResourceResult.Loading }
                                .first()
                            
                            when (groupResult) {
                                is DataResourceResult.Success -> {
                                    Log.d("OAuthViewModel", "그룹에 사용자 추가 성공")
                                }
                                is DataResourceResult.Failure -> {
                                    Log.e("OAuthViewModel", "그룹에 사용자 추가 실패", groupResult.exception)
                                    _loginStatus.value = "그룹 추가 실패: ${groupResult.exception.message}"
                                    return@launch
                                }
                                else -> {
                                    Log.w("OAuthViewModel", "그룹에 사용자 추가 결과: $groupResult")
                                }
                            }
                        }
                        
                        sessionManager.loginUser(devUser)
                        _loginStatus.value = "개발용 로그인 성공"
                        Log.d("OAuthViewModel", "개발용 로그인 완료 - 사용자: $devUser")
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
            } catch (e: Exception) {
                val errorMsg = "개발용 로그인 중 오류 발생: ${e.message}"
                _loginStatus.value = errorMsg
                Log.e("OAuthViewModel", errorMsg, e)
            }
        }
    }
}
