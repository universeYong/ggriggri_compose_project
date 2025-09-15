package com.ahn.ggriggri.screen.setting.modifygrouppw

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.PasswordTextField
import com.ahn.ggriggri.screen.ui.setting.viewmodel.SettingGroupViewModel
import theme.GgriggriTheme

@Composable
fun ModifyGroupPwScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettingGroup: () -> Unit = {},
    viewModel: SettingGroupViewModel = hiltViewModel()
) {

    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 유효성 검사 로직 (예: 새 비밀번호와 확인 비밀번호가 일치하는지)
    val isNewPasswordValid = newPassword.isNotEmpty() && newPassword == confirmNewPassword
    val isButtonEnabled = isNewPasswordValid && !isLoading

    // 에러 메시지 표시
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    // 성공 메시지 표시
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }
    GgriggriTheme {
        androidx.compose.material3.Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 새 비밀번호 입력 필드
                PasswordTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "새 비밀번호 입력",
                    supportingText = " ",
                    enabled = !isLoading
                )

                // 새 비밀번호 재입력 필드
                PasswordTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = "새 비밀번호 재입력",
                    isError = confirmNewPassword.isNotEmpty() && !isNewPasswordValid,
                    supportingText = if (confirmNewPassword.isNotEmpty() && !isNewPasswordValid) "비밀번호가 일치하지 않습니다." else " ",
                    enabled = !isLoading
                )

                // 버튼을 하단에 고정하기 위한 Spacer
                Spacer(modifier = Modifier.weight(1f))

                // 변경하기 버튼
                CommonButton(
                    onClick = { 
                        if (isNewPasswordValid) {
                            viewModel.updateGroupPassword(newPassword)
                            onNavigateToSettingGroup()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    enabled = isButtonEnabled,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "변경하기",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}