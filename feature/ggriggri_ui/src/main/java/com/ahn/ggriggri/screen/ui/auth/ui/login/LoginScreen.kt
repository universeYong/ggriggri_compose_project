package com.ahn.ggriggri.screen.ui.auth.ui.login

import android.Manifest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.common_ui.R
import com.ahn.ggriggri.BuildConfig
import com.ahn.common_ui.components.AppLogo
import com.ahn.ggriggri.screen.ui.auth.ui.login.components.LoginText
import com.ahn.ggriggri.screen.ui.auth.ui.login.components.SocialLoginDivider
import com.ahn.ggriggri.screen.ui.auth.viewmodel.AppStartViewModel
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import com.ahn.common_ui.theme.NanumSquareBold

@Composable
fun LoginScreen(
    onNavigationToGroup: () -> Unit,
    onNavigationToHome: () -> Unit,
    onNavigationToDevLogin: () -> Unit = {},
    authViewModel: OAuthViewModel = hiltViewModel(),
) {

    val context = LocalContext.current // 이것도 여기서 쓰면 안됨

    val loginStatus by authViewModel.loginStatus.collectAsStateWithLifecycle()
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val autoLoginFinished by authViewModel.autoLoginFinished.collectAsStateWithLifecycle()

    // 앱을 새로 켰을 때만 자동 로그인을 시도한다(Activity 범위 플래그). 그룹 화면 등에서 돌아온 경우는 시도하지 않는다.
    val appStartViewModel: AppStartViewModel = hiltViewModel(
        checkNotNull(context as? ComponentActivity) { "LoginScreen은 ComponentActivity 안에서 쓴다." }
    )
    val runAutoLogin = rememberSaveable { appStartViewModel.consumeAutoLogin() }
    val checkingSession = runAutoLogin && !autoLoginFinished

    LaunchedEffect(Unit) {
        if (runAutoLogin) {
            authViewModel.tryAutoLogin(
                onNavigationToGroup = onNavigationToGroup,
                onNavigationToHome = onNavigationToHome
            )
        }
    }

    // 다른 화면으로 이동해 이 화면이 사라질 때 로딩 표시를 되돌려, 뒤로가기로 돌아왔을 때 스피너가 남지 않게 한다.
    DisposableEffect(Unit) {
        onDispose { authViewModel.resetLoading() }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> authViewModel.handlePermissionResult(isGranted) }


    // 뒤로가기 버튼을 눌렀을 때 앱 종료
    BackHandler {
        // 앱 종료 처리
        (context as? android.app.Activity)?.finishAffinity()
    }

    // 이미 로그인된 사용자가 뒤로가기로 온 경우 자동 네비게이션 방지
    // 새로운 로그인 시에만 네비게이션 체크
    LaunchedEffect(loginStatus) {
        if (loginStatus.contains("성공") && currentUserId != null) {
            Log.d("LoginScreen", "Login successful, checking group and navigating.")
            authViewModel.checkUserGroupAndNavigate(
                onNavigationToGroup = onNavigationToGroup,
                onNavigationToHome = onNavigationToHome
            )
        }
        // 권한 요청은 Activity가 필요하므로 ViewModel이 아니라 화면에서 런처로 수행한다.
        if (authViewModel.shouldRequestNotificationPermission()) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 실패 메시지(진행 중/성공 메시지는 제외)
    val errorMessage = loginStatus.takeIf {
        it.isNotBlank() && !it.contains("성공") && !it.contains("처리 중")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(160.dp))
            AppLogo()
            Spacer(Modifier.height(20.dp))
            LoginText(
                text = stringResource(R.string.login_header_title),
                style = MaterialTheme.typography.displayMedium,
                fontFamily = NanumSquareBold
            )
            Spacer(Modifier.height(80.dp))
            SocialLoginDivider()
            Spacer(Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.kakao_login_large_wide),
                contentDescription = "Kakao Login",
                modifier = Modifier
                    .clickable {
                        authViewModel.handleKakaoLogin(context)

                    }
                    .fillMaxWidth()
                    .height(60.dp)
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            // 개발용 로그인 버튼 (디버그 빌드에서만 표시)
            if (BuildConfig.DEBUG) {
                Button(
                    onClick = { onNavigationToDevLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("개발용 로그인")
                }
            }

        }

        // 로그인 진행 중 / 저장된 로그인 확인 중 표시. 터치를 막아 중복 로그인을 방지한다.
        // 앱 시작 시 확인 중에는 배경을 불투명하게 해서 로그인 버튼이 잠깐 보이지 않게 한다.
        if (isLoading || checkingSession) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = if (checkingSession) 1f else 0.85f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (checkingSession) "로그인 정보를 확인하고 있어요..." else "로그인 중이에요. 잠시만 기다려 주세요...",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        }
    }
}