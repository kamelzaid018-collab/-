package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetPrayer: PrayerType = PrayerType.ALL,
    val minutesBefore: Int = 15,
    val repeatDays: String = "1,2,3,4,5,6,7", // 1=Sunday, 7=Saturday
    val soundUri: String? = null,
    val soundName: String = "Default Chime",
    val isEnabled: Boolean = true
) {
    fun repeatsOnDay(calendarDayOfWeek: Int): Boolean {
        if (repeatDays.isBlank()) return true
        val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.contains(calendarDayOfWeek)
    }
}
