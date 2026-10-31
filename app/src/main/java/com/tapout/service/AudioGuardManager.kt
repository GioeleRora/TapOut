package com.tapout.service

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioGuardManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var volumeObserver: ContentObserver? = null
    private var guardJob: Job? = null
    private var vibrationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startGuard(enableVibration: Boolean) {
        forceMaxVolume()

        // Registra ContentObserver per intercettare la pressione dei tasti bilanciere hardware
        volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                forceMaxVolume()
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver!!
        )

        // Loop di monitoraggio periodico ogni 100ms
        guardJob = scope.launch {
            while (isActive) {
                forceMaxVolume()
                delay(100)
            }
        }

        if (enableVibration) {
            startVibration()
        }
    }

    private fun forceMaxVolume() {
        try {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (currentVol < maxVol) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
            }
        } catch (_: Exception) {}
        ensureDndBypass()
    }

    private fun ensureDndBypass() {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager?.isNotificationPolicyAccessGranted == true) {
                // Imposta il filtro Non Disturbare per consentire sempre gli allarmi
                notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS)
            }
        } catch (_: Exception) {}
    }

    private fun startVibration() {
        vibrationJob = scope.launch {
            val pattern = longArrayOf(0, 600, 200, 600, 200, 800)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(android.media.AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()

            while (isActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(effect, audioAttributes)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, 0)
                }
                delay(3000)
            }
        }
    }

    fun stopGuard() {
        volumeObserver?.let {
            try {
                context.contentResolver.unregisterContentObserver(it)
            } catch (_: Exception) {}
        }
        volumeObserver = null

        guardJob?.cancel()
        guardJob = null

        vibrationJob?.cancel()
        vibrationJob = null

        try {
            vibrator.cancel()
        } catch (_: Exception) {}
    }
}
