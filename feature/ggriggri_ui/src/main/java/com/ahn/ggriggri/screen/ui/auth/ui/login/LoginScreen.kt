package com.ahn.ggriggri.screen.auth.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.common_ui.R
import com.ahn.common_ui.components.AppLogo
import com.ahn.ggriggri.screen.auth.login.components.LoginText
import com.ahn.ggriggri.screen.auth.login.components.SocialLoginDivider
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import theme.GgriggriTheme
import theme.NanumSquareBold

@Composable
fun LoginScreen(
    onNavigationToGroup: () -> Unit,
    onNavigationToHome: () -> Unit,
    onNavigationToDevLogin: () -> Unit = {},
) {

    val authViewModel: OAuthViewModel = hiltViewModel()

    val context = LocalContext.current // 이것도 여기서 쓰면 안됨

    val loginStatus by authViewModel.loginStatus.collectAsStateWithLifecycle()
    val currentUserId by authViewModel.currentUserId.collectAsStateWithLifecycle()

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
    }

    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
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

                Spacer(Modifier.height(20.dp))

                // 개발용 로그인 버튼 (개발 빌드에서만 표시)
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
    }
}