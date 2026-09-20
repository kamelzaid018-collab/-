package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.media.SoundHelper
import com.example.receiver.AlarmScheduler
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PrayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PrayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure default synthetic audio files exist and initialize alarms
        lifecycleScope.launch(Dispatchers.IO) {
            SoundHelper.ensureDefaultSounds(applicationContext)
            AlarmScheduler.scheduleAll(applicationContext)
            com.example.notification.PrayerNotificationHelper.updatePersistentNotification(applicationContext)
            com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(applicationContext)
        }

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            com.example.notification.PrayerNotificationHelper.updatePersistentNotification(applicationContext)
            com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(applicationContext)
        }
    }
}
