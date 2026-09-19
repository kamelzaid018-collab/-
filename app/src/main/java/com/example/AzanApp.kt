package com.example

import android.app.Application
import android.content.Intent
import android.util.Log
import com.example.media.SoundHelper
import com.example.receiver.AlarmScheduler
import com.example.service.PrayerKeeperService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AzanApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Global crash-protection handler to prevent application from ever crashing or stopping
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AzanApp", "Caught unhandled exception in thread ${thread.name}: ${throwable.message}", throwable)
            try {
                // Ensure background service is started
                PrayerKeeperService.start(this@AzanApp)
            } catch (e: Exception) {
                Log.e("AzanApp", "Failed during crash recovery: ${e.message}")
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                SoundHelper.ensureDefaultSounds(this@AzanApp)
                AlarmScheduler.scheduleAll(this@AzanApp)
                PrayerKeeperService.start(this@AzanApp)
            } catch (e: Exception) {
                Log.e("AzanApp", "Error during app init: ${e.message}")
            }
        }
    }
}
