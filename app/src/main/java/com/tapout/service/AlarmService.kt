package com.tapout.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tapout.R
import com.tapout.TapOutApplication
import com.tapout.alarm.AlarmScheduler
import com.tapout.ui.screens.alarmactive.AlarmActivity

class AlarmService : Service() {

    private lateinit var audioGuard: AudioGuardManager
    private lateinit var toneHelper: ToneGeneratorHelper

    override fun onCreate() {
        super.onCreate()
        audioGuard = AudioGuardManager(this)
        toneHelper = ToneGeneratorHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM

        when (action) {
            ACTION_START_ALARM -> {
                isAlarmRinging = true
                val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
                val label = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Sveglia TapOut"
                val soundUri = intent?.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI) ?: ""
                val vibrate = intent?.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true) ?: true
                val requireNfc = intent?.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_NFC, true) ?: true
                val requireDance = intent?.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_DANCE, true) ?: true
                val requireSmileKiss = intent?.getBooleanExtra(AlarmScheduler.EXTRA_REQUIRE_SMILE_KISS, false) ?: false

                startForegroundServiceWithNotification(alarmId, label, requireNfc, requireDance, requireSmileKiss)
                audioGuard.startGuard(vibrate)
                toneHelper.startAlarmSound(soundUri)
            }

            ACTION_DUCK_AUDIO -> {
                toneHelper.setDuckedVolume(true)
            }

            ACTION_UNDUCK_AUDIO -> {
                toneHelper.setDuckedVolume(false)
            }

            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
        }

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification(
        alarmId: Long,
        label: String,
        requireNfc: Boolean,
        requireDance: Boolean,
        requireSmileKiss: Boolean
    ) {
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            action = "com.tapout.action.ALARM_RING"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_REQUIRE_NFC, requireNfc)
            putExtra(AlarmScheduler.EXTRA_REQUIRE_DANCE, requireDance)
            putExtra(AlarmScheduler.EXTRA_REQUIRE_SMILE_KISS, requireSmileKiss)
        }

        val activityOptionsBundle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.app.ActivityOptions.makeBasic().apply {
                setPendingIntentBackgroundActivityStartMode(
                    android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }.toBundle()
        } else {
            null
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            activityOptionsBundle
        )

        val notification: Notification = NotificationCompat.Builder(this, TapOutApplication.ALARM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.alarm_ringing_title))
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Tenta l'avvio immediato a schermo intero
        try {
            startActivity(fullScreenIntent)
        } catch (_: Exception) {}
    }

    private fun stopAlarm() {
        isAlarmRinging = false
        audioGuard.stopGuard()
        toneHelper.stopAlarmSound()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_ALARM = "com.tapout.action.START_ALARM"
        const val ACTION_STOP_ALARM = "com.tapout.action.STOP_ALARM"
        const val ACTION_DUCK_AUDIO = "com.tapout.action.DUCK_AUDIO"
        const val ACTION_UNDUCK_AUDIO = "com.tapout.action.UNDUCK_AUDIO"
        const val NOTIFICATION_ID = 9001

        @Volatile
        var isAlarmRinging: Boolean = false
            private set
    }
}
