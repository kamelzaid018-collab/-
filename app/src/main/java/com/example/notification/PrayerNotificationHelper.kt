package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
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

            val alHudaTitle = if (lang.isRtl) {
                "مسجد ${loc.nameAr} • القادمة: $nextPrayerName (${String.format("%02d:%02d", hoursLeft, minsLeft)})"
            } else {
                "${loc.nameAr} • Next: $nextPrayerName (in ${String.format("%02dh %02dm", hoursLeft, minsLeft)})"
            }
            val alHudaBigText = buildString {
                append("{ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا }\n")
                if (hijriDateStr.isNotBlank()) {
                    append("📅 $hijriDateStr\n")
                }
                append("• الفجر: ${formatTime(result.fajrMillis)} | الشروق: ${formatTime(result.sunriseMillis)} | الظهر: ${formatTime(result.dhuhrMillis)}\n")
                append("• العصر: ${formatTime(result.asrMillis)} | المغرب: ${formatTime(result.maghribMillis)} | العشاء: ${formatTime(result.ishaMillis)}\n")
                append("🤲 الصلاة على النبي ﷺ: مستمرة")
            }

            val finalTitle = if (notifStyle == "ALHUDA") alHudaTitle else title
            val finalContent = if (notifStyle == "ALHUDA") alHudaBigText else summaryLine

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

            fun formatTimeDigits(ms: Long): String {
                val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId)).apply { timeInMillis = ms }
                val pattern = if (timeFormat == TimeFormatPreference.FORMAT_24H) "HH:mm" else "hh:mm"
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone(tzId)
                return sdf.format(cal.time)
            }

            fun formatAmPm(ms: Long): String {
                if (timeFormat == TimeFormatPreference.FORMAT_24H) return ""
                val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId)).apply { timeInMillis = ms }
                val sdf = SimpleDateFormat("a", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone(tzId)
                return sdf.format(cal.time)
            }

            val dayOfWeekStr = SimpleDateFormat("EEEE", if (lang.isRtl) Locale("ar") else Locale.ENGLISH).format(nowCal.time)
            val gregDateStr = SimpleDateFormat("d MMMM yyyy", if (lang.isRtl) Locale("ar") else Locale.ENGLISH).format(nowCal.time)
            val locationAndGregText = if (lang.isRtl) {
                "${loc.nameAr} • اليوم: $dayOfWeekStr $gregDateStr"
            } else {
                "${loc.nameAr} • $dayOfWeekStr $gregDateStr"
            }
            val hijriFullText = "${result.hijriDay} ${result.hijriMonthNameAr} ${result.hijriYear}هـ"

            // Custom Notification RemoteViews (Faithful to the ornate Islamic board)
            val expandedViews = RemoteViews(context.packageName, R.layout.notification_prayer_board_expanded)
            val collapsedViews = RemoteViews(context.packageName, R.layout.notification_prayer_board_collapsed)

            // Header info
            expandedViews.setTextViewText(R.id.notif_location_and_gregorian, locationAndGregText)
            expandedViews.setTextViewText(R.id.notif_hijri_date, hijriFullText)

            // 5 Prayers Times & AM/PM
            expandedViews.setTextViewText(R.id.notif_time_fajr, formatTimeDigits(result.fajrMillis))
            expandedViews.setTextViewText(R.id.notif_ampm_fajr, formatAmPm(result.fajrMillis))

            expandedViews.setTextViewText(R.id.notif_time_dhuhr, formatTimeDigits(result.dhuhrMillis))
            expandedViews.setTextViewText(R.id.notif_ampm_dhuhr, formatAmPm(result.dhuhrMillis))

            expandedViews.setTextViewText(R.id.notif_time_asr, formatTimeDigits(result.asrMillis))
            expandedViews.setTextViewText(R.id.notif_ampm_asr, formatAmPm(result.asrMillis))

            expandedViews.setTextViewText(R.id.notif_time_maghrib, formatTimeDigits(result.maghribMillis))
            expandedViews.setTextViewText(R.id.notif_ampm_maghrib, formatAmPm(result.maghribMillis))

            expandedViews.setTextViewText(R.id.notif_time_isha, formatTimeDigits(result.ishaMillis))
            expandedViews.setTextViewText(R.id.notif_ampm_isha, formatAmPm(result.ishaMillis))

            // Highlight next prayer card with radiant golden glow
            expandedViews.setInt(R.id.card_fajr, "setBackgroundResource", R.drawable.bg_card_fajr)
            expandedViews.setInt(R.id.card_dhuhr, "setBackgroundResource", R.drawable.bg_card_dhuhr)
            expandedViews.setInt(R.id.card_asr, "setBackgroundResource", R.drawable.bg_card_asr)
            expandedViews.setInt(R.id.card_maghrib, "setBackgroundResource", R.drawable.bg_card_maghrib)
            expandedViews.setInt(R.id.card_isha, "setBackgroundResource", R.drawable.bg_card_isha)

            when (nextPrayerType) {
                PrayerType.FAJR -> expandedViews.setInt(R.id.card_fajr, "setBackgroundResource", R.drawable.bg_card_active)
                PrayerType.DHUHR -> expandedViews.setInt(R.id.card_dhuhr, "setBackgroundResource", R.drawable.bg_card_active)
                PrayerType.ASR -> expandedViews.setInt(R.id.card_asr, "setBackgroundResource", R.drawable.bg_card_active)
                PrayerType.MAGHRIB -> expandedViews.setInt(R.id.card_maghrib, "setBackgroundResource", R.drawable.bg_card_active)
                PrayerType.ISHA -> expandedViews.setInt(R.id.card_isha, "setBackgroundResource", R.drawable.bg_card_active)
                else -> {}
            }

            // Right Info panel
            val curPattern = if (timeFormat == TimeFormatPreference.FORMAT_24H) "HH:mm" else "hh:mm a"
            val curSdf = SimpleDateFormat(curPattern, Locale.US)
            curSdf.timeZone = TimeZone.getTimeZone(tzId)
            expandedViews.setTextViewText(R.id.notif_current_time, curSdf.format(nowCal.time))

            val nextTitleStr = if (lang.isRtl) "الصلاة القادمة: $nextPrayerName" else "Next: $nextPrayerName"
            val countdownStr = if (lang.isRtl) "متبقي: $hoursLeft س و $minsLeft د" else "in ${hoursLeft}h ${minsLeft}m"
            expandedViews.setTextViewText(R.id.notif_next_prayer_name, nextTitleStr)
            expandedViews.setTextViewText(R.id.notif_next_prayer_countdown, countdownStr)
            expandedViews.setTextViewText(R.id.notif_salawat_countdown, String.format(Locale.US, "%02d : %02d", hoursLeft, minsLeft))

            // Collapsed view setup
            collapsedViews.setTextViewText(R.id.notif_collapsed_next_prayer, "🕌 $nextPrayerName: ${formatTimeDigits(nextPrayerTimeMs)}")
            collapsedViews.setTextViewText(R.id.notif_collapsed_countdown, String.format(Locale.US, "متبقي %02d:%02d", hoursLeft, minsLeft))
            collapsedViews.setTextViewText(R.id.notif_col_time_fajr, formatTimeDigits(result.fajrMillis))
            collapsedViews.setTextViewText(R.id.notif_col_time_dhuhr, formatTimeDigits(result.dhuhrMillis))
            collapsedViews.setTextViewText(R.id.notif_col_time_asr, formatTimeDigits(result.asrMillis))
            collapsedViews.setTextViewText(R.id.notif_col_time_maghrib, formatTimeDigits(result.maghribMillis))
            collapsedViews.setTextViewText(R.id.notif_col_time_isha, formatTimeDigits(result.ishaMillis))
            collapsedViews.setTextViewText(R.id.notif_col_location, loc.nameAr)
            collapsedViews.setTextViewText(R.id.notif_col_hijri, "${result.hijriDay} ${result.hijriMonthNameAr}")

            expandedViews.setOnClickPendingIntent(R.id.notif_board_root, pendingOpen)
            collapsedViews.setOnClickPendingIntent(R.id.notif_collapsed_root, pendingOpen)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_PERSISTENT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingOpen)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if (notifStyle == "PLAIN") {
                builder.setContentTitle(finalTitle)
                builder.setContentText(finalContent)
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(finalContent))
            } else {
                builder.setCustomContentView(collapsedViews)
                builder.setCustomBigContentView(expandedViews)
                builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
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
