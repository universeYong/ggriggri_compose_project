package com.ahn.ggriggri.screen.ui.setting.ui.settinggroup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.ahn.common_ui.theme.GgriggriTheme
import com.ahn.common_ui.theme.NanumSquareRegular

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

    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 메시지: 스낵바로 보여준 뒤 clear 한다.
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    // 성공 메시지: 스낵바로 보여준 뒤 clear 한다.
    // (그룹 나가기는 성공 즉시 화면을 떠나므로 이 화면에서는 메시지가 보이지 않을 수 있다.)
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    GgriggriTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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

                // 그룹 비밀번호 섹션
                // 구성원이 공유하는 참여 관문이므로 그룹 코드와 함께 노출한다.
                // 이 값이 보이기 때문에 변경 화면에서 현재 비밀번호를 다시 묻지 않는다.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "그룹 비밀번호 : ",
                        fontSize = 20.sp,
                        fontFamily = NanumSquareRegular
                    )
                    Text(
                        text = currentGroup?.groupPw ?: "로딩 중...",
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
}


