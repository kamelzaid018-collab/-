package com.example.calculator

import com.example.data.model.CalculationMethod
import com.example.data.model.DstMode
import com.example.data.model.JuristicMethod
import com.example.data.model.PrayerTimesResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object PrayerTimeCalculator {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    data class SolarCoordinates(
        val declination: Double, // degrees
        val equationOfTime: Double // hours
    )

    /**
     * Computes solar coordinates (declination and equation of time) for Julian Date.
     */
    private fun getSolarCoordinates(jd: Double): SolarCoordinates {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(g * DEG_TO_RAD) + 0.020 * sin(2.0 * g * DEG_TO_RAD))
        val e = 23.439 - 0.00000036 * d

        var ra = atan2(cos(e * DEG_TO_RAD) * sin(l * DEG_TO_RAD), cos(l * DEG_TO_RAD)) * RAD_TO_DEG
        ra = fixAngle(ra) / 15.0 // in hours

        val sinDelta = sin(e * DEG_TO_RAD) * sin(l * DEG_TO_RAD)
        val delta = asin(sinDelta) * RAD_TO_DEG

        val eqT = (q / 15.0) - ra
        return SolarCoordinates(declination = delta, equationOfTime = eqT)
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * Math.floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * Math.floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }

    /**
     * Computes the hour angle for a given zenith angle (in degrees), latitude, and solar declination.
     */
    private fun getHourAngle(zenithAngleDeg: Double, latDeg: Double, declinationDeg: Double): Double {
        val cosH = (cos(zenithAngleDeg * DEG_TO_RAD) - sin(latDeg * DEG_TO_RAD) * sin(declinationDeg * DEG_TO_RAD)) /
                (cos(latDeg * DEG_TO_RAD) * cos(declinationDeg * DEG_TO_RAD))
        val clamped = cosH.coerceIn(-1.0, 1.0)
        return acos(clamped) * RAD_TO_DEG / 15.0 // in hours
    }

    /**
     * Computes the hour angle for Asr prayer based on juristic shadow factor (1.0 for Shafi'i/Maliki/Hanbali, 2.0 for Hanafi).
     */
    private fun getAsrHourAngle(shadowFactor: Double, latDeg: Double, declinationDeg: Double): Double {
        val diff = abs(latDeg - declinationDeg)
        val cotAlt = shadowFactor + tan(diff * DEG_TO_RAD)
        val alt = atan(1.0 / cotAlt) * RAD_TO_DEG
        val zenith = 90.0 - alt
        return getHourAngle(zenith, latDeg, declinationDeg)
    }

    /**
     * Converts a fractional day hour (e.g. 15.75) into absolute epoch milliseconds for the target calendar date.
     */
    private fun hourToMillis(calendar: Calendar, hour: Double, timeZone: TimeZone): Long {
        val normalized = fixHour(hour)
        val h = normalized.toInt()
        val m = ((normalized - h) * 60.0).toInt()
        val s = ((((normalized - h) * 60.0) - m) * 60.0).toInt()

        val cal = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, s)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Calculates prayer times for a specified date and location.
     */
    fun calculatePrayerTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        method: CalculationMethod,
        juristicMethod: JuristicMethod,
        dstMode: DstMode,
        manualOffsetMinutes: Map<String, Int> = emptyMap(), // keys: "fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"
        hijriOffsetDays: Int = 0
    ): PrayerTimesResult {
        val timeZone = TimeZone.getTimeZone(timeZoneId)
        val rawOffsetHours = timeZone.rawOffset / 3600000.0

        val isDst = DstHelper.isDstActive(dstMode, calendar)
        val dstOffsetHours = if (isDst) 1.0 else 0.0
        val effectiveTzHours = rawOffsetHours + dstOffsetHours

        // Compute Julian Day for local midday
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        val a = Math.floor((14.0 - m) / 12.0).toInt()
        val yP = y + 4800 - a
        val mP = m + 12 * a - 3
        val jd = d + Math.floor((153.0 * mP + 2.0) / 5.0).toInt() +
                365 * yP + Math.floor(yP / 4.0).toInt() -
                Math.floor(yP / 100.0).toInt() + Math.floor(yP / 400.0).toInt() - 32045.0 - 0.5 + (12.0 - effectiveTzHours) / 24.0

        val solar = getSolarCoordinates(jd)

        // Midday / Solar Noon
        val noon = 12.0 + effectiveTzHours - (longitude / 15.0) - solar.equationOfTime

        // Sunrise & Sunset (Zenith angle standard is 90.833°)
        val sunRiseSetH = getHourAngle(90.833, latitude, solar.declination)
        val sunriseHour = noon - sunRiseSetH
        val sunsetHour = noon + sunRiseSetH

        // Fajr (Zenith = 90° + fajrAngle)
        val fajrH = getHourAngle(90.0 + method.fajrAngle, latitude, solar.declination)
        val fajrHour = noon - fajrH

        // Dhuhr (solar noon + slight margin e.g. 1-2 min after zenith)
        val dhuhrHour = noon + (2.0 / 60.0)

        // Asr
        val asrH = getAsrHourAngle(juristicMethod.shadowFactor, latitude, solar.declination)
        val asrHour = noon + asrH

        // Maghrib
        val maghribHour = if (method.maghribAngle > 0.0) {
            val maghribH = getHourAngle(90.0 + method.maghribAngle, latitude, solar.declination)
            noon + maghribH
        } else {
            sunsetHour + (2.0 / 60.0) // 2 min margin after sunset
        }

        // Isha
        val ishaHour = if (method.ishaMinutesAfterMaghrib > 0) {
            maghribHour + (method.ishaMinutesAfterMaghrib / 60.0)
        } else {
            val ishaH = getHourAngle(90.0 + method.ishaAngle, latitude, solar.declination)
            noon + ishaH
        }

        // Convert to Milliseconds
        var fajrMs = hourToMillis(calendar, fajrHour, timeZone)
        var sunriseMs = hourToMillis(calendar, sunriseHour, timeZone)
        var dhuhrMs = hourToMillis(calendar, dhuhrHour, timeZone)
        var asrMs = hourToMillis(calendar, asrHour, timeZone)
        var maghribMs = hourToMillis(calendar, maghribHour, timeZone)
        var ishaMs = hourToMillis(calendar, ishaHour, timeZone)

        // Apply manual offsets in minutes
        fajrMs += (manualOffsetMinutes["fajr"] ?: 0) * 60_000L
        sunriseMs += (manualOffsetMinutes["sunrise"] ?: 0) * 60_000L
        dhuhrMs += (manualOffsetMinutes["dhuhr"] ?: 0) * 60_000L
        asrMs += (manualOffsetMinutes["asr"] ?: 0) * 60_000L
        maghribMs += (manualOffsetMinutes["maghrib"] ?: 0) * 60_000L
        ishaMs += (manualOffsetMinutes["isha"] ?: 0) * 60_000L

        // Hijri date
        val hijri = HijriCalendarHelper.getHijriDate(calendar, hijriOffsetDays)

        // Day of week names
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayNamesAr = arrayOf("", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
        val dayNamesEn = arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val dayNamesFr = arrayOf("", "Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi")

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        sdf.timeZone = timeZone
        val gregDateText = sdf.format(calendar.time)

        return PrayerTimesResult(
            fajrMillis = fajrMs,
            sunriseMillis = sunriseMs,
            dhuhrMillis = dhuhrMs,
            asrMillis = asrMs,
            maghribMillis = maghribMs,
            ishaMillis = ishaMs,
            hijriDay = hijri.day,
            hijriMonth = hijri.month,
            hijriYear = hijri.year,
            hijriMonthNameAr = hijri.monthNameAr,
            hijriMonthNameEn = hijri.monthNameEn,
            hijriMonthNameFr = hijri.monthNameFr,
            gregorianDateText = gregDateText,
            dayOfWeekNameAr = dayNamesAr.getOrElse(dayOfWeek) { "" },
            dayOfWeekNameEn = dayNamesEn.getOrElse(dayOfWeek) { "" },
            dayOfWeekNameFr = dayNamesFr.getOrElse(dayOfWeek) { "" }
        )
    }
}
