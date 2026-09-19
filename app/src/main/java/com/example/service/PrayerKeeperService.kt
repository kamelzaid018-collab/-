package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * High-reliability persistent service that ensures prayer alarms and reminders
 * never stop working even under aggressive Android OS memory management.
 */
class PrayerKeeperService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PrayerKeeperService created - keeping app active and reliable")

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AzanApp:KeeperLock")

        // Periodically verify alarms every 30 minutes in background
        serviceScope.launch {
            while (isActive) {
                try {
                    AlarmScheduler.scheduleAll(applicationContext)
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing alarms in keeper service: ${e.message}")
                }
                delay(30 * 60 * 1000L) // 30 minutes
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "PrayerKeeperService onStartCommand - START_STICKY")
        
        // Always refresh alarms on service start or auto-restart
        serviceScope.launch {
            AlarmScheduler.scheduleAll(applicationContext)
        }

        // START_STICKY ensures Android OS revives this service automatically if killed
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "Task removed by user - restarting keeper and rescheduling alarms")
        
        // Immediately restart service and reschedule alarms if swiped from recent apps
        val restartIntent = Intent(applicationContext, PrayerKeeperService::class.java)
        val restartPendingIntent = PendingIntent.getService(
            applicationContext,
            999,
            restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        try {
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000L,
                restartPendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling restart alarm: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        // If killed, attempt to self-restart
        val restartIntent = Intent(applicationContext, PrayerKeeperService::class.java)
        startService(restartIntent)
    }

    companion object {
        private const val TAG = "PrayerKeeperService"

        fun start(context: Context) {
            try {
                val intent = Intent(context, PrayerKeeperService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start PrayerKeeperService: ${e.message}")
            }
        }
    }
}
