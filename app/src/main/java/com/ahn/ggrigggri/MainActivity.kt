package com.ahn.ggrigggri

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ahn.domain.common.SessionManager
import com.ahn.ggrigggri.ui.theme.GgrigggriTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var pendingNotificationRoute: NotificationRoute? by mutableStateOf(null)
    private var isLoggedIn: Boolean? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNotificationRoute = parseNotificationRoute(intent)
        lifecycleScope.launch {
            isLoggedIn = sessionManager.isLoggedInFlow.first()
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }

        setContent {
            GgrigggriTheme {
                EntryPointScreen(
                    isLoggedIn = isLoggedIn,
                    notificationRoute = pendingNotificationRoute,
                    onNotificationRouteConsumed = {
                        pendingNotificationRoute = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRoute = parseNotificationRoute(intent)
    }

    fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun parseNotificationRoute(intent: Intent?): NotificationRoute? {
        val type = intent?.getStringExtra("type") ?: return null
        val requestId = intent.getStringExtra("requestId")
        return NotificationRoute(type = type, requestId = requestId)
    }
}
