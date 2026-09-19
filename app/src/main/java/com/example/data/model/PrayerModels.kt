package com.example.data.model

enum class Language(val code: String, val displayName: String, val isRtl: Boolean) {
    ARABIC("ar", "العربية", true),
    ENGLISH("en", "English", false),
    FRENCH("fr", "Français", false)
}

enum class PrayerType(val id: Int) {
    FAJR(0),
    SUNRISE(1),
    DHUHR(2),
    ASR(3),
    MAGHRIB(4),
    ISHA(5),
    JUMUAH(6),
    ALL(7)
}

enum class CalculationMethod(
    val id: Int,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int = 0,
    val maghribAngle: Double = 0.0
) {
    MWL(1, 18.0, 17.0), // Muslim World League
    ISNA(2, 15.0, 15.0), // Islamic Society of North America
    EGYPT(3, 19.5, 17.5), // Egyptian General Authority of Survey
    MAKKAH(4, 18.5, 0.0, ishaMinutesAfterMaghrib = 90), // Umm al-Qura Makkah
    KARACHI(5, 18.0, 18.0), // Univ. of Islamic Sciences Karachi
    TEHRAN(6, 17.7, 14.0, maghribAngle = 4.5), // Institute of Geophysics Tehran
    SHIA_ITHNA(7, 16.0, 14.0, maghribAngle = 4.0), // Shia Ithna-Ashari
    GULF(8, 18.2, 0.0, ishaMinutesAfterMaghrib = 90), // Gulf Region
    KUWAIT(9, 18.0, 17.5), // Kuwait
    QATAR(10, 18.0, 0.0, ishaMinutesAfterMaghrib = 90), // Qatar
    MUIS(11, 20.0, 18.0), // Singapore MUIS
    FRANCE(12, 12.0, 12.0), // Union des Organisations Islamiques de France
    TURKEY(13, 18.0, 17.0) // Diyanet Turkey
}

enum class JuristicMethod(val id: Int, val shadowFactor: Double) {
    STANDARD(1, 1.0), // Shafi'i, Maliki, Hanbali
    HANAFI(2, 2.0)    // Hanafi
}

enum class DstMode {
    AUTO,       // Automatic based on Egyptian/regional rule (last Thursday of April to last Thursday of October)
    ALWAYS_ON,  // Manually On
    ALWAYS_OFF  // Manually Off
}

enum class TimeFormatPreference {
    FORMAT_12H,
    FORMAT_24H
}

data class LocationItem(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val nameFr: String,
    val countryAr: String,
    val countryEn: String,
    val countryFr: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val defaultMethod: CalculationMethod = CalculationMethod.MWL
) {
    fun getName(lang: Language): String = when (lang) {
        Language.ARABIC -> nameAr
        Language.FRENCH -> nameFr
        Language.ENGLISH -> nameEn
    }

    fun getCountry(lang: Language): String = when (lang) {
        Language.ARABIC -> countryAr
        Language.FRENCH -> countryFr
        Language.ENGLISH -> countryEn
    }

    fun getDisplayName(lang: Language): String {
        val city = getName(lang)
        val country = getCountry(lang)
        return if (country.isBlank()) city else "$city, $country"
    }
}

data class PrayerTimesResult(
    val fajrMillis: Long,
    val sunriseMillis: Long,
    val dhuhrMillis: Long,
    val asrMillis: Long,
    val maghribMillis: Long,
    val ishaMillis: Long,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriYear: Int,
    val hijriMonthNameAr: String,
    val hijriMonthNameEn: String,
    val hijriMonthNameFr: String,
    val gregorianDateText: String,
    val dayOfWeekNameAr: String,
    val dayOfWeekNameEn: String,
    val dayOfWeekNameFr: String
) {
    fun getDayOfWeek(lang: Language): String = when (lang) {
        Language.ARABIC -> dayOfWeekNameAr
        Language.FRENCH -> dayOfWeekNameFr
        Language.ENGLISH -> dayOfWeekNameEn
    }

    fun getHijriDateText(lang: Language): String {
        val mName = when (lang) {
            Language.ARABIC -> hijriMonthNameAr
            Language.FRENCH -> hijriMonthNameFr
            Language.ENGLISH -> hijriMonthNameEn
        }
        val suffix = when (lang) {
            Language.ARABIC -> "هـ"
            Language.FRENCH -> "AH"
            Language.ENGLISH -> "AH"
        }
        return "$hijriDay $mName $hijriYear $suffix"
    }

    fun getTimeMillis(prayer: PrayerType): Long {
        return when (prayer) {
            PrayerType.FAJR -> fajrMillis
            PrayerType.SUNRISE -> sunriseMillis
            PrayerType.DHUHR, PrayerType.JUMUAH -> dhuhrMillis
            PrayerType.ASR -> asrMillis
            PrayerType.MAGHRIB -> maghribMillis
            PrayerType.ISHA -> ishaMillis
            PrayerType.ALL -> fajrMillis
        }
    }
}
