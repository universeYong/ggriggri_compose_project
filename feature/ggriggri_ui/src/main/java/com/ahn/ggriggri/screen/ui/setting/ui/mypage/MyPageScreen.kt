package com.ahn.ggriggri.screen.setting.mypage

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ahn.common_ui.R
import com.ahn.ggriggri.screen.ui.setting.viewmodel.MyPageViewModel

@Composable
fun MyPageScreen(
    onNavigateToGroupSetting: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    myPageviewModel: MyPageViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by myPageviewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        myPageviewModel.refreshGroupInfo()
        onDispose { }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.user != null) {
        val user = uiState.user!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 20.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = if (user.userProfileImage.isNotEmpty()) {
                        user.userProfileImage
                    } else {
                        R.drawable.outline_account_circle_24
                    },
                    contentDescription = "profile",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user.userName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Text(
                text = "그룹명",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Text(
                text = uiState.group?.groupName ?: "그룹 없음",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingMenuItem(
                title = "알림 on/off",
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            putExtra("app_package", context.packageName)
                            putExtra("app_uid", context.applicationInfo.uid)
                        }
                    }
                    context.startActivity(intent)
                },
            )
            HorizontalDivider()

            SettingMenuItem(
                title = "그룹 설정",
                onClick = { onNavigateToGroupSetting() },
            )
            HorizontalDivider()

            SettingMenuItem(
                title = "로그아웃",
                onClick = { myPageviewModel.logout(onNavigateToLogin) },
            )
        }
    }
}

@Composable
fun SettingMenuItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}
