package com.ahn.ggriggri.screen.group.joingroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import com.ahn.ggriggri.screen.ui.group.viewmodel.GroupViewModel
import theme.GgriggriTheme

@Composable
fun JoinGroupScreen(
    userId: String,
) {

    val groupViewModel: GroupViewModel = hiltViewModel()
    var groupCode by remember { mutableStateOf("") }
    var groupPw by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }


    GgriggriTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){

            Column(
                modifier = Modifier
                    .fillMaxSize()
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
}