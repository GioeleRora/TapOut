package com.tapout.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.tapout.service.AlarmService
import com.tapout.ui.screens.alarmactive.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_ALARM_TRIGGER) {
            return
        }

        // Sveglia e accende lo schermo con priorità massima
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "TapOut:AlarmReceiverWakeLock"
        )
        wakeLock.acquire(60 * 1000L) // 60 secondi di wakelock solido

        // 1. Avvia ForegroundService per suono continuo, AudioGuard e forzatura volume
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtras(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Avvia DIRETTAMENTE l'Activity a schermo intero: AlarmReceiver ha l'esenzione
        // ufficiale AlarmClock per avviare Activity anche se l'app è completamente chiusa
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            action = "com.tapout.action.ALARM_RING"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtras(intent)
        }

        try {
            context.startActivity(activityIntent)
        } catch (_: Exception) {}
    }
}
