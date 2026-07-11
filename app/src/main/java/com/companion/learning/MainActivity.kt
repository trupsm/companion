package com.companion.learning

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.ui.navigation.AppNavigation
import com.companion.learning.ui.notification.NotificationHelper
import com.companion.learning.ui.notification.NotificationReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var secureStorage: SecureStorage

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
            scheduleStudyReminder()
        } else {
            Log.d("MainActivity", "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Create channels
        NotificationHelper.createNotificationChannels(this)

        // 2. Request runtime permission on Android 13+
        checkNotificationPermission()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                scheduleStudyReminder()
            } else {
                requestPermissionLauncher.launch(permission)
            }
        } else {
            // No permission needed on Android 12 and below
            scheduleStudyReminder()
        }
    }

    private fun scheduleStudyReminder() {
        val hour = secureStorage.getStudyReminderHour()
        val minute = secureStorage.getStudyReminderMinute()
        NotificationReceiver.scheduleDailyAlarm(this, hour, minute, secureStorage)
    }
}
