package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.calculator.PrayerTimeCalculator
import com.example.data.database.AppDatabase
import com.example.data.model.PrayerType
import com.example.data.repository.SettingsRepository
import java.util.Calendar

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    const val ACTION_AZAN = "com.example.ACTION_AZAN"
    const val ACTION_ALERT = "com.example.ACTION_ALERT"
    const val ACTION_SALAWAT = "com.example.ACTION_SALAWAT"
    const val ACTION_IFTAR_CANNON = "com.example.ACTION_IFTAR_CANNON"
    const val ACTION_MUSAHARATI = "com.example.ACTION_MUSAHARATI"
    const val ACTION_DAILY_RESCHEDULE = "com.example.ACTION_DAILY_RESCHEDULE"

    const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
    const val EXTRA_ALERT_ID = "extra_alert_id"
    const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
    const val EXTRA_SOUND_URI = "extra_sound_uri"

    /**
     * Reschedules all alarms for today and upcoming prayer events.
     */
    suspend fun scheduleAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val settingsRepo = SettingsRepository(context)
        val alertDao = AppDatabase.getDatabase(context).alertDao()

        val location = settingsRepo.selectedLocation.value
        val timeZoneId = settingsRepo.getEffectiveTimeZoneId()
        val method = settingsRepo.calculationMethod.value
        val juristic = settingsRepo.juristicMethod.value
        val dst = settingsRepo.dstMode.value
        val offsets = settingsRepo.getOffsetsMap()
        val hijriOffset = settingsRepo.hijriOffsetDays.value

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Calculate for today and tomorrow to guarantee upcoming events are covered
        for (dayOffset in 0..1) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, dayOffset)
            }
            val result = PrayerTimeCalculator.calculatePrayerTimes(
                calendar = dayCal,
                latitude = location.latitude,
                longitude = location.longitude,
                timeZoneId = timeZoneId,
                method = method,
                juristicMethod = juristic,
                dstMode = dst,
                manualOffsetMinutes = offsets,
                hijriOffsetDays = hijriOffset
            )

            // Check if today is Friday for Dhuhr vs Jumuah
            val isFriday = dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY

            val prayers = listOf(
                PrayerType.FAJR to result.fajrMillis,
                (if (isFriday) PrayerType.JUMUAH else PrayerType.DHUHR) to result.dhuhrMillis,
                PrayerType.ASR to result.asrMillis,
                PrayerType.MAGHRIB to result.maghribMillis,
                PrayerType.ISHA to result.ishaMillis
            )

            // 1. Schedule Azans
            prayers.forEach { (prayer, timeMs) ->
                if (timeMs > now) {
                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        action = ACTION_AZAN
                        putExtra(EXTRA_PRAYER_TYPE, prayer.name)
                    }
                    val requestCode = 1000 + prayer.id + (dayOffset * 10)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setAlarmClock(context, alarmManager, timeMs, pendingIntent)
                }
            }

            // 2. Schedule Alerts
            val activeAlerts = alertDao.getActiveAlerts()
            activeAlerts.forEach { alert ->
                val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
                if (alert.repeatsOnDay(dayOfWeek)) {
                    val targetPrayers = if (alert.targetPrayer == PrayerType.ALL) {
                        listOf(PrayerType.FAJR, (if (isFriday) PrayerType.JUMUAH else PrayerType.DHUHR), PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)
                    } else if (alert.targetPrayer == PrayerType.JUMUAH) {
                        if (isFriday) listOf(PrayerType.JUMUAH) else emptyList()
                    } else if (alert.targetPrayer == PrayerType.DHUHR) {
                        if (!isFriday) listOf(PrayerType.DHUHR) else emptyList()
                    } else {
                        listOf(alert.targetPrayer)
                    }

                    targetPrayers.forEach { targetPrayer ->
                        val prayerMs = result.getTimeMillis(targetPrayer)
                        val alertMs = prayerMs - (alert.minutesBefore * 60_000L)
                        if (alertMs > now) {
                            val alertIntent = Intent(context, AlarmReceiver::class.java).apply {
                                action = ACTION_ALERT
                                putExtra(EXTRA_ALERT_ID, alert.id)
                                putExtra(EXTRA_PRAYER_TYPE, targetPrayer.name)
                                putExtra(EXTRA_MINUTES_BEFORE, alert.minutesBefore)
                                putExtra(EXTRA_SOUND_URI, alert.soundUri)
                            }
                            val reqCode = (alert.id * 100 + targetPrayer.id + (dayOffset * 10)).toInt()
                            val pending = PendingIntent.getBroadcast(
                                context,
                                reqCode,
                                alertIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            setAlarmClock(context, alarmManager, alertMs, pending)
                        }
                    }
                }
            }

            // 3. Ramadan: Iftar Cannon (3 mins before Maghrib)
            val ramadan = settingsRepo.ramadanConfig.value
            if (ramadan.isRamadanMode && ramadan.isIftarCannonEnabled) {
                val cannonMs = result.maghribMillis - (3 * 60_000L)
                if (cannonMs > now) {
                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        action = ACTION_IFTAR_CANNON
                    }
                    val pending = PendingIntent.getBroadcast(
                        context,
                        7000 + dayOffset,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setAlarmClock(context, alarmManager, cannonMs, pending)
                }
            }

            // 4. Ramadan: Musaharati
            if (ramadan.isRamadanMode && ramadan.isMusaharatiEnabled) {
                val musaharatiMs = if (ramadan.musaharatiIsFixedTime) {
                    Calendar.getInstance().apply {
                        timeInMillis = dayCal.timeInMillis
                        set(Calendar.HOUR_OF_DAY, ramadan.musaharatiFixedHour)
                        set(Calendar.MINUTE, ramadan.musaharatiFixedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                } else {
                    result.fajrMillis - (ramadan.musaharatiMinutesBeforeFajr * 60_000L)
                }

                if (musaharatiMs > now) {
                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        action = ACTION_MUSAHARATI
                    }
                    val pending = PendingIntent.getBroadcast(
                        context,
                        8000 + dayOffset,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setAlarmClock(context, alarmManager, musaharatiMs, pending)
                }
            }
        }

        // 5. Salawat Reminders
        val salawat = settingsRepo.salawatConfig.value
        if (salawat.isEnabled) {
            val intervalMs = salawat.intervalMinutes * 60_000L
            val nextTime = if (salawat.nextReminderTimeMillis > now) {
                salawat.nextReminderTimeMillis
            } else {
                now + intervalMs
            }
            settingsRepo.saveSalawatConfig(salawat.copy(nextReminderTimeMillis = nextTime))

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_SALAWAT
            }
            val pending = PendingIntent.getBroadcast(
                context,
                9000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setAlarmClock(context, alarmManager, nextTime, pending)
        }

        // 6. Schedule Midnight Daily Rescheduler
        val midnightCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
        }
        val dailyIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DAILY_RESCHEDULE
        }
        val dailyPending = PendingIntent.getBroadcast(
            context,
            9999,
            dailyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarmClock(context, alarmManager, midnightCal.timeInMillis, dailyPending)

        // 7. Update Persistent Status Bar Notification & Home Screen Widgets
        try {
            com.example.notification.PrayerNotificationHelper.updatePersistentNotification(context)
            com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification bar or widgets: ${e.message}")
        }

        Log.d(TAG, "All alarms rescheduled successfully with AlarmClock precision.")
    }

    private fun setAlarmClock(context: Context, alarmManager: AlarmManager, timeMs: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = Intent(context, com.example.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    pendingIntent.hashCode(),
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMs, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d(TAG, "Scheduled AlarmClock (real phone alarm) at $timeMs")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException setting alarm clock: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
                }
            } catch (e2: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm clock: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } catch (e3: Exception) {
                Log.e(TAG, "Fallback alarm failed: ${e3.message}")
            }
        }
    }
}
