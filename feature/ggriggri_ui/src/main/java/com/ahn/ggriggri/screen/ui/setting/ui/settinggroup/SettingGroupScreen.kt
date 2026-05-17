package com.ahn.ggriggri.screen.setting.settinggroup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.ggriggri.screen.ui.setting.viewmodel.SettingGroupViewModel
import theme.GgriggriTheme
import theme.NanumSquareRegular

@Composable
fun SettingGroupScreen(
    onNavigateToModifyGroupPw: () -> Unit = {},
    onNavigateToModifyGroupName: () -> Unit = {},
    onNavigateToLeaveGroup: () -> Unit = {},
    viewModel: SettingGroupViewModel = hiltViewModel()
) {
    val currentGroup by viewModel.currentGroup.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    // 디버깅을 위한 로그
    LaunchedEffect(currentGroup, isLoading, errorMessage) {
        android.util.Log.d("SettingGroupScreen", "상태 업데이트 - currentGroup: $currentGroup, isLoading: $isLoading, errorMessage: $errorMessage")
    }

    // 에러 메시지 처리
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // 추후 스낵바로 표시
            viewModel.clearErrorMessage()
        }
    }

    // 성공 메시지 처리
    successMessage?.let { message ->
        LaunchedEffect(message) {
            // 추후 스낵바로 표시
            viewModel.clearSuccessMessage()
        }
    }

    GgriggriTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 그룹 코드 섹션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "그룹 코드 : ",
                    fontSize = 20.sp,
                    fontFamily = NanumSquareRegular
                )
                Text(
                    text = currentGroup?.groupCode ?: "로딩 중...",
                    fontSize = 20.sp,
                    fontFamily = NanumSquareRegular
                )
            }

            // 그룹명 표시
            currentGroup?.groupName?.let { groupName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "그룹명 : ",
                        fontSize = 20.sp,
                        fontFamily = NanumSquareRegular
                    )
                    Text(
                        text = groupName,
                        fontSize = 20.sp,
                        fontFamily = NanumSquareRegular
                    )
                }
            }

            // 구분선 (View)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .background(Color(0xFFF0F0F0))
            )

            // 로딩 중일 때
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // 설정 메뉴 항목들
                SettingMenuItem(
                    text = "그룹 비밀번호 변경",
                    onClick = { onNavigateToModifyGroupPw() }
                )
                SettingMenuItem(
                    text = "그룹명 변경",
                    onClick = { onNavigateToModifyGroupName() }
                )
                SettingMenuItem(
                    text = "그룹 나가기",
                    onClick = { 
                        viewModel.leaveGroup(onNavigateToLeaveGroup)
                    }
                )
            }
        }
    }
}


