package com.ahn.ggriggri.screen.group.makegroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import com.ahn.ggriggri.screen.ui.group.viewmodel.GroupViewModel
import theme.GgrigggriTheme

@Composable
fun MakeGroupScreen(
    groupViewModel: GroupViewModel = viewModel(),
    userId: String
) {

    var groupName by remember { mutableStateOf("") }
    var groupCode by remember { mutableStateOf("") }
    var groupPw by remember { mutableStateOf("") }
    var groupConfirmPw by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isPasswordMismatch =
        groupPw.isNotEmpty() && groupConfirmPw.isNotEmpty() && groupPw != groupConfirmPw


    val createResult by groupViewModel.createResult.collectAsState()

    GgrigggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // 그룹명
                CommonOutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text(stringResource(R.string.group_name)) },
                    singleLine = true
                )

                // 그룹코드
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    CommonOutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = groupCode,
                        onValueChange = { groupCode = it },
                        label = { Text(stringResource(R.string.group_code)) },
                        singleLine = true
                    )
                    CommonButton(
                        onClick = {}
                    ) { Text(stringResource(R.string.button_check_id_duplication)) }
                }

                // 그룹코드 비밀번호
                CommonOutlinedTextField(
                    value = groupPw,
                    onValueChange = { groupPw = it },
                    label = { Text(stringResource(R.string.group_pw)) },
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

                // 그룹코드 비밀번호 확인
                CommonOutlinedTextField(
                    value = groupConfirmPw,
                    onValueChange = { groupConfirmPw = it },
                    label = { Text(stringResource(R.string.group_confirm_pw)) },
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

                Spacer(modifier = Modifier.weight(1f))

                CommonButton(
                    onClick = {
                        if (groupPw == groupConfirmPw && groupName.isNotBlank() &&
                            groupCode.isNotBlank()
                        ) {
                            groupViewModel.createGroupAndUpdateUser(
                                userId = userId,
                                groupName,
                                groupCode,
                                groupPw
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 32.dp)

                ) {
                    Text(stringResource(R.string.group_btn_makeGroup))
                }
            }
        }
    }
}