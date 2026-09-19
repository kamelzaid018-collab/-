package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.service.PrayerKeeperService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "Boot/System event received: $action - Rescheduling all alarms.")

        PrayerKeeperService.start(context)

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    AlarmScheduler.scheduleAll(context)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
