package com.ahn.ggriggri.screen.ui.auth.viewmodel

import android.content.Context
import android.service.autofill.UserData
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.ahn.data.datasource.UserDataSource
import com.ahn.data.remote.firebase.FirestoreUserDataSourceImpl
import com.ahn.data.repository.FirestoreUserRepositoryImpl
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.model.User
import com.ahn.domain.repository.UserRepository
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OAuthViewModel: ViewModel() {

    private val _loginStatus = MutableStateFlow("")
    val loginStatus: StateFlow<String> = _loginStatus

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val remoteDataSource: UserDataSource = FirestoreUserDataSourceImpl()
    private val userRepository: UserRepository = FirestoreUserRepositoryImpl(remoteDataSource)

    init {
        viewModelScope.launch {
            currentUserId.collect { value ->
                Log.d("OAuthViewModel", "currentUserId updated: $value")
            }
        }
    }

    private fun setCurrentUserId(userId: String) {
        Log.d("OAuthViewModel", "setCurrentUserId: $userId")
        _currentUserId.value = userId
    }

    fun handleKakaoLogin(context: Context) {
        /**
         * 카카오톡 설치 여부 확인
         */
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            /**
             * 카카오톡으로 로그인
             */
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    _loginStatus.value = "로그인 실패"
                    Log.e("_oauth", "Fail: ${error.message}", error)

                    /**
                     * 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우
                     * 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                     */
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    /**
                     * 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                     */
                    loginKakaoLoginWithAccount(context)
                } else if (token != null) {
                    _loginStatus.value = "로그인 성공"
                    fetchUserInfoAndSave()
                    Log.d("_oauth", "Success")

                }
            }
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
            fetchUserInfoAndSave()
            Log.d("oauth", "Success")
        }
    }
    // 로그인시 read로 읽어와서 없으면 create 있으면 update
//    private fun fetchUserInfoAndSave() {
//        UserApiClient.instance.me { user, error ->
//            if (error != null) {
//                Log.e("oauth", "사용자 정보 요청 실패: ${error.message}")
//                _loginStatus.value = "사용자 정보 요청 실패"
//            } else if (user != null) {
//
//                val kakaoAccount = user.kakaoAccount
//                val profile = kakaoAccount?.profile
//
//                val appUser = User(
//                    userId = user.id.toString(),
//                    userName = profile?.nickname ?: "UnKnown",
//                    userProfileImage = profile?.profileImageUrl ?: ""
//                )
//                Log.d("OAuthViewModel", "fetchUserInfoAndSave: userId=${user.id}")
//                setCurrentUserId(user.id.toString())
//                Log.d("OAuthViewModel", "setCurrentUserId 호출 완료")
//
//                viewModelScope.launch {
//                    userRepository.create(appUser).collect { result ->
//                        when (result) {
//                            is DataResourceResult.Loading -> {
//                                _loginStatus.value = "회원 정보 저장 중..."
//                            }
//
//                            is DataResourceResult.Success -> {
//                                _loginStatus.value = "로그인 및 회원 정보 저장 성공"
//                                Log.d("oauth", "회원 정보 저장 성공")
//                            }
//
//                            is DataResourceResult.Failure -> {
//                                _loginStatus.value = "회원 정보 저장 실패: ${result.exception.message}"
//                                Log.e("oauth", "회원 정보 저장 실패", result.exception)
//                            }
//
//                            else -> {}
//                        }
//                    }
//                }
//
//            }
//        }
//    }

    private fun fetchUserInfoAndSave() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("oauth", "사용자 정보 요청 실패: ${error.message}")
                _loginStatus.value = "사용자 정보 요청 실패"
                return@me
            }
            if (user == null) {
                Log.e("oauth", "사용자 정보가 null")
                _loginStatus.value = "사용자 정보가 null"
                return@me
            }

            val userIdString = user.id.toString()
            val kakaoAccount = user.kakaoAccount
            val profile = kakaoAccount?.profile

            viewModelScope.launch {
                // 1) Firestore에서 사용자 존재 여부 확인
                val userReadResult = userRepository.read().first() // read() 함수 구현에 따라 userId로 필터링하거나 전체 목록 조회

                val existingUser = when(userReadResult) {
                    is DataResourceResult.Success -> userReadResult.data.find { it.userId == userIdString }
                    else -> null
                }

                val appUser = User(
                    userId = userIdString,
                    userName = profile?.nickname ?: "UnKnown",
                    userProfileImage = profile?.profileImageUrl ?: "",
                    // 만약 User 모델에 userGroupDocumentId가 있다면 기존 값을 유지하도록 여기에 포함
                    userGroupDocumentId = existingUser?.userGroupDocumentId ?: ""
                )

                setCurrentUserId(userIdString)

                if(existingUser != null) {
                    // 2) 이미 있을 경우 update() 호출 (필드 부분 수정)
                    userRepository.update(appUser).collect { result ->
                        when(result) {
                            is DataResourceResult.Success -> {
                                _loginStatus.value = "로그인 및 회원 정보 업데이트 성공"
                                Log.d("oauth", "회원 정보 업데이트 성공")
                            }
                            is DataResourceResult.Failure -> {
                                _loginStatus.value = "회원 정보 업데이트 실패: ${result.exception.message}"
                                Log.e("oauth", "회원 정보 업데이트 실패", result.exception)
                            }
                            else -> {}
                        }
                    }
                } else {
                    // 3) 없으면 create() 호출
                    userRepository.create(appUser).collect { result ->
                        when(result) {
                            is DataResourceResult.Success -> {
                                _loginStatus.value = "로그인 및 회원 정보 생성 성공"
                                Log.d("oauth", "회원 정보 생성 성공")
                            }
                            is DataResourceResult.Failure -> {
                                _loginStatus.value = "회원 정보 생성 실패: ${result.exception.message}"
                                Log.e("oauth", "회원 정보 생성 실패", result.exception)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    fun checkUserGroupAndNavigate(
        userId: String,
        onNavigationToGroup: () -> Unit,
        onNavigationToHome: () -> Unit
    ){
        viewModelScope.launch {
            userRepository.getUserGroupDocumentId(userId)
                .collect { result ->
                    when(result) {
                        is DataResourceResult.Success -> {
                            val groupId = result.data
                            if (groupId.isNullOrEmpty()) {
                                onNavigationToGroup()
                            } else {
                                onNavigationToHome()
                            }
                        }

                        is DataResourceResult.Loading -> {}
                        is DataResourceResult.Failure -> {}
                        else -> {}
                    }
                }

        }
    }

}