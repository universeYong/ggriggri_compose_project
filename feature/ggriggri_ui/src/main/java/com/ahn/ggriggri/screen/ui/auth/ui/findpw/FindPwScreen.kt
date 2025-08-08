package com.ahn.ggriggri.screen.auth.findpw

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonButton
import com.ahn.common_ui.components.CommonOutlinedTextField
import theme.GgrigggriTheme
import theme.NanumSquareBold

@Composable
fun FindPwScreen(
    onNavigateToResetPw: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    GgrigggriTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            CommonOutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text(stringResource(R.string.find_password_id_hint)) },
                singleLine = true,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommonOutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.find_password_phone_number_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                CommonButton(
                    onClick = {}
                ) {
                    Text(
                        text = stringResource(R.string.find_password_request_verification_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = NanumSquareBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommonOutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.find_password_verification_code_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CommonButton(onClick = {}, enabled = false) {
                    Text(
                        text = stringResource(R.string.find_password_confirm_verification_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = NanumSquareBold
                    )
                }
            }
        }
    }
}