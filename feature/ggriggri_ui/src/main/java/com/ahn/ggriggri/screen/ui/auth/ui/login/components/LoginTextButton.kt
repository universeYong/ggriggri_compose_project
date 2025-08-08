package com.ahn.ggriggri.screen.auth.login.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahn.common_ui.R
import theme.NanumSquareExtraBold
import theme.NanumSquareRegular

@Composable
fun AuthButtons(
    onFindIdClick: () -> Unit,
    onFindPwClick : () -> Unit,
    onRegisterClick : () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoginText(
                text = stringResource(R.string.login_signup_prompt),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = NanumSquareRegular
            )
            Spacer(Modifier.width(4.dp))
            LoginText(
                text = stringResource(R.string.login_signup_link),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = NanumSquareExtraBold,
                modifier = Modifier.clickable{
                    Log.d("register","회원가입 눌림")
                    onRegisterClick() }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoginText(
                text = stringResource(R.string.login_find_id_link),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = NanumSquareExtraBold,
                modifier = Modifier.clickable{onFindIdClick()}

            )
            Spacer(Modifier.width(4.dp))
            LoginText(
                text = stringResource(R.string.login_find_password_link),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = NanumSquareExtraBold,
                modifier = Modifier.clickable{onFindPwClick()}
            )
        }
    }
}