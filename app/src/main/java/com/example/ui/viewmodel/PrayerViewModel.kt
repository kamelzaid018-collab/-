package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.DstHelper
import com.example.calculator.PrayerTimeCalculator
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AlertRepository
import com.example.data.repository.LocationRepository
import com.example.data.repository.SettingsRepository
import com.example.receiver.AlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NextPrayerInfo(
    val prayerType: PrayerType,
    val targetTimeMillis: Long,
    val remainingMillis: Long,
    val countdownText: String // HH:mm:ss
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    val settingsRepo = SettingsRepository(application)
    private val alertDao = AppDatabase.getDatabase(application).alertDao()
    val alertRepo = AlertRepository(alertDao)

    // Current Time in Millis (ticks every second)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    // Calculated Prayer Times for Today
    private val _prayerTimes = MutableStateFlow<PrayerTimesResult?>(null)
    val prayerTimes: StateFlow<PrayerTimesResult?> = _prayerTimes.asStateFlow()

    // Next Prayer and Live Countdown
    private val _nextPrayerInfo = MutableStateFlow<NextPrayerInfo?>(null)
    val nextPrayerInfo: StateFlow<NextPrayerInfo?> = _nextPrayerInfo.asStateFlow()

    // Alerts Flow from Room
    val alertsList: StateFlow<List<AlertItem>> = alertRepo.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Salawat countdown
    private val _salawatRemainingSeconds = MutableStateFlow(0L)
    val salawatRemainingSeconds: StateFlow<Long> = _salawatRemainingSeconds.asStateFlow()

    // Friday state flow (ticks every second and evaluates day of week)
    private val _isFriday = MutableStateFlow(checkIsFriday())
    val isFriday: StateFlow<Boolean> = _isFriday.asStateFlow()

    private fun checkIsFriday(): Boolean {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(settingsRepo.getEffectiveTimeZoneId()))
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    init {
        // Recalculate on any setting changes
        viewModelScope.launch {
            combine(
                settingsRepo.selectedLocation,
                settingsRepo.isAutoTimeZone,
                settingsRepo.manualTimeZoneId,
                settingsRepo.dstMode,
                settingsRepo.calculationMethod,
                settingsRepo.juristicMethod,
                settingsRepo.hijriOffsetDays,
                settingsRepo.fajrOffset,
                settingsRepo.dhuhrOffset,
                settingsRepo.asrOffset,
                settingsRepo.maghribOffset,
                settingsRepo.ishaOffset
            ) { _ ->
                calculateTodayPrayers()
                AlarmScheduler.scheduleAll(getApplication())
            }.collect()
        }

        // Live 1-second ticker loop for electronic clock display and countdown
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                _currentTimeMillis.value = now
                _isFriday.value = checkIsFriday()
                updateNextPrayerCountdown(now)
                updateSalawatCountdown(now)
                delay(1000L)
            }
        }
    }

    fun calculateTodayPrayers() {
        val cal = Calendar.getInstance()
        val loc = settingsRepo.selectedLocation.value
        val tzId = settingsRepo.getEffectiveTimeZoneId()
        val method = settingsRepo.calculationMethod.value
        val juristic = settingsRepo.juristicMethod.value
        val dst = settingsRepo.dstMode.value
        val offsets = settingsRepo.getOffsetsMap()
        val hijriOffset = settingsRepo.hijriOffsetDays.value

        val res = PrayerTimeCalculator.calculatePrayerTimes(
            calendar = cal,
            latitude = loc.latitude,
            longitude = loc.longitude,
            timeZoneId = tzId,
            method = method,
            juristicMethod = juristic,
            dstMode = dst,
            manualOffsetMinutes = offsets,
            hijriOffsetDays = hijriOffset
        )
        _prayerTimes.value = res
        updateNextPrayerCountdown(System.currentTimeMillis())
    }

    private fun updateNextPrayerCountdown(now: Long) {
        val prayers = _prayerTimes.value ?: return
        val cal = Calendar.getInstance()
        val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        val dhuhrType = if (isFriday) PrayerType.JUMUAH else PrayerType.DHUHR

        val todayList = listOf(
            PrayerType.FAJR to prayers.fajrMillis,
            dhuhrType to prayers.dhuhrMillis,
            PrayerType.ASR to prayers.asrMillis,
            PrayerType.MAGHRIB to prayers.maghribMillis,
            PrayerType.ISHA to prayers.ishaMillis
        )

        // Find first prayer whose time is in the future
        var next = todayList.firstOrNull { it.second > now }

        if (next == null) {
            // All prayers for today have passed -> next prayer is Tomorrow's Fajr
            val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
            val loc = settingsRepo.selectedLocation.value
            val tzId = settingsRepo.getEffectiveTimeZoneId()
            val tomorrowResult = PrayerTimeCalculator.calculatePrayerTimes(
                calendar = tomorrowCal,
                latitude = loc.latitude,
                longitude = loc.longitude,
                timeZoneId = tzId,
                method = settingsRepo.calculationMethod.value,
                juristicMethod = settingsRepo.juristicMethod.value,
                dstMode = settingsRepo.dstMode.value,
                manualOffsetMinutes = settingsRepo.getOffsetsMap(),
                hijriOffsetDays = settingsRepo.hijriOffsetDays.value
            )
            next = PrayerType.FAJR to tomorrowResult.fajrMillis
        }

        val diffMs = (next.second - now).coerceAtLeast(0L)
        val totalSecs = diffMs / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        val countdownText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        _nextPrayerInfo.value = NextPrayerInfo(
            prayerType = next.first,
            targetTimeMillis = next.second,
            remainingMillis = diffMs,
            countdownText = countdownText
        )
    }

    private fun updateSalawatCountdown(now: Long) {
        val config = settingsRepo.salawatConfig.value
        if (!config.isEnabled) {
            _salawatRemainingSeconds.value = 0L
            return
        }
        val target = config.nextReminderTimeMillis
        if (target <= now) {
            _salawatRemainingSeconds.value = 0L
        } else {
            _salawatRemainingSeconds.value = ((target - now) / 1000).coerceAtLeast(0L)
        }
    }

    fun formatTime(timeMillis: Long, format: TimeFormatPreference, lang: Language): String {
        val pattern = when (format) {
            TimeFormatPreference.FORMAT_24H -> "HH:mm"
            TimeFormatPreference.FORMAT_12H -> "hh:mm a"
        }
        val locale = when (lang) {
            Language.ARABIC -> Locale("ar")
            Language.FRENCH -> Locale.FRENCH
            Language.ENGLISH -> Locale.US
        }
        val sdf = SimpleDateFormat(pattern, locale)
        sdf.timeZone = TimeZone.getTimeZone(settingsRepo.getEffectiveTimeZoneId())
        return sdf.format(Date(timeMillis))
    }

    fun formatClockTime(nowMillis: Long, format: TimeFormatPreference): Triple<String, String, String> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(settingsRepo.getEffectiveTimeZoneId())).apply {
            timeInMillis = nowMillis
        }
        val h = if (format == TimeFormatPreference.FORMAT_24H) {
            String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
        } else {
            val hour12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            String.format(Locale.US, "%02d", hour12)
        }
        val m = String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE))
        val s = String.format(Locale.US, "%02d", cal.get(Calendar.SECOND))
        return Triple(h, m, s)
    }

    fun getAmPmIndicator(nowMillis: Long, lang: Language): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(settingsRepo.getEffectiveTimeZoneId())).apply {
            timeInMillis = nowMillis
        }
        val isAm = cal.get(Calendar.AM_PM) == Calendar.AM
        return when (lang) {
            Language.ARABIC -> if (isAm) "ص" else "م"
            Language.ENGLISH -> if (isAm) "AM" else "PM"
            Language.FRENCH -> if (isAm) "AM" else "PM"
        }
    }

    // Alert Actions
    fun insertAlert(alert: AlertItem) = viewModelScope.launch {
        alertRepo.insertAlert(alert)
        AlarmScheduler.scheduleAll(getApplication())
    }

    fun updateAlert(alert: AlertItem) = viewModelScope.launch {
        alertRepo.updateAlert(alert)
        AlarmScheduler.scheduleAll(getApplication())
    }

    fun toggleAlertEnabled(alert: AlertItem, isEnabled: Boolean) = viewModelScope.launch {
        alertRepo.updateAlert(alert.copy(isEnabled = isEnabled))
        AlarmScheduler.scheduleAll(getApplication())
    }

    fun deleteAlert(alert: AlertItem) = viewModelScope.launch {
        alertRepo.deleteAlert(alert)
        AlarmScheduler.scheduleAll(getApplication())
    }

    fun triggerReschedule() = viewModelScope.launch {
        AlarmScheduler.scheduleAll(getApplication())
    }
}
