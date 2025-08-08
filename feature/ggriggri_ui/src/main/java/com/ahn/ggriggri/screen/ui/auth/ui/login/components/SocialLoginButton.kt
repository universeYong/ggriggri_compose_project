package com.ahn.ggriggri.screen.auth.login.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R

@Composable
fun SocialLoginButton(
    onKaKaoClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onNaverClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialLoginIcon(
            iconResId = R.drawable.kakao,
            onClick = onKaKaoClick
        )
        SocialLoginIcon(
            iconResId = R.drawable.google,
            onClick = onGoogleClick
        )
        SocialLoginIcon(
            iconResId = R.drawable.naver,
            onClick = onNaverClick
        )
    }
}

@Composable
private fun SocialLoginIcon(
    iconResId: Int,
    onClick: () -> Unit
){
    Image(
        painter = painterResource(id = iconResId),
        contentDescription = stringResource(R.string.social_login_icon),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable{ onClick() }
    )
}