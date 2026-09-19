package com.example.data.repository

import com.example.data.database.AlertDao
import com.example.data.model.AlertItem
import kotlinx.coroutines.flow.Flow

class AlertRepository(private val alertDao: AlertDao) {

    val allAlerts: Flow<List<AlertItem>> = alertDao.getAllAlerts()

    suspend fun getActiveAlerts(): List<AlertItem> = alertDao.getActiveAlerts()

    suspend fun insertAlert(alert: AlertItem): Long = alertDao.insertAlert(alert)

    suspend fun updateAlert(alert: AlertItem) = alertDao.updateAlert(alert)

    suspend fun deleteAlert(alert: AlertItem) = alertDao.deleteAlert(alert)

    suspend fun deleteAlertById(id: Long) = alertDao.deleteAlertById(id)
}
