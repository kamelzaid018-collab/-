package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import com.example.data.model.AlertItem
import com.example.data.model.PrayerType
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromPrayerType(value: PrayerType): String = value.name

    @TypeConverter
    fun toPrayerType(value: String): PrayerType {
        return try {
            PrayerType.valueOf(value)
        } catch (e: Exception) {
            PrayerType.ALL
        }
    }
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY id ASC")
    fun getAllAlerts(): Flow<List<AlertItem>>

    @Query("SELECT * FROM alerts WHERE isEnabled = 1")
    suspend fun getActiveAlerts(): List<AlertItem>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getAlertById(id: Long): AlertItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertItem): Long

    @Update
    suspend fun updateAlert(alert: AlertItem)

    @Delete
    suspend fun deleteAlert(alert: AlertItem)

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Long)
}
