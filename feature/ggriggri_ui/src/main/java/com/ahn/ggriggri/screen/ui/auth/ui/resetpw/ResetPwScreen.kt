package com.ahn.ggriggri.screen.auth.resetpw

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import theme.GgrigggriTheme

@Composable
fun ResetPwScreen() {

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isPasswordMismatch =
        password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword

    GgrigggriTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            CommonOutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text(stringResource(R.string.reset_password_new_password_hint)) },
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

            CommonOutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text(stringResource(R.string.reset_password_confirm_password_hint)) },
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

        CommonButton(
            onClick = {}
        ) { Text(text = stringResource(R.string.reset_password_submit_button)) }
    }
}
