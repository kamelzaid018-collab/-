package com.example.calculator

import com.example.data.model.Language
import java.util.Calendar
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val month: Int, // 1 to 12
    val year: Int,
    val monthNameAr: String,
    val monthNameEn: String,
    val monthNameFr: String
) {
    fun format(language: Language): String {
        val monthName = when (language) {
            Language.ARABIC -> monthNameAr
            Language.ENGLISH -> monthNameEn
            Language.FRENCH -> monthNameFr
        }
        return when (language) {
            Language.ARABIC -> "$day $monthName $year هـ"
            Language.ENGLISH -> "$day $monthName $year AH"
            Language.FRENCH -> "$day $monthName $year AH"
        }
    }
}

object HijriCalendarHelper {

    private val AR_MONTHS = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val EN_MONTHS = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val FR_MONTHS = arrayOf(
        "Mouharram", "Safar", "Rabi al-Awwal", "Rabi ath-Thani",
        "Joumada al-Oula", "Joumada ath-Thania", "Rajab", "Cha'bane",
        "Ramadan", "Chawwal", "Dhou al-Qi'da", "Dhou al-Hijja"
    )

    /**
     * Converts a Gregorian calendar date + manual offset (in days) to Hijri date.
     * Uses the Umm al-Qura astronomical algorithm approximation.
     */
    fun getHijriDate(calendar: Calendar = Calendar.getInstance(), manualOffsetDays: Int = 0): HijriDate {
        val cal = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            add(Calendar.DAY_OF_MONTH, manualOffsetDays)
        }

        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1 // 1-12
        val d = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Day Number calculation
        val a = floor((14.0 - m) / 12.0).toInt()
        val yPrime = y + 4800 - a
        val mPrime = m + 12 * a - 3
        val jd = d + floor((153.0 * mPrime + 2.0) / 5.0).toInt() +
                365 * yPrime + floor(yPrime / 4.0).toInt() -
                floor(yPrime / 100.0).toInt() + floor(yPrime / 400.0).toInt() - 32045

        // Approximate Islamic calendar from Julian Day
        val l = jd - 1948440 + 10632
        val n = floor((l - 1.0) / 10631.0).toInt()
        val lPrime = l - 10631 * n + 354
        val j = (floor((10985.0 - lPrime) / 5316.0) * floor((50.0 * lPrime) / 17719.0) +
                floor(lPrime / 5670.0) * floor((43.0 * lPrime) / 15238.0)).toInt()
        val lDoublePrime = lPrime - floor((30.0 - j) / 15.0).toInt() * floor((17719.0 * j) / 50.0).toInt() -
                floor(j / 16.0).toInt() * floor((15238.0 * j) / 43.0).toInt() + 29
        val hMonth = floor((24.0 * lDoublePrime) / 709.0).toInt()
        val hDay = lDoublePrime - floor((709.0 * hMonth) / 24.0).toInt()
        val hYear = 30 * n + j - 30

        val validMonth = ((hMonth - 1) % 12 + 12) % 12
        val monthIdx = validMonth.coerceIn(0, 11)

        return HijriDate(
            day = hDay.coerceIn(1, 30),
            month = monthIdx + 1,
            year = hYear,
            monthNameAr = AR_MONTHS[monthIdx],
            monthNameEn = EN_MONTHS[monthIdx],
            monthNameFr = FR_MONTHS[monthIdx]
        )
    }

    fun isRamadan(calendar: Calendar = Calendar.getInstance(), manualOffsetDays: Int = 0): Boolean {
        return getHijriDate(calendar, manualOffsetDays).month == 9
    }
}
