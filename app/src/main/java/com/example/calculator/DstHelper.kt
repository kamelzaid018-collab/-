package com.example.calculator

import com.example.data.model.DstMode
import java.util.Calendar

object DstHelper {

    /**
     * Finds the day of the month for the last Thursday of a given month (0-indexed, so April=3, October=9).
     */
    fun getLastThursday(year: Int, monthZeroIndexed: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroIndexed)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)

        // Calendar.THURSDAY is 5
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Determines whether DST is active automatically according to the rule:
     * Starts on the last Thursday of April at midnight/24:00,
     * Ends on the last Thursday of October at midnight/24:00.
     */
    fun isDstActiveAuto(calendar: Calendar = Calendar.getInstance()): Boolean {
        val year = calendar.get(Calendar.YEAR)
        val lastThursdayApril = getLastThursday(year, Calendar.APRIL)
        val lastThursdayOctober = getLastThursday(year, Calendar.OCTOBER)

        val aprilStart = Calendar.getInstance().apply {
            set(year, Calendar.APRIL, lastThursdayApril, 23, 59, 59)
        }
        val octoberEnd = Calendar.getInstance().apply {
            set(year, Calendar.OCTOBER, lastThursdayOctober, 23, 59, 59)
        }

        val time = calendar.timeInMillis
        return time in aprilStart.timeInMillis..octoberEnd.timeInMillis
    }

    /**
     * Resolves DST state based on mode and auto rule.
     */
    fun isDstActive(mode: DstMode, calendar: Calendar = Calendar.getInstance()): Boolean {
        return when (mode) {
            DstMode.ALWAYS_ON -> true
            DstMode.ALWAYS_OFF -> false
            DstMode.AUTO -> isDstActiveAuto(calendar)
        }
    }
}
