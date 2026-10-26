package com.ahn.ggriggri.screen.setting.modifygroupname

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahn.common_ui.components.CommonButton
import com.ahn.ggriggri.screen.ui.setting.viewmodel.SettingGroupViewModel
import theme.GgriggriTheme

/**
 * 그룹명 변경 화면의 Route.
 * ViewModel 상태 수집과 사이드 이펙트(스낵바 메시지 소비)를 담당하고,
 * UI는 stateless [ModifyGroupNameScreen]에 위임한다.
 */
@Composable
fun ModifyGroupNameRoute(
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

    // 성공 메시지를 보여준 뒤에만 화면을 벗어난다.
    // 버튼에서 곧바로 이동하면 실패 메시지를 볼 수 없다. 실패하면 이 화면에 머문다.
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
            onNavigateToSettingGroup()
        }
    }

    ModifyGroupNameScreen(
        groupName = groupName,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onGroupNameChange = { groupName = it },
        onSubmitClick = {
            if (groupName.isNotEmpty()) {
                viewModel.updateGroupName(groupName)
            }
        }
    )
}

/**
 * 그룹명 변경 화면 (stateless).
 */
@Composable
fun ModifyGroupNameScreen(
    groupName: String,
    isLoading: Boolean,
    onGroupNameChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // 버튼 활성화 여부를 groupName의 내용과 로딩 상태에 따라 결정
    val isButtonEnabled = groupName.isNotEmpty() && !isLoading

    GgriggriTheme {
        Scaffold(
            modifier = modifier,
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

                GroupNameTextField(
                    value = groupName,
                    onValueChange = onGroupNameChange,
                    enabled = !isLoading
                )

                // 버튼을 하단에 고정하기 위한 Spacer
                Spacer(modifier = Modifier.weight(1f))

                // 변경하기 버튼
                CommonButton(
                    onClick = onSubmitClick,
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

/**
 * 그룹명 입력 필드 (clear 버튼 포함).
 */
@Composable
private fun GroupNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("그룹명") },
        singleLine = true,
        enabled = enabled,
        // app:endIconMode="clear_text" 구현
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
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
        // NOTE: 프로젝트 테마(GgriggriTheme)에 대응하는 색상 토큰이 없어 기존 값을 유지한다.
        //       (PasswordTextField와 동일한 값 — 추후 테마 토큰으로 통합 검토)
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.DarkGray, // app:boxStrokeColor
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black, // app:cursorColor
            focusedLabelColor = Color.DarkGray // app:hintTextColor
        )
    )
}

@Preview(name = "기본(그룹명 입력됨)", showBackground = true)
@Composable
private fun ModifyGroupNameScreenPreview() {
    ModifyGroupNameScreen(
        groupName = "끼리끼리",
        isLoading = false,
        onGroupNameChange = {},
        onSubmitClick = {}
    )
}

@Preview(name = "빈 입력(버튼 비활성)", showBackground = true)
@Composable
private fun ModifyGroupNameScreenEmptyPreview() {
    ModifyGroupNameScreen(
        groupName = "",
        isLoading = false,
        onGroupNameChange = {},
        onSubmitClick = {}
    )
}

@Preview(name = "로딩", showBackground = true)
@Composable
private fun ModifyGroupNameScreenLoadingPreview() {
    ModifyGroupNameScreen(
        groupName = "끼리끼리",
        isLoading = true,
        onGroupNameChange = {},
        onSubmitClick = {}
    )
}
