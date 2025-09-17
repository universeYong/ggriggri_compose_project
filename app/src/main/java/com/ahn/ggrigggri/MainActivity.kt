package com.ahn.ggrigggri

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ahn.ggrigggri.ui.theme.GgrigggriTheme
import com.ahn.ggriggri.screen.auth.login.LoginScreen
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var oauthViewModel: OAuthViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        oauthViewModel = hiltViewModel()

        // Activity Result API 설정
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // 권한이 허용됨
                Log.d("MainActivity", "Notification permission granted")
                oauthViewModel.handlePermissionResult(true)
            } else {
                // 권한이 거부됨
                Log.d("MainActivity", "Notification permission denied")
                oauthViewModel.handlePermissionResult(false)
            }
        }

        setContent {
            GgrigggriTheme {
                EntryPointScreen()
            }
        }
    }

    // 권한 요청 함수
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}