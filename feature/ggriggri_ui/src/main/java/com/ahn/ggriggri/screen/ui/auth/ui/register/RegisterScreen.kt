package com.ahn.ggriggri.screen.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonDialog
import com.ahn.common_ui.components.CommonOutlinedTextField
import theme.GgrigggriTheme


@Composable
fun RegisterScreen(
) {
    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var authCode by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isPasswordMismatch =
        password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword

    var showIdCheckDialog by remember { mutableStateOf(false) }
    if (showIdCheckDialog) {
        CommonDialog(
            icon = Icons.Default.Error,
            title = "사용 가능한 아이디 입니다",
            confirmButtonText = "사용하기",
            dismissButtonText = "취소",
            onDismissRequest = { showIdCheckDialog = false },
            onConfirmation = {
                // '사용하기' 로직 실행...
                showIdCheckDialog = false // 로직 실행 후 다이얼로그 닫기
            },
            onDismissClick = { showIdCheckDialog = false }
        )
    }

    GgrigggriTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }

            // 이름
            item {
                CommonOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.hint_name)) },
                    singleLine = true
                )
            }

            // 아이디
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommonOutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text(stringResource(R.string.hint_id)) },
                        singleLine = true,
//                            supportingText = { Text(stringResource(R.string.id_check_success_message)) },
                    )
                    CommonButton(
                        onClick = { showIdCheckDialog = true }

                    ) { Text(stringResource(R.string.button_check_id_duplication)) }
                }
            }

            // 비밀번호
            item {
                CommonOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.hint_password)) },
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
            }

            // 비밀번호 재입력
            item {
                CommonOutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.hint_confirm_password)) },
                    singleLine = true,
                    isError = isPasswordMismatch,
                    supportingText = {
                        if (isPasswordMismatch)
                            Text(stringResource(R.string.password_mismatch_error))
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
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
            }

            // 휴대폰 번호, 인증 버튼
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
                {
                    CommonOutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.hint_phone_number)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    CommonButton(
                        onClick = {}
                    ) { Text(stringResource(R.string.button_request_verification)) }
                }
            }
//
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
                {
                    CommonOutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = authCode,
                        onValueChange = { authCode = it },
                        label = { Text(stringResource(R.string.hint_verification_code)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    CommonButton(onClick = {}, enabled = false) {
                        Text(stringResource(R.string.button_confirm_verification))
                    }
                }
            }
        }
    }

}