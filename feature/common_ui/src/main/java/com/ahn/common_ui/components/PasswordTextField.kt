package com.ahn.common_ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff


/**
 * 비밀번호 입력을 위한 OutlinedTextField Composable
 * @param value TextField에 표시될 값
 * @param onValueChange 값이 변경될 때 호출될 람다
 * @param label TextField의 라벨 텍스트
 * @param isError 오류 상태 여부
 * @param supportingText 하단에 표시될 보조 텍스트 또는 오류 메시지
 * @param enabled TextField 활성화 상태
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        enabled = enabled,
        supportingText = {
            if (supportingText != null) {
                Text(text = supportingText)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보이기"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        colors = OutlinedTextFieldDefaults.colors( // TextInputLayout 색상 속성 대체
            focusedBorderColor = Color.DarkGray, // app:boxStrokeColor
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black, // app:cursorColor
            focusedLabelColor = Color.DarkGray // app:hintTextColor
        )
    )
}