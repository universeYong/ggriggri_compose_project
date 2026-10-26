package com.ahn.ggrigggri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ahn.common_ui.theme.GgriggriTheme
import dagger.hilt.android.AndroidEntryPoint


// 알림 권한 요청은 LoginScreen이 ActivityResult 런처로 직접 수행한다.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GgriggriTheme {
                EntryPointScreen()
            }
        }
    }
}
