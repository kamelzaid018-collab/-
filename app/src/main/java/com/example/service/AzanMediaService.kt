package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.model.Language
import com.example.data.model.PrayerType
import com.example.data.repository.SettingsRepository
import com.example.media.SoundHelper
import com.example.ui.azan.AzanScreenActivity
import com.example.ui.language.AppStrings

class AzanMediaService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var timeAlertPlayer: MediaPlayer? = null
    private var azanPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AzanApp:AzanMediaServiceLock").apply {
            acquire(10 * 60 * 1000L) // max 10 minutes safety timeout
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopPlaybackAndService()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_TYPE) ?: PrayerType.DHUHR.name
        val prayerType = try { PrayerType.valueOf(prayerName) } catch (e: Exception) { PrayerType.DHUHR }

        val settingsRepo = SettingsRepository(applicationContext)
        val lang = settingsRepo.language.value
        val config = settingsRepo.getAzanConfig(prayerType)

        // Start Foreground Notification
        createNotificationChannel()
        val notification = buildForegroundNotification(prayerType, lang)
        startForeground(NOTIFICATION_ID, notification)

        // Mandatory sequence: Time Alert first, then Azan immediately without delay
        val fallbackAlertPath = SoundHelper.getDefaultTimeAlertPath(applicationContext)
        val fallbackAzanPath = SoundHelper.getDefaultAzanPath(applicationContext)

        Log.d(TAG, "Starting playback sequence: Time Alert -> Azan for $prayerType")

        timeAlertPlayer = SoundHelper.playSound(
            context = applicationContext,
            uriString = config.timeAlertSoundUri,
            fallbackPath = fallbackAlertPath,
            isAlarm = true,
            onCompletion = {
                Log.d(TAG, "Time alert finished, launching Azan immediately.")
                playAzan(config.azanSoundUri, fallbackAzanPath, prayerType, config.duaaVideoUri)
            }
        )

        // If time alert failed to play or was null, play Azan directly
        if (timeAlertPlayer == null) {
            playAzan(config.azanSoundUri, fallbackAzanPath, prayerType, config.duaaVideoUri)
        }

        return START_NOT_STICKY
    }

    private fun playAzan(azanUri: String?, fallbackPath: String, prayerType: PrayerType, duaaVideoUri: String?) {
        azanPlayer = SoundHelper.playSound(
            context = applicationContext,
            uriString = azanUri,
            fallbackPath = fallbackPath,
            isAlarm = true,
            onCompletion = {
                Log.d(TAG, "Azan finished. Checking for Duaa video: $duaaVideoUri")
                if (!duaaVideoUri.isNullOrBlank()) {
                    // Launch AzanScreenActivity to play the Duaa video
                    val videoIntent = Intent(applicationContext, AzanScreenActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(AzanScreenActivity.EXTRA_PRAYER_TYPE, prayerType.name)
                        putExtra(AzanScreenActivity.EXTRA_PLAY_VIDEO_NOW, true)
                        putExtra(AzanScreenActivity.EXTRA_VIDEO_URI, duaaVideoUri)
                    }
                    startActivity(videoIntent)
                }
                stopPlaybackAndService()
            }
        )
    }

    private fun stopPlaybackAndService() {
        SoundHelper.stopCurrentSound()
        try {
            timeAlertPlayer?.release()
            azanPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing players: ${e.message}")
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaybackAndService()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wakeLock: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Azan Call to Prayer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Foreground playback for Azan"
                setSound(null, null)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(prayerType: PrayerType, lang: Language): Notification {
        val prayerName = AppStrings.getPrayerName(prayerType, lang)
        val title = when (lang) {
            Language.ARABIC -> "حان الآن موعد أذان $prayerName"
            Language.FRENCH -> "C'est l'heure de l'Adhan de $prayerName"
            Language.ENGLISH -> "It is now time for $prayerName Azan"
        }

        val openIntent = Intent(this, AzanScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AzanScreenActivity.EXTRA_PRAYER_TYPE, prayerType.name)
        }
        val openPending = PendingIntent.getActivity(
            this,
            201,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AzanMediaService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this,
            202,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissText = when (lang) {
            Language.ARABIC -> "إيقاف"
            Language.FRENCH -> "Arrêter"
            Language.ENGLISH -> "Stop"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("🕌 $prayerName")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openPending)
            .setFullScreenIntent(openPending, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, dismissText, stopPending)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "AzanMediaService"
        const val CHANNEL_ID = "azan_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "action_stop_azan"
        const val EXTRA_PRAYER_TYPE = "extra_prayer_type"

        fun start(context: Context, prayerType: PrayerType) {
            val intent = Intent(context, AzanMediaService::class.java).apply {
                putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AzanMediaService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
