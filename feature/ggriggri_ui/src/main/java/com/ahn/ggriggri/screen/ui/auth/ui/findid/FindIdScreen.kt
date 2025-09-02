package com.ahn.ggriggri.screen.auth.findid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R
import com.ahn.common_ui.components.CommonOutlinedTextField
import theme.GgriggriTheme

@Composable
fun FindIdScreen(
    onNavigateBack: () -> Unit,
) {
    GgriggriTheme {
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
                label = { Text(stringResource(R.string.find_id_name_hint)) },
                singleLine = true
            )

            CommonOutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text(stringResource(R.string.find_id_phone_number_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

    }
}