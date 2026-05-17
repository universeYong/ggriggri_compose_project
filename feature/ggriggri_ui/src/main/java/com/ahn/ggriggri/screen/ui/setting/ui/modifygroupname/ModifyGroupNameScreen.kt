package com.ahn.ggriggri.screen.setting.modifygroupname

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.common_ui.components.CommonButton
import com.ahn.ggriggri.screen.ui.setting.viewmodel.SettingGroupViewModel
import theme.GgriggriTheme

@Composable
fun ModifyGroupNameScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettingGroup: () -> Unit = {},
    viewModel: SettingGroupViewModel = hiltViewModel()
) {

    var groupName by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val currentGroup by viewModel.currentGroup.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // 현재 그룹명을 초기값으로 설정
    LaunchedEffect(currentGroup) {
        currentGroup?.let { group ->
            groupName = group.groupName
        }
    }

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

    // 버튼 활성화 여부를 groupName의 내용과 로딩 상태에 따라 결정
    val isButtonEnabled = groupName.isNotEmpty() && !isLoading
    GgriggriTheme {
        androidx.compose.material3.Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp), // layout_marginHorizontal="20dp"
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp)) // layout_marginVertical="10dp"

                // 그룹명 입력 필드
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("그룹명") },
                    singleLine = true,
                    enabled = !isLoading,
                    // app:endIconMode="clear_text" 구현
                    trailingIcon = {
                        if (groupName.isNotEmpty()) {
                            IconButton(onClick = { groupName = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "텍스트 지우기"
                                )
                            }
                        }
                    },
                    // app:helperText=" " 와 동일한 효과 (공간 확보)
                    supportingText = { Text(" ") },
                    // TextInputLayout의 색상 속성들 대체
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.DarkGray, // app:boxStrokeColor
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.Black, // app:cursorColor
                        focusedLabelColor = Color.DarkGray // app:hintTextColor
                    )
                )

                // 버튼을 하단에 고정하기 위한 Spacer
                Spacer(modifier = Modifier.weight(1f))

                // 변경하기 버튼
                CommonButton(
                    onClick = { 
                        if (groupName.isNotEmpty()) {
                            viewModel.updateGroupName(groupName)
                            onNavigateToSettingGroup()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp), // layout_margin="20dp"의 하단 부분
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
