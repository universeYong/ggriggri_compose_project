package com.ahn.common_ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R

@Composable
fun AppLogo(
     modifier: Modifier = Modifier,
     contentDescription: String? = stringResource(R.string.app_logo)
) {
    Image(
        painter = painterResource(id = R.drawable.main_logo_background_round),
        contentDescription = contentDescription,
        modifier = modifier.size(250.dp)
    )
}