package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.TimeZone

class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("azan_prayer_settings", Context.MODE_PRIVATE)

    // Language
    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<Language> = _language.asStateFlow()

    // Location
    private val _selectedLocation = MutableStateFlow(loadLocation())
    val selectedLocation: StateFlow<LocationItem> = _selectedLocation.asStateFlow()

    private val _isAutoLocation = MutableStateFlow(prefs.getBoolean(KEY_IS_AUTO_LOCATION, false))
    val isAutoLocation: StateFlow<Boolean> = _isAutoLocation.asStateFlow()

    // Timezone
    private val _isAutoTimeZone = MutableStateFlow(prefs.getBoolean(KEY_IS_AUTO_TIMEZONE, true))
    val isAutoTimeZone: StateFlow<Boolean> = _isAutoTimeZone.asStateFlow()

    private val _manualTimeZoneId = MutableStateFlow(prefs.getString(KEY_MANUAL_TIMEZONE_ID, TimeZone.getDefault().id) ?: "Asia/Riyadh")
    val manualTimeZoneId: StateFlow<String> = _manualTimeZoneId.asStateFlow()

    // DST
    private val _dstMode = MutableStateFlow(
        DstMode.valueOf(prefs.getString(KEY_DST_MODE, DstMode.AUTO.name) ?: DstMode.AUTO.name)
    )
    val dstMode: StateFlow<DstMode> = _dstMode.asStateFlow()

    // Calculation Method
    private val _calculationMethod = MutableStateFlow(
        CalculationMethod.valueOf(prefs.getString(KEY_CALC_METHOD, CalculationMethod.MAKKAH.name) ?: CalculationMethod.MAKKAH.name)
    )
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    // Juristic Method (Asr)
    private val _juristicMethod = MutableStateFlow(
        JuristicMethod.valueOf(prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.STANDARD.name) ?: JuristicMethod.STANDARD.name)
    )
    val juristicMethod: StateFlow<JuristicMethod> = _juristicMethod.asStateFlow()

    // Time Format
    private val _timeFormat = MutableStateFlow(
        TimeFormatPreference.valueOf(prefs.getString(KEY_TIME_FORMAT, TimeFormatPreference.FORMAT_12H.name) ?: TimeFormatPreference.FORMAT_12H.name)
    )
    val timeFormat: StateFlow<TimeFormatPreference> = _timeFormat.asStateFlow()

    // Hijri Adjustment
    private val _hijriOffsetDays = MutableStateFlow(prefs.getInt(KEY_HIJRI_OFFSET, 0))
    val hijriOffsetDays: StateFlow<Int> = _hijriOffsetDays.asStateFlow()

    // Manual Prayer Offsets (minutes)
    private val _fajrOffset = MutableStateFlow(prefs.getInt(KEY_OFFSET_FAJR, 0))
    val fajrOffset: StateFlow<Int> = _fajrOffset.asStateFlow()

    private val _sunriseOffset = MutableStateFlow(prefs.getInt(KEY_OFFSET_SUNRISE, 0))
    val sunriseOffset: StateFlow<Int> = _sunriseOffset.asStateFlow()

    private val _dhuhrOffset = MutableStateFlow(prefs.getInt(KEY_OFFSET_DHUHR, 0))
    val dhuhrOffset: StateFlow<Int> = _dhuhrOffset.asStateFlow()

    private val _asrOffset = MutableStateFlow(prefs.getInt(KEY_OFFSET_ASR, 0))
    val asrOffset: StateFlow<Int> = _asrOffset.asStateFlow()

    private val _maghribOffset = MutableStateFlow(prefs.getInt(KEY_OFFSET_MAGHRIB, 0))
    val maghribOffset: StateFlow<Int> = _maghribOffset.asStateFlow()

    private val _ishaOffset = MutableStateFlow(prefs.getInt(KEY_OFFSET_ISHA, 0))
    val ishaOffset: StateFlow<Int> = _ishaOffset.asStateFlow()

    // Ramadan
    private val _ramadanConfig = MutableStateFlow(loadRamadanConfig())
    val ramadanConfig: StateFlow<RamadanConfig> = _ramadanConfig.asStateFlow()

    // Salawat
    private val _salawatConfig = MutableStateFlow(loadSalawatConfig())
    val salawatConfig: StateFlow<SalawatConfig> = _salawatConfig.asStateFlow()

    // Notification Bar & Widgets
    private val _isNotificationBarEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF_BAR_ENABLED, true))
    val isNotificationBarEnabled: StateFlow<Boolean> = _isNotificationBarEnabled.asStateFlow()

    private val _notificationBarStyle = MutableStateFlow(prefs.getString(KEY_NOTIF_BAR_STYLE, "DETAILED") ?: "DETAILED")
    val notificationBarStyle: StateFlow<String> = _notificationBarStyle.asStateFlow()

    private val _isNotificationCountdownEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF_COUNTDOWN, true))
    val isNotificationCountdownEnabled: StateFlow<Boolean> = _isNotificationCountdownEnabled.asStateFlow()

    private val _isNotificationHijriEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF_HIJRI, true))
    val isNotificationHijriEnabled: StateFlow<Boolean> = _isNotificationHijriEnabled.asStateFlow()

    private val _isWidgetHijriEnabled = MutableStateFlow(prefs.getBoolean(KEY_WIDGET_HIJRI, true))
    val isWidgetHijriEnabled: StateFlow<Boolean> = _isWidgetHijriEnabled.asStateFlow()

    private val _isWidgetLocationEnabled = MutableStateFlow(prefs.getBoolean(KEY_WIDGET_LOC, true))
    val isWidgetLocationEnabled: StateFlow<Boolean> = _isWidgetLocationEnabled.asStateFlow()

    private val _widgetThemeStyle = MutableStateFlow(prefs.getString(KEY_WIDGET_THEME, "EMERALD") ?: "EMERALD")
    val widgetThemeStyle: StateFlow<String> = _widgetThemeStyle.asStateFlow()

    private fun loadLanguage(): Language {
        val code = prefs.getString(KEY_LANGUAGE, "ar") ?: "ar"
        return Language.values().find { it.code == code } ?: Language.ARABIC
    }

    private fun loadLocation(): LocationItem {
        val locId = prefs.getString(KEY_LOCATION_ID, null)
        if (locId != null) {
            val found = LocationRepository.findById(locId)
            if (found != null) return found
        }
        val lat = prefs.getFloat(KEY_LOCATION_LAT, 21.4225f).toDouble()
        val lng = prefs.getFloat(KEY_LOCATION_LNG, 39.8262f).toDouble()
        val name = prefs.getString(KEY_LOCATION_NAME, "مكة المكرمة") ?: "مكة المكرمة"
        val tz = prefs.getString(KEY_LOCATION_TZ, "Asia/Riyadh") ?: "Asia/Riyadh"
        return LocationItem("custom", name, name, name, "", "", "", lat, lng, tz)
    }

    fun setLanguage(lang: Language) {
        prefs.edit().putString(KEY_LANGUAGE, lang.code).apply()
        _language.value = lang
    }

    fun setLocation(location: LocationItem, isAuto: Boolean = false) {
        prefs.edit()
            .putString(KEY_LOCATION_ID, location.id)
            .putFloat(KEY_LOCATION_LAT, location.latitude.toFloat())
            .putFloat(KEY_LOCATION_LNG, location.longitude.toFloat())
            .putString(KEY_LOCATION_NAME, location.nameAr)
            .putString(KEY_LOCATION_TZ, location.timeZoneId)
            .putBoolean(KEY_IS_AUTO_LOCATION, isAuto)
            .apply()
        _selectedLocation.value = location
        _isAutoLocation.value = isAuto
    }

    fun setIsAutoTimeZone(auto: Boolean) {
        prefs.edit().putBoolean(KEY_IS_AUTO_TIMEZONE, auto).apply()
        _isAutoTimeZone.value = auto
    }

    fun setManualTimeZoneId(tzId: String) {
        prefs.edit().putString(KEY_MANUAL_TIMEZONE_ID, tzId).apply()
        _manualTimeZoneId.value = tzId
    }

    fun setDstMode(mode: DstMode) {
        prefs.edit().putString(KEY_DST_MODE, mode.name).apply()
        _dstMode.value = mode
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_CALC_METHOD, method.name).apply()
        _calculationMethod.value = method
    }

    fun setJuristicMethod(method: JuristicMethod) {
        prefs.edit().putString(KEY_JURISTIC_METHOD, method.name).apply()
        _juristicMethod.value = method
    }

    fun setTimeFormat(format: TimeFormatPreference) {
        prefs.edit().putString(KEY_TIME_FORMAT, format.name).apply()
        _timeFormat.value = format
    }

    fun setHijriOffsetDays(days: Int) {
        prefs.edit().putInt(KEY_HIJRI_OFFSET, days).apply()
        _hijriOffsetDays.value = days
    }

    fun setPrayerOffset(prayer: PrayerType, offsetMinutes: Int) {
        val editor = prefs.edit()
        when (prayer) {
            PrayerType.FAJR -> { editor.putInt(KEY_OFFSET_FAJR, offsetMinutes); _fajrOffset.value = offsetMinutes }
            PrayerType.SUNRISE -> { editor.putInt(KEY_OFFSET_SUNRISE, offsetMinutes); _sunriseOffset.value = offsetMinutes }
            PrayerType.DHUHR -> { editor.putInt(KEY_OFFSET_DHUHR, offsetMinutes); _dhuhrOffset.value = offsetMinutes }
            PrayerType.ASR -> { editor.putInt(KEY_OFFSET_ASR, offsetMinutes); _asrOffset.value = offsetMinutes }
            PrayerType.MAGHRIB -> { editor.putInt(KEY_OFFSET_MAGHRIB, offsetMinutes); _maghribOffset.value = offsetMinutes }
            PrayerType.ISHA -> { editor.putInt(KEY_OFFSET_ISHA, offsetMinutes); _ishaOffset.value = offsetMinutes }
            else -> {}
        }
        editor.apply()
    }

    fun getOffsetsMap(): Map<String, Int> {
        return mapOf(
            "fajr" to _fajrOffset.value,
            "sunrise" to _sunriseOffset.value,
            "dhuhr" to _dhuhrOffset.value,
            "asr" to _asrOffset.value,
            "maghrib" to _maghribOffset.value,
            "isha" to _ishaOffset.value
        )
    }

    fun getEffectiveTimeZoneId(): String {
        return if (_isAutoTimeZone.value) {
            TimeZone.getDefault().id
        } else {
            _manualTimeZoneId.value
        }
    }

    // --- Azan Per-Prayer Config ---
    fun getAzanConfig(prayer: PrayerType): PrayerAzanConfig {
        val prefix = "azan_${prayer.name}_"
        val screenModeStr = prefs.getString("${prefix}screen_mode", AzanScreenMode.SLIDESHOW.name)
        val screenMode = try { AzanScreenMode.valueOf(screenModeStr!!) } catch (e: Exception) { AzanScreenMode.SLIDESHOW }
        val duration = prefs.getInt("${prefix}duration", 5)
        val extractedStr = prefs.getString("${prefix}extracted_images", "") ?: ""
        val list = if (extractedStr.isBlank()) emptyList() else extractedStr.split(";;")

        return PrayerAzanConfig(
            prayerType = prayer,
            azanSoundUri = prefs.getString("${prefix}sound_uri", null),
            azanSoundName = prefs.getString("${prefix}sound_name", null),
            timeAlertSoundUri = prefs.getString("${prefix}alert_sound_uri", null),
            timeAlertSoundName = prefs.getString("${prefix}alert_sound_name", null),
            duaaVideoUri = prefs.getString("${prefix}duaa_video_uri", null),
            duaaVideoName = prefs.getString("${prefix}duaa_video_name", null),
            screenMode = screenMode,
            slideshowDurationSeconds = duration,
            staticImageUri = prefs.getString("${prefix}static_img", null),
            zipArchiveUri = prefs.getString("${prefix}zip_uri", null),
            extractedImages = list
        )
    }

    fun saveAzanConfig(config: PrayerAzanConfig) {
        val prefix = "azan_${config.prayerType.name}_"
        prefs.edit()
            .putString("${prefix}sound_uri", config.azanSoundUri)
            .putString("${prefix}sound_name", config.azanSoundName)
            .putString("${prefix}alert_sound_uri", config.timeAlertSoundUri)
            .putString("${prefix}alert_sound_name", config.timeAlertSoundName)
            .putString("${prefix}duaa_video_uri", config.duaaVideoUri)
            .putString("${prefix}duaa_video_name", config.duaaVideoName)
            .putString("${prefix}screen_mode", config.screenMode.name)
            .putInt("${prefix}duration", config.slideshowDurationSeconds)
            .putString("${prefix}static_img", config.staticImageUri)
            .putString("${prefix}zip_uri", config.zipArchiveUri)
            .putString("${prefix}extracted_images", config.extractedImages.joinToString(";;"))
            .apply()
    }

    // --- Ramadan Config ---
    private fun loadRamadanConfig(): RamadanConfig {
        return RamadanConfig(
            isRamadanMode = prefs.getBoolean(KEY_RAMADAN_MODE, true),
            iftarCannonVideoUri = prefs.getString(KEY_IFTAR_CANNON_URI, null),
            iftarCannonVideoName = prefs.getString(KEY_IFTAR_CANNON_NAME, null),
            musaharatiVideoUri = prefs.getString(KEY_MUSAHARATI_URI, null),
            musaharatiVideoName = prefs.getString(KEY_MUSAHARATI_NAME, null),
            musaharatiIsFixedTime = prefs.getBoolean(KEY_MUSAHARATI_IS_FIXED, false),
            musaharatiFixedHour = prefs.getInt(KEY_MUSAHARATI_FIXED_H, 2),
            musaharatiFixedMinute = prefs.getInt(KEY_MUSAHARATI_FIXED_M, 30),
            musaharatiMinutesBeforeFajr = prefs.getInt(KEY_MUSAHARATI_MIN_BEFORE, 45),
            isMusaharatiEnabled = prefs.getBoolean(KEY_MUSAHARATI_ENABLED, true),
            isIftarCannonEnabled = prefs.getBoolean(KEY_IFTAR_ENABLED, true)
        )
    }

    fun saveRamadanConfig(config: RamadanConfig) {
        prefs.edit()
            .putBoolean(KEY_RAMADAN_MODE, config.isRamadanMode)
            .putString(KEY_IFTAR_CANNON_URI, config.iftarCannonVideoUri)
            .putString(KEY_IFTAR_CANNON_NAME, config.iftarCannonVideoName)
            .putString(KEY_MUSAHARATI_URI, config.musaharatiVideoUri)
            .putString(KEY_MUSAHARATI_NAME, config.musaharatiVideoName)
            .putBoolean(KEY_MUSAHARATI_IS_FIXED, config.musaharatiIsFixedTime)
            .putInt(KEY_MUSAHARATI_FIXED_H, config.musaharatiFixedHour)
            .putInt(KEY_MUSAHARATI_FIXED_M, config.musaharatiFixedMinute)
            .putInt(KEY_MUSAHARATI_MIN_BEFORE, config.musaharatiMinutesBeforeFajr)
            .putBoolean(KEY_MUSAHARATI_ENABLED, config.isMusaharatiEnabled)
            .putBoolean(KEY_IFTAR_ENABLED, config.isIftarCannonEnabled)
            .apply()
        _ramadanConfig.value = config
    }

    // --- Salawat Config ---
    private fun loadSalawatConfig(): SalawatConfig {
        val modeStr = prefs.getString(KEY_SALAWAT_MODE, SalawatPlayMode.ORDER.name)
        val mode = try { SalawatPlayMode.valueOf(modeStr!!) } catch (e: Exception) { SalawatPlayMode.ORDER }
        val soundsStr = prefs.getString(KEY_SALAWAT_SOUNDS, "") ?: ""
        val list = if (soundsStr.isBlank()) emptyList() else soundsStr.split(";;")

        return SalawatConfig(
            isEnabled = prefs.getBoolean(KEY_SALAWAT_ENABLED, true),
            intervalMinutes = prefs.getInt(KEY_SALAWAT_INTERVAL, 15),
            playMode = mode,
            specificSoundUri = prefs.getString(KEY_SALAWAT_SPECIFIC_URI, null),
            specificSoundName = prefs.getString(KEY_SALAWAT_SPECIFIC_NAME, null),
            zipArchiveUri = prefs.getString(KEY_SALAWAT_ZIP_URI, null),
            extractedSounds = list,
            lastPlayedIndex = prefs.getInt(KEY_SALAWAT_LAST_INDEX, 0),
            nextReminderTimeMillis = prefs.getLong(KEY_SALAWAT_NEXT_TIME, 0L)
        )
    }

    fun saveSalawatConfig(config: SalawatConfig) {
        prefs.edit()
            .putBoolean(KEY_SALAWAT_ENABLED, config.isEnabled)
            .putInt(KEY_SALAWAT_INTERVAL, config.intervalMinutes)
            .putString(KEY_SALAWAT_MODE, config.playMode.name)
            .putString(KEY_SALAWAT_SPECIFIC_URI, config.specificSoundUri)
            .putString(KEY_SALAWAT_SPECIFIC_NAME, config.specificSoundName)
            .putString(KEY_SALAWAT_ZIP_URI, config.zipArchiveUri)
            .putString(KEY_SALAWAT_SOUNDS, config.extractedSounds.joinToString(";;"))
            .putInt(KEY_SALAWAT_LAST_INDEX, config.lastPlayedIndex)
            .putLong(KEY_SALAWAT_NEXT_TIME, config.nextReminderTimeMillis)
            .apply()
        _salawatConfig.value = config
    }

    fun setNotificationBarEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_BAR_ENABLED, enabled).commit()
        _isNotificationBarEnabled.value = enabled
    }

    fun setNotificationBarStyle(style: String) {
        prefs.edit().putString(KEY_NOTIF_BAR_STYLE, style).commit()
        _notificationBarStyle.value = style
    }

    fun setNotificationCountdownEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_COUNTDOWN, enabled).commit()
        _isNotificationCountdownEnabled.value = enabled
    }

    fun setNotificationHijriEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_HIJRI, enabled).commit()
        _isNotificationHijriEnabled.value = enabled
    }

    fun setWidgetHijriEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIDGET_HIJRI, enabled).commit()
        _isWidgetHijriEnabled.value = enabled
    }

    fun setWidgetLocationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIDGET_LOC, enabled).commit()
        _isWidgetLocationEnabled.value = enabled
    }

    fun setWidgetThemeStyle(theme: String) {
        prefs.edit().putString(KEY_WIDGET_THEME, theme).commit()
        _widgetThemeStyle.value = theme
    }

    companion object {
        private const val KEY_LANGUAGE = "key_lang"
        private const val KEY_LOCATION_ID = "key_loc_id"
        private const val KEY_LOCATION_LAT = "key_loc_lat"
        private const val KEY_LOCATION_LNG = "key_loc_lng"
        private const val KEY_LOCATION_NAME = "key_loc_name"
        private const val KEY_LOCATION_TZ = "key_loc_tz"
        private const val KEY_IS_AUTO_LOCATION = "key_is_auto_loc"
        private const val KEY_IS_AUTO_TIMEZONE = "key_is_auto_tz"
        private const val KEY_MANUAL_TIMEZONE_ID = "key_manual_tz_id"
        private const val KEY_DST_MODE = "key_dst_mode"
        private const val KEY_CALC_METHOD = "key_calc_method"
        private const val KEY_JURISTIC_METHOD = "key_juristic_method"
        private const val KEY_TIME_FORMAT = "key_time_format"
        private const val KEY_HIJRI_OFFSET = "key_hijri_offset"

        private const val KEY_NOTIF_BAR_ENABLED = "key_notif_bar_enabled"
        private const val KEY_NOTIF_BAR_STYLE = "key_notif_bar_style"
        private const val KEY_NOTIF_COUNTDOWN = "key_notif_countdown"
        private const val KEY_NOTIF_HIJRI = "key_notif_hijri"
        private const val KEY_WIDGET_HIJRI = "key_widget_hijri"
        private const val KEY_WIDGET_LOC = "key_widget_loc"
        private const val KEY_WIDGET_THEME = "key_widget_theme"

        private const val KEY_OFFSET_FAJR = "key_off_fajr"
        private const val KEY_OFFSET_SUNRISE = "key_off_sunrise"
        private const val KEY_OFFSET_DHUHR = "key_off_dhuhr"
        private const val KEY_OFFSET_ASR = "key_off_asr"
        private const val KEY_OFFSET_MAGHRIB = "key_off_maghrib"
        private const val KEY_OFFSET_ISHA = "key_off_isha"

        private const val KEY_RAMADAN_MODE = "key_ramadan_mode"
        private const val KEY_IFTAR_CANNON_URI = "key_iftar_uri"
        private const val KEY_IFTAR_CANNON_NAME = "key_iftar_name"
        private const val KEY_MUSAHARATI_URI = "key_musaharati_uri"
        private const val KEY_MUSAHARATI_NAME = "key_musaharati_name"
        private const val KEY_MUSAHARATI_IS_FIXED = "key_musaharati_is_fixed"
        private const val KEY_MUSAHARATI_FIXED_H = "key_musaharati_fixed_h"
        private const val KEY_MUSAHARATI_FIXED_M = "key_musaharati_fixed_m"
        private const val KEY_MUSAHARATI_MIN_BEFORE = "key_musaharati_min_before"
        private const val KEY_MUSAHARATI_ENABLED = "key_musaharati_enabled"
        private const val KEY_IFTAR_ENABLED = "key_iftar_enabled"

        private const val KEY_SALAWAT_ENABLED = "key_salawat_enabled"
        private const val KEY_SALAWAT_INTERVAL = "key_salawat_interval"
        private const val KEY_SALAWAT_MODE = "key_salawat_mode"
        private const val KEY_SALAWAT_SPECIFIC_URI = "key_salawat_spec_uri"
        private const val KEY_SALAWAT_SPECIFIC_NAME = "key_salawat_spec_name"
        private const val KEY_SALAWAT_ZIP_URI = "key_salawat_zip_uri"
        private const val KEY_SALAWAT_SOUNDS = "key_salawat_sounds"
        private const val KEY_SALAWAT_LAST_INDEX = "key_salawat_last_idx"
        private const val KEY_SALAWAT_NEXT_TIME = "key_salawat_next_time"
    }
}
