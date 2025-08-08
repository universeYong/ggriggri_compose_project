package com.ahn.ggriggri.screen.setting.modifygrouppw

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.PasswordTextField
import theme.GgrigggriTheme

@Composable
fun ModifyGroupPwScreen() {

    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    // 유효성 검사 로직 (예: 새 비밀번호와 확인 비밀번호가 일치하는지)
    val isNewPasswordValid = newPassword.isNotEmpty() && newPassword == confirmNewPassword
    val isButtonEnabled = isNewPasswordValid
    GgrigggriTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()

                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 새 비밀번호 입력 필드
            PasswordTextField(
                value = newPassword,
                onValueChange = {
                    // android:digits="@string/digit_value" 와 같은 입력 필터링은
                    // onValueChange 콜백에서 처리합니다.
                    // 예: val filtered = it.filter { char -> char.isLetterOrDigit() }
                    // newPassword = filtered
                    newPassword = it
                },
                label = "새 비밀번호 입력",
                supportingText = " " // app:helperText=" " 와 동일한 효과 (공간 확보)
            )

            // 새 비밀번호 재입력 필드
            PasswordTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = "새 비밀번호 재입력",
                isError = confirmNewPassword.isNotEmpty() && !isNewPasswordValid,
                supportingText = if (confirmNewPassword.isNotEmpty() && !isNewPasswordValid) "비밀번호가 일치하지 않습니다." else " "
            )

            // 버튼을 하단에 고정하기 위한 Spacer
            Spacer(modifier = Modifier.weight(1f))

            // 변경하기 버튼
            CommonButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                enabled = isButtonEnabled,
            ) {
                Text(
                    text = "변경하기",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}