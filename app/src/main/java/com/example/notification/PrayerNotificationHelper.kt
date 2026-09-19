package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.calculator.HijriCalendarHelper
import com.example.calculator.PrayerTimeCalculator
import com.example.data.model.PrayerType
import com.example.data.model.TimeFormatPreference
import com.example.data.repository.SettingsRepository
import com.example.ui.language.AppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object PrayerNotificationHelper {

    private const val TAG = "PrayerNotificationHelper"
    const val NOTIFICATION_ID_PERSISTENT = 77001
    const val CHANNEL_ID_PERSISTENT = "prayer_persistent_status_bar"

    fun updatePersistentNotification(context: Context) {
        val settingsRepo = SettingsRepository(context.applicationContext)
        val isEnabled = settingsRepo.isNotificationBarEnabled.value

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!isEnabled) {
            notificationManager.cancel(NOTIFICATION_ID_PERSISTENT)
            return
        }

        try {
            val lang = settingsRepo.language.value
            val loc = settingsRepo.selectedLocation.value
            val tzId = settingsRepo.getEffectiveTimeZoneId()
            val calcMethod = settingsRepo.calculationMethod.value
            val juristic = settingsRepo.juristicMethod.value
            val dst = settingsRepo.dstMode.value
            val offsets = settingsRepo.getOffsetsMap()
            val hijriOffset = settingsRepo.hijriOffsetDays.value
            val timeFormat = settingsRepo.timeFormat.value
            val notifStyle = settingsRepo.notificationBarStyle.value
            val showCountdown = settingsRepo.isNotificationCountdownEnabled.value
            val showHijri = settingsRepo.isNotificationHijriEnabled.value

            val nowCal = Calendar.getInstance(TimeZone.getTimeZone(tzId))
            val nowMs = System.currentTimeMillis()

            val result = PrayerTimeCalculator.calculatePrayerTimes(
                calendar = nowCal,
                latitude = loc.latitude,
                longitude = loc.longitude,
                timeZoneId = tzId,
                method = calcMethod,
                juristicMethod = juristic,
                dstMode = dst,
                manualOffsetMinutes = offsets,
                hijriOffsetDays = hijriOffset
            )

            // Determine next prayer
            val prayers = listOf(
                PrayerType.FAJR to result.fajrMillis,
                PrayerType.SUNRISE to result.sunriseMillis,
                PrayerType.DHUHR to result.dhuhrMillis,
                PrayerType.ASR to result.asrMillis,
                PrayerType.MAGHRIB to result.maghribMillis,
                PrayerType.ISHA to result.ishaMillis
            )

            var nextPrayer = prayers.firstOrNull { it.second > nowMs }
            var nextPrayerType = nextPrayer?.first ?: PrayerType.FAJR
            var nextPrayerTimeMs = nextPrayer?.second

            if (nextPrayerTimeMs == null) {
                // Next day's Fajr
                val tomorrowCal = Calendar.getInstance(TimeZone.getTimeZone(tzId)).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                val tomorrowResult = PrayerTimeCalculator.calculatePrayerTimes(
                    calendar = tomorrowCal,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    timeZoneId = tzId,
                    method = calcMethod,
                    juristicMethod = juristic,
                    dstMode = dst,
                    manualOffsetMinutes = offsets,
                    hijriOffsetDays = hijriOffset
                )
                nextPrayerType = PrayerType.FAJR
                nextPrayerTimeMs = tomorrowResult.fajrMillis
            }

            val nextPrayerName = AppStrings.getPrayerName(nextPrayerType, lang)
            val diffMs = (nextPrayerTimeMs - nowMs).coerceAtLeast(0L)
            val hoursLeft = diffMs / 3600000L
            val minsLeft = (diffMs % 3600000L) / 60000L

            fun formatTime(ms: Long): String {
                val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId)).apply { timeInMillis = ms }
                val pattern = if (timeFormat == TimeFormatPreference.FORMAT_24H) "HH:mm" else "hh:mm a"
                val sdf = SimpleDateFormat(pattern, if (lang.isRtl) Locale("ar") else Locale.ENGLISH)
                sdf.timeZone = TimeZone.getTimeZone(tzId)
                return sdf.format(cal.time)
            }

            val hijriDateStr = if (showHijri) {
                "${result.hijriDay} ${result.hijriMonthNameAr} ${result.hijriYear}هـ"
            } else ""

            val title = if (showCountdown) {
                if (lang.isRtl) {
                    "🕌 الصلاة القادمة: $nextPrayerName (متبقي ${String.format("%02d:%02d", hoursLeft, minsLeft)})"
                } else {
                    "🕌 Next: $nextPrayerName (in ${String.format("%02dh %02dm", hoursLeft, minsLeft)})"
                }
            } else {
                "🕌 $nextPrayerName • ${formatTime(nextPrayerTimeMs)}"
            }

            val summaryLine = if (lang.isRtl) {
                "الفجر ${formatTime(result.fajrMillis)} • الظهر ${formatTime(result.dhuhrMillis)} • العصر ${formatTime(result.asrMillis)} • المغرب ${formatTime(result.maghribMillis)} • العشاء ${formatTime(result.ishaMillis)}"
            } else {
                "Fajr ${formatTime(result.fajrMillis)} | Dhuhr ${formatTime(result.dhuhrMillis)} | Asr ${formatTime(result.asrMillis)} | Maghrib ${formatTime(result.maghribMillis)} | Isha ${formatTime(result.ishaMillis)}"
            }

            val bigTextContent = buildString {
                if (hijriDateStr.isNotBlank()) {
                    append("📅 $hijriDateStr | 📍 ${loc.nameAr}\n")
                }
                append("• ${AppStrings.getPrayerName(PrayerType.FAJR, lang)}: ${formatTime(result.fajrMillis)}\n")
                append("• ${AppStrings.getPrayerName(PrayerType.SUNRISE, lang)}: ${formatTime(result.sunriseMillis)}\n")
                append("• ${AppStrings.getPrayerName(PrayerType.DHUHR, lang)}: ${formatTime(result.dhuhrMillis)}\n")
                append("• ${AppStrings.getPrayerName(PrayerType.ASR, lang)}: ${formatTime(result.asrMillis)}\n")
                append("• ${AppStrings.getPrayerName(PrayerType.MAGHRIB, lang)}: ${formatTime(result.maghribMillis)}\n")
                append("• ${AppStrings.getPrayerName(PrayerType.ISHA, lang)}: ${formatTime(result.ishaMillis)}")
            }

            // Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID_PERSISTENT,
                    "شريط مواقيت الصلاة الدائم (Status Bar)",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "عرض الصلاة القادمة والمواقيت في شريط الإشعارات العلوي باستمرار"
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingOpen = PendingIntent.getActivity(
                context,
                7701,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_PERSISTENT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(summaryLine)
                .setContentIntent(pendingOpen)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if (notifStyle == "DETAILED") {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigTextContent))
            } else {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(summaryLine))
            }

            notificationManager.notify(NOTIFICATION_ID_PERSISTENT, builder.build())
            Log.d(TAG, "Persistent status bar notification updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update persistent notification: ${e.message}")
        }
    }

    fun cancelPersistentNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_PERSISTENT)
    }
}
