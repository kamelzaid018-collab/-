package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerType
import com.example.data.model.SalawatPlayMode
import com.example.data.repository.SettingsRepository
import com.example.media.SoundHelper
import com.example.service.AzanMediaService
import com.example.service.PrayerKeeperService
import com.example.ui.alerts.PrePrayerAlertActivity
import com.example.ui.azan.AzanScreenActivity
import com.example.ui.language.AppStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "onReceive action: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AzanApp:AlarmReceiverLock").apply {
            acquire(60_000L) // Hold CPU awake for up to 60 seconds
        }

        try {
            // Keep background service alive
            PrayerKeeperService.start(context)

            val settingsRepo = SettingsRepository(context.applicationContext)
            val lang = settingsRepo.language.value

            when (action) {
                AlarmScheduler.ACTION_AZAN -> {
                    val prayerStr = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_TYPE) ?: PrayerType.DHUHR.name
                    val prayerType = try { PrayerType.valueOf(prayerStr) } catch (e: Exception) { PrayerType.DHUHR }

                    // 1. Start Azan Foreground Media Service (Plays Time Alert, then Azan immediately)
                    AzanMediaService.start(context, prayerType)

                    // 2. Launch Azan Screen Activity on lockscreen
                    val screenIntent = Intent(context, AzanScreenActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        putExtra(AzanScreenActivity.EXTRA_PRAYER_TYPE, prayerType.name)
                    }
                    context.startActivity(screenIntent)

                    // Re-trigger schedule for upcoming events
                    CoroutineScope(Dispatchers.IO).launch {
                        AlarmScheduler.scheduleAll(context)
                    }
                }

                AlarmScheduler.ACTION_ALERT -> {
                    val prayerStr = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_TYPE) ?: PrayerType.ASR.name
                    val prayerType = try { PrayerType.valueOf(prayerStr) } catch (e: Exception) { PrayerType.ASR }
                    val minutesBefore = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTES_BEFORE, 15)
                    val soundUri = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI)

                    val message = AppStrings.formatAlertMessage(minutesBefore, prayerType, lang)
                    val prayerName = AppStrings.getPrayerName(prayerType, lang)

                    // Launch PrePrayerAlertActivity which keeps screen on while sound plays and closes upon completion
                    val alertIntent = Intent(context, PrePrayerAlertActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        putExtra(PrePrayerAlertActivity.EXTRA_PRAYER_TYPE, prayerType.name)
                        putExtra(PrePrayerAlertActivity.EXTRA_MINUTES_BEFORE, minutesBefore)
                        putExtra(PrePrayerAlertActivity.EXTRA_SOUND_URI, soundUri)
                    }
                    context.startActivity(alertIntent)

                    // Post Fullscreen Notification (shows on lockscreen immediately)
                    val fullScreenPendingIntent = PendingIntent.getActivity(
                        context,
                        (System.currentTimeMillis() % 10000).toInt(),
                        alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    showAlertNotification(context, prayerName, message, fullScreenPendingIntent)
                }

                AlarmScheduler.ACTION_SALAWAT -> {
                    val config = settingsRepo.salawatConfig.value
                    if (config.isEnabled) {
                        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                        val wakeLock = powerManager?.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK,
                            "AzanApp:SalawatWakeLock"
                        )?.apply {
                            try {
                                acquire(35_000L) // 35 seconds wake lock
                            } catch (e: Exception) {
                                Log.e(TAG, "WakeLock acquire error: ${e.message}")
                            }
                        }

                        val availableSounds = if (config.extractedSounds.isNotEmpty()) {
                            config.extractedSounds
                        } else {
                            SoundHelper.getDefaultSalawatList(context)
                        }

                        var selectedSound: String? = null
                        var nextIndex = config.lastPlayedIndex

                        when (config.playMode) {
                            SalawatPlayMode.SPECIFIC -> {
                                selectedSound = config.specificSoundUri
                            }
                            SalawatPlayMode.RANDOM -> {
                                if (availableSounds.isNotEmpty()) {
                                    selectedSound = availableSounds[Random.nextInt(availableSounds.size)]
                                }
                            }
                            SalawatPlayMode.ORDER -> {
                                if (availableSounds.isNotEmpty()) {
                                    nextIndex = (config.lastPlayedIndex + 1) % availableSounds.size
                                    selectedSound = availableSounds[nextIndex]
                                }
                            }
                        }

                        val fallbackSalawat = SoundHelper.getDefaultAlertPath(context)
                        SoundHelper.playSound(
                            context = context,
                            uriString = selectedSound,
                            fallbackPath = fallbackSalawat,
                            isAlarm = true, // Play as Alarm stream for guaranteed audio
                            onCompletion = {
                                try {
                                    if (wakeLock?.isHeld == true) {
                                        wakeLock.release()
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }
                        )

                        // Post High-Priority Salawat notification
                        showSalawatNotification(context)

                        // Reschedule next exact Salawat alarm clock
                        CoroutineScope(Dispatchers.IO).launch {
                            val intervalMs = config.intervalMinutes * 60_000L
                            val nextTime = System.currentTimeMillis() + intervalMs
                            settingsRepo.saveSalawatConfig(
                                config.copy(
                                    lastPlayedIndex = nextIndex,
                                    nextReminderTimeMillis = nextTime
                                )
                            )
                            AlarmScheduler.scheduleAll(context)
                        }
                    }
                }

                AlarmScheduler.ACTION_IFTAR_CANNON -> {
                    val ramadan = settingsRepo.ramadanConfig.value
                    val title = if (lang.isRtl) "مدفع الإفطار" else "Iftar Cannon"
                    val text = if (lang.isRtl) "اقترب موعد أذان المغرب وإفطار الصائمين" else "Time for Iftar is approaching"
                    showAlertNotification(context, title, text)

                    if (!ramadan.iftarCannonVideoUri.isNullOrBlank()) {
                        val screenIntent = Intent(context, AzanScreenActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(AzanScreenActivity.EXTRA_PLAY_VIDEO_NOW, true)
                            putExtra(AzanScreenActivity.EXTRA_VIDEO_URI, ramadan.iftarCannonVideoUri)
                            putExtra(AzanScreenActivity.EXTRA_TITLE_OVERRIDE, title)
                        }
                        context.startActivity(screenIntent)
                    }
                }

                AlarmScheduler.ACTION_MUSAHARATI -> {
                    val ramadan = settingsRepo.ramadanConfig.value
                    val title = if (lang.isRtl) "المسحراتي" else "Musaharati (Suhoor)"
                    val text = if (lang.isRtl) "اصحى يا نايم وحّد الدايم.. موعد السحور" else "Wake up for Suhoor"
                    showAlertNotification(context, title, text)

                    if (!ramadan.musaharatiVideoUri.isNullOrBlank()) {
                        val screenIntent = Intent(context, AzanScreenActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(AzanScreenActivity.EXTRA_PLAY_VIDEO_NOW, true)
                            putExtra(AzanScreenActivity.EXTRA_VIDEO_URI, ramadan.musaharatiVideoUri)
                            putExtra(AzanScreenActivity.EXTRA_TITLE_OVERRIDE, title)
                        }
                        context.startActivity(screenIntent)
                    }
                }

                AlarmScheduler.ACTION_DAILY_RESCHEDULE -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        AlarmScheduler.scheduleAll(context)
                    }
                }
            }
        } finally {
            try {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun showAlertNotification(
        context: Context,
        title: String,
        content: String,
        fullScreenPendingIntent: PendingIntent? = null
    ) {
        val channelId = "prayer_alerts_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pre-Azan notifications and alarms"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            301,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (fullScreenPendingIntent != null) {
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun showSalawatNotification(context: Context) {
        val channelId = "salawat_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Salawat Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-precision Salawat reminders"
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ﷺ الصلاة على النبي ﷺ")
            .setContentText("اللهم صلِّ وسلِّم وبارك على نبينا وحبيبنا محمد وعلى آله وصحبه أجمعين")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(4001, notification)
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
