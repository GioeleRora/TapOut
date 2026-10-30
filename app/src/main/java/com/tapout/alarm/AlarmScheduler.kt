package com.tapout.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tapout.TapOutApplication
import com.tapout.data.local.entity.AlarmEntity
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm.id)
            return
        }

        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.daysOfWeek)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_REQUIRE_NFC, alarm.requireNfc)
            putExtra(EXTRA_REQUIRE_DANCE, alarm.requireDance)
            putExtra(EXTRA_REQUIRE_SMILE_KISS, alarm.requireSmileKiss)
            putExtra(EXTRA_SOUND_URI, alarm.soundUri)
            putExtra(EXTRA_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ShowPendingIntent per la visualizzazione dell'orario esatto nella lockscreen di sistema
        val showIntent = Intent(context, com.tapout.ui.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            // Su Android 12+ se manca SCHEDULE_EXACT_ALARM
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAllActiveAlarms() {
        val activeAlarms = TapOutApplication.database.alarmDao().getActiveAlarms()
        for (alarm in activeAlarms) {
            scheduleAlarm(alarm)
        }
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeek: String): Long {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val activeDays = if (daysOfWeek.isBlank()) {
            emptyList()
        } else {
            daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        }

        if (activeDays.isEmpty()) {
            // Sveglia una tantum: se l'orario è già passato oggi, suonerà domani alla stessa ora
            if (calendar.timeInMillis <= now.timeInMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // Sveglia ripetuta: cerca il giorno valido più vicino
        while (true) {
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            if (activeDays.contains(dayOfWeek) && calendar.timeInMillis > now.timeInMillis) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.tapout.ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_REQUIRE_NFC = "extra_require_nfc"
        const val EXTRA_REQUIRE_DANCE = "extra_require_dance"
        const val EXTRA_REQUIRE_SMILE_KISS = "extra_require_smile_kiss"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_VIBRATE = "extra_vibrate"
    }
}
