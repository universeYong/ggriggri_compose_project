package com.ahn.ggriggri.screen.ui.auth.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahn.data.fcm.FCMTokenManager
import com.ahn.data.fcm.PermissionManager
import com.ahn.domain.common.DataResourceResult
import com.ahn.domain.common.SessionManager
import com.ahn.domain.model.User
import com.ahn.domain.repository.GroupRepository
import com.ahn.domain.repository.UserRepository
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
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
import jakarta.inject.Inject

@HiltViewModel
class OAuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val permissionManager: PermissionManager,
    private val fcmTokenManager: FCMTokenManager,
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
            currentUserId.collect { value ->
                Log.d("OAuthViewModel", "currentUserId updated: $value")
            }
        }
        _notificationPermissionState.value = permissionManager.hasNotificationPermission()
    }

    fun requestNotificationPermission(activity: Activity) {
        permissionManager.requestNotificationPermission(activity)
    }

    fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            Log.d("LoginViewModel", "Notification permission granted")
        } else {
            Log.d("LoginViewModel", "Notification permission denied")
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

    private val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            _loginStatus.value = "Login failed: ${error.message}"
            Log.e("oauth", "Fail")
        } else if (token != null) {
            _loginStatus.value = "Login success"
            fetchKakaoUserInfoAndProcessLogin(token)
            Log.d("oauth", "Success")
        }
    }

    private fun fetchKakaoUserInfoAndProcessLogin(kakaoOAuthToken: OAuthToken) {
        UserApiClient.instance.me { kakaoUser, error ->
            if (error != null) {
                Log.e("oauth", "Failed to request user info: ${error.message}")
                _loginStatus.value = "Failed to request user info"
                return@me
            }
            if (kakaoUser == null) {
                Log.e("oauth", "User info is null")
                _loginStatus.value = "User info is null"
                return@me
            }

            val userIdString = kakaoUser.id.toString()
            val profile = kakaoUser.kakaoAccount?.profile

            viewModelScope.launch {
                val userReadResult = userRepository.read()
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                Log.d("OAuthViewModel", "Firestore read result: $userReadResult")

                val existingUser = when (userReadResult) {
                    is DataResourceResult.Success -> {
                        Log.d("OAuthViewModel", "Firestore read successful: ${userReadResult.data}")
                        userReadResult.data.find { it.userId == userIdString }
                    }
                    is DataResourceResult.Failure -> {
                        Log.e("OAuthViewModel", "Firestore read failed: ${userReadResult.exception}")
                        null
                    }
                    else -> null
                }

                val appUser = User(
                    userId = userIdString,
                    userName = profile?.nickname ?: existingUser?.userName ?: "Unknown",
                    userProfileImage = profile?.profileImageUrl ?: existingUser?.userProfileImage ?: "",
                    userGroupDocumentId = existingUser?.userGroupDocumentId ?: "",
                    userAutoLoginToken = kakaoOAuthToken.accessToken,
                )

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

                Log.d("OAuthViewModel", "dbOperationResult: $dbOperationResult")

                when (dbOperationResult) {
                    is DataResourceResult.Success -> {
                        sessionManager.loginUser(appUser)
                        fcmTokenManager.sendTokenToServer(appUser.userId)
                        _loginStatus.value = if (existingUser != null) {
                            "User update success"
                        } else {
                            "User create success"
                        }
                    }
                    is DataResourceResult.Failure -> {
                        val failMsg = "User save failed: ${dbOperationResult.exception.message}"
                        _loginStatus.value = failMsg
                        Log.e("oauth", failMsg, dbOperationResult.exception)
                    }
                    is DataResourceResult.Loading -> Unit
                    else -> Unit
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
            if (currentUser == null) {
                Log.d("OAuthViewModel", "No user logged in, cannot check group.")
                return@launch
            }

            val latestUserResult = userRepository.getUserById(currentUser.userId)
                .filter { it !is DataResourceResult.Loading }
                .first()

            when (latestUserResult) {
                is DataResourceResult.Success -> {
                    val latestUser = latestUserResult.data
                    if (latestUser != null) {
                        sessionManager.loginUser(latestUser)
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

    fun handleDevLogin(
        userId: String,
        userName: String,
        userProfileImage: String,
        userGroupDocumentId: String,
        userAutoLoginToken: String,
    ) {
        viewModelScope.launch {
            _loginStatus.value = "Processing dev login..."

            try {
                val devUser = User(
                    userId = userId,
                    userName = userName,
                    userProfileImage = userProfileImage,
                    userGroupDocumentId = userGroupDocumentId,
                    userAutoLoginToken = userAutoLoginToken,
                )

                val existingUser = userRepository.getUserByIdSync(userId)
                Log.d("OAuthViewModel", "Existing user check result: $existingUser")

                val dbOperationResultFlow =
                    if (existingUser is DataResourceResult.Success && existingUser.data != null) {
                        Log.d("OAuthViewModel", "Updating existing dev user: $devUser")
                        userRepository.update(devUser)
                    } else {
                        Log.d("OAuthViewModel", "Creating dev user: $devUser")
                        userRepository.create(devUser)
                    }

                val dbOperationResult = dbOperationResultFlow
                    .filter { it !is DataResourceResult.Loading }
                    .first()

                Log.d("OAuthViewModel", "Dev login DB result: $dbOperationResult")

                when (dbOperationResult) {
                    is DataResourceResult.Success -> {
                        if (userGroupDocumentId.isNotEmpty()) {
                            val groupResult = groupRepository.addUserToGroup(userGroupDocumentId, userId)
                                .filter { it !is DataResourceResult.Loading }
                                .first()

                            when (groupResult) {
                                is DataResourceResult.Success -> {
                                    Log.d("OAuthViewModel", "Added user to group")
                                }
                                is DataResourceResult.Failure -> {
                                    Log.e("OAuthViewModel", "Failed to add user to group", groupResult.exception)
                                    _loginStatus.value = "Group add failed: ${groupResult.exception.message}"
                                    return@launch
                                }
                                else -> {
                                    Log.w("OAuthViewModel", "Group add result: $groupResult")
                                }
                            }
                        }

                        sessionManager.loginUser(devUser)
                        fcmTokenManager.sendTokenToServer(devUser.userId)
                        _loginStatus.value = "Dev login success"
                    }
                    is DataResourceResult.Failure -> {
                        val failMsg = "Dev login failed: ${dbOperationResult.exception.message}"
                        _loginStatus.value = failMsg
                        Log.e("OAuthViewModel", failMsg, dbOperationResult.exception)
                    }
                    else -> {
                        _loginStatus.value = "Processing dev login..."
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Error during dev login: ${e.message}"
                _loginStatus.value = errorMsg
                Log.e("OAuthViewModel", errorMsg, e)
            }
        }
    }
}
