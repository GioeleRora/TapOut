package com.tapout

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.tapout.data.local.AppDatabase
import com.tapout.data.repository.BillingManager
import com.tapout.data.repository.SettingsRepository

class TapOutApplication : Application() {

    companion object {
        const val ALARM_CHANNEL_ID = "tapout_alarm_channel_v1"
        const val BLOCKER_CHANNEL_ID = "tapout_blocker_channel_v1"

        lateinit var instance: TapOutApplication
            private set

        val database: AppDatabase by lazy {
            AppDatabase.getInstance(instance)
        }

        val settingsRepository: SettingsRepository by lazy {
            SettingsRepository(instance)
        }

        val billingManager: BillingManager by lazy {
            BillingManager(settingsRepository)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canale Sveglia Inarrestabile (Alta Priorità)
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 300, 800, 300, 1000)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(Settings.System.DEFAULT_ALARM_ALERT_URI ?: Uri.EMPTY, audioAttributes)
            }

            // Canale Blocco App
            val blockerChannel = NotificationChannel(
                BLOCKER_CHANNEL_ID,
                getString(R.string.blocker_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.blocker_channel_desc)
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(blockerChannel)
        }
    }
}
