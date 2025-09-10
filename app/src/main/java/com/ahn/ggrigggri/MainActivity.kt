package com.ahn.ggrigggri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ahn.ggrigggri.ui.theme.GgrigggriTheme
import com.ahn.ggriggri.screen.auth.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GgrigggriTheme {
                EntryPointScreen()
            }
        }
    }
}