package com.ahn.ggriggri.screen.ui.group.ui.joingroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import com.ahn.domain.common.DataResourceResult
import com.ahn.ggriggri.screen.ui.group.viewmodel.GroupViewModel
import kotlinx.coroutines.launch

@Composable
fun JoinGroupScreen(
    groupViewModel: GroupViewModel = hiltViewModel(),
    userId: String,
    onNavigateToHome: () -> Unit = {}
) {
    var groupCode by remember { mutableStateOf("") }
    var groupPw by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    val createResult by groupViewModel.createResult.collectAsStateWithLifecycle()

    // 성공 시 화면 이동
    LaunchedEffect(createResult) {
        if (createResult is DataResourceResult.Success) {
            onNavigateToHome()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // 그룹 참여 실패(그룹 없음, 비밀번호 불일치 등) 메시지를 스낵바로 보여준다. MakeGroupScreen과 같은 방식.
    // snackBarMessage는 replay 없는 SharedFlow(일회성 이벤트)라 별도 코루틴에서 띄워 VM의 emit이 막히지 않게 하고,
    // 새 메시지가 오면 이전 스낵바를 닫는다.
    LaunchedEffect(groupViewModel) {
        groupViewModel.snackBarMessage.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // 바깥 Scaffold(EntryPointScreen)가 이미 시스템 바 여백을 적용하므로 중복 적용하지 않는다(기존 Surface 레이아웃 유지).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ){
            // 그룹 코드
            CommonOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = groupCode,
                onValueChange = { groupCode = it },
                label = { Text(stringResource(R.string.group_code)) },
                singleLine = true
            )

            // 그룹코드 비밀번호
            CommonOutlinedTextField(
                value = groupPw,
                onValueChange = {groupPw = it},
                label = {Text(stringResource(R.string.group_pw))},
                singleLine = true,
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(imageVector = image, contentDescription = "")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            CommonButton(
                onClick = {
                    groupViewModel.joinGroup(userId, groupCode, groupPw)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 32.dp)
            ){
                Text(stringResource(R.string.group_btn_joinGroup))
            }
        }
    }
}