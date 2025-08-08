package com.ahn.ggriggri.screen.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahn.common_ui.R
import com.ahn.common_ui.components.AppLogo
import com.ahn.ggriggri.screen.auth.login.components.LoginText
import com.ahn.ggriggri.screen.auth.login.components.SocialLoginDivider
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import theme.GgrigggriTheme
import theme.NanumSquareBold

@Composable
fun LoginScreen(
    authViewModel: OAuthViewModel = viewModel(),
    onNavigationToGroup: () -> Unit,
    onNavigationToHome: () -> Unit
) {
    val context = LocalContext.current

    val loginStatus by authViewModel.loginStatus.collectAsStateWithLifecycle()
    val userId by authViewModel.currentUserId.collectAsStateWithLifecycle()

    if (loginStatus == "로그인 및 회원 정보 저장 성공" && userId != null){
        LaunchedEffect(userId) {
            authViewModel.checkUserGroupAndNavigate(
                userId = userId!!,
                onNavigationToGroup = onNavigationToGroup,
                onNavigationToHome = onNavigationToHome
            )
        }
    }

//    if (loginStatus == "로그인 성공") {
////        onSuccessLogin()
//    }

    GgrigggriTheme {
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
                    modifier = Modifier.clickable {
                        authViewModel.handleKakaoLogin(context)

                    }.fillMaxWidth().height(60.dp)
                )

            }
        }
    }
}