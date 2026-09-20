package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calculator.PrayerTimeCalculator
import com.example.data.model.PrayerType
import com.example.data.model.TimeFormatPreference
import com.example.data.repository.SettingsRepository
import com.example.ui.language.AppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PrayerAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            updateAllWidgets(context)
        }
    }

    companion object {
        private const val TAG = "PrayerAppWidget"
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, PrayerAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
                val intent = Intent(context, PrayerAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets: ${e.message}")
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val settingsRepo = SettingsRepository(context.applicationContext)
                val lang = settingsRepo.language.value
                val loc = settingsRepo.selectedLocation.value
                val tzId = settingsRepo.getEffectiveTimeZoneId()
                val calcMethod = settingsRepo.calculationMethod.value
                val juristic = settingsRepo.juristicMethod.value
                val dst = settingsRepo.dstMode.value
                val offsets = settingsRepo.getOffsetsMap()
                val hijriOffset = settingsRepo.hijriOffsetDays.value
                val timeFormat = settingsRepo.timeFormat.value
                val isHijriEnabled = settingsRepo.isWidgetHijriEnabled.value
                val isLocEnabled = settingsRepo.isWidgetLocationEnabled.value

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

                val nextPrayer = prayers.firstOrNull { it.second > nowMs }
                var nextPrayerType = nextPrayer?.first ?: PrayerType.FAJR
                var nextPrayerTimeMs = nextPrayer?.second

                if (nextPrayerTimeMs == null) {
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
                val secsLeft = (diffMs % 60000L) / 1000L

                fun formatTime(ms: Long): String {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId)).apply { timeInMillis = ms }
                    val pattern = if (timeFormat == TimeFormatPreference.FORMAT_24H) "HH:mm" else "hh:mm"
                    val sdf = SimpleDateFormat(pattern, Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone(tzId)
                    return sdf.format(cal.time)
                }

                val widgetTheme = settingsRepo.widgetThemeStyle.value
                val layoutId = when (widgetTheme) {
                    "ALHUDA" -> R.layout.widget_prayer_times_alhuda
                    "EMERALD" -> R.layout.widget_prayer_times
                    else -> R.layout.widget_prayer_times_board
                }
                val views = RemoteViews(context.packageName, layoutId)

                // Location & Hijri Date
                views.setTextViewText(
                    R.id.widget_location_name,
                    if (widgetTheme == "ALHUDA") {
                        if (isLocEnabled) "مسجد • ${loc.nameAr}" else "مواقيت الصلاة"
                    } else if (widgetTheme == "BOARD") {
                        val dayOfWeekStr = SimpleDateFormat("EEEE", Locale("ar")).format(nowCal.time)
                        val gregStr = SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(nowCal.time)
                        if (isLocEnabled) "${loc.nameAr} • اليوم: $dayOfWeekStr $gregStr" else "مواقيت الصلاة • $dayOfWeekStr $gregStr"
                    } else {
                        if (isLocEnabled) "مواقيت الصلاة • ${loc.nameAr}" else "مواقيت الصلاة"
                    }
                )
                if (isHijriEnabled) {
                    val hijriText = if (widgetTheme == "ALHUDA" || widgetTheme == "BOARD") {
                        val dayOfWeekStr = SimpleDateFormat("EEEE", Locale("ar")).format(nowCal.time)
                        "$dayOfWeekStr ${result.hijriDay} ${result.hijriMonthNameAr} ${result.hijriYear}هـ"
                    } else {
                        "${result.hijriDay} ${result.hijriMonthNameAr} ${result.hijriYear}هـ"
                    }
                    views.setTextViewText(R.id.widget_hijri_date, hijriText)
                } else {
                    views.setTextViewText(R.id.widget_hijri_date, result.gregorianDateText)
                }

                // Next prayer title & countdown
                if (widgetTheme == "ALHUDA") {
                    views.setTextViewText(R.id.widget_next_prayer_countdown, String.format(Locale.US, "%02d:%02d", hoursLeft, minsLeft))
                    val timeSdf = SimpleDateFormat(if (timeFormat == TimeFormatPreference.FORMAT_24H) "HH:mm" else "hh:mm", Locale.US)
                    timeSdf.timeZone = TimeZone.getTimeZone(tzId)
                    val amPmSdf = SimpleDateFormat("a", Locale("ar"))
                    amPmSdf.timeZone = TimeZone.getTimeZone(tzId)
                    views.setTextViewText(R.id.widget_digital_time, timeSdf.format(nowCal.time))
                    views.setTextViewText(R.id.widget_digital_ampm, amPmSdf.format(nowCal.time))
                    views.setTextViewText(R.id.widget_salawat_text, "الصلاة على النبي ﷺ: يتبقى ${String.format(Locale.US, "%02d:%02d", hoursLeft, minsLeft)}")
                } else if (widgetTheme == "BOARD") {
                    val timeSdf = SimpleDateFormat(if (timeFormat == TimeFormatPreference.FORMAT_24H) "HH:mm" else "hh:mm a", Locale.US)
                    timeSdf.timeZone = TimeZone.getTimeZone(tzId)
                    views.setTextViewText(R.id.widget_digital_time, timeSdf.format(nowCal.time))
                    views.setTextViewText(R.id.widget_next_prayer_title, "الصلاة القادمة: $nextPrayerName")
                    views.setTextViewText(R.id.widget_next_prayer_countdown, "متبقي: ${String.format(Locale.US, "%02d:%02d", hoursLeft, minsLeft)}")
                    views.setTextViewText(R.id.widget_salawat_text, String.format(Locale.US, "%02d : %02d", hoursLeft, minsLeft))

                    // AM/PM labels for board
                    fun formatAmPmStr(ms: Long): String {
                        if (timeFormat == TimeFormatPreference.FORMAT_24H) return ""
                        val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId)).apply { timeInMillis = ms }
                        val sdf = SimpleDateFormat("a", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone(tzId)
                        return sdf.format(cal.time)
                    }
                    views.setTextViewText(R.id.widget_ampm_fajr, formatAmPmStr(result.fajrMillis))
                    views.setTextViewText(R.id.widget_ampm_dhuhr, formatAmPmStr(result.dhuhrMillis))
                    views.setTextViewText(R.id.widget_ampm_asr, formatAmPmStr(result.asrMillis))
                    views.setTextViewText(R.id.widget_ampm_maghrib, formatAmPmStr(result.maghribMillis))
                    views.setTextViewText(R.id.widget_ampm_isha, formatAmPmStr(result.ishaMillis))

                    // Highlight next prayer card
                    views.setInt(R.id.widget_card_fajr, "setBackgroundResource", R.drawable.bg_card_fajr)
                    views.setInt(R.id.widget_card_dhuhr, "setBackgroundResource", R.drawable.bg_card_dhuhr)
                    views.setInt(R.id.widget_card_asr, "setBackgroundResource", R.drawable.bg_card_asr)
                    views.setInt(R.id.widget_card_maghrib, "setBackgroundResource", R.drawable.bg_card_maghrib)
                    views.setInt(R.id.widget_card_isha, "setBackgroundResource", R.drawable.bg_card_isha)

                    when (nextPrayerType) {
                        PrayerType.FAJR -> views.setInt(R.id.widget_card_fajr, "setBackgroundResource", R.drawable.bg_card_active)
                        PrayerType.DHUHR -> views.setInt(R.id.widget_card_dhuhr, "setBackgroundResource", R.drawable.bg_card_active)
                        PrayerType.ASR -> views.setInt(R.id.widget_card_asr, "setBackgroundResource", R.drawable.bg_card_active)
                        PrayerType.MAGHRIB -> views.setInt(R.id.widget_card_maghrib, "setBackgroundResource", R.drawable.bg_card_active)
                        PrayerType.ISHA -> views.setInt(R.id.widget_card_isha, "setBackgroundResource", R.drawable.bg_card_active)
                        else -> {}
                    }
                } else {
                    views.setTextViewText(R.id.widget_next_prayer_title, "🕌 القادمة: $nextPrayerName")
                    views.setTextViewText(
                        R.id.widget_next_prayer_countdown,
                        String.format(Locale.US, "%02d:%02d:%02d", hoursLeft, minsLeft, secsLeft)
                    )
                }

                // 6 Prayer times
                val isFriday = nowCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
                views.setTextViewText(R.id.widget_title_dhuhr, if (isFriday) (if (lang.isRtl) "الجمعة" else "Jumu'ah") else (if (lang.isRtl) "الظهر" else "Dhuhr"))
                views.setTextViewText(R.id.widget_time_fajr, formatTime(result.fajrMillis))
                views.setTextViewText(R.id.widget_time_sunrise, formatTime(result.sunriseMillis))
                views.setTextViewText(R.id.widget_time_dhuhr, formatTime(result.dhuhrMillis))
                views.setTextViewText(R.id.widget_time_asr, formatTime(result.asrMillis))
                views.setTextViewText(R.id.widget_time_maghrib, formatTime(result.maghribMillis))
                views.setTextViewText(R.id.widget_time_isha, formatTime(result.ishaMillis))

                // On click open App
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    widgetIdToRequestCode(appWidgetId),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Error building widget views: ${e.message}")
            }
        }

        private fun widgetIdToRequestCode(widgetId: Int): Int = widgetId + 8000
    }
}
