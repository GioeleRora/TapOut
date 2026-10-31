package com.tapout.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class ToneGeneratorHelper(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var fallbackTrack: AudioTrack? = null
    private var fallbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startAlarmSound(soundUriString: String) {
        stopAlarmSound()

        val uri = if (soundUriString.isNotBlank()) {
            Uri.parse(soundUriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            // Se fallisce l'apertura del file, avvia la sirena d'emergenza sintetizzata
            startSynthesizedSiren()
        }
    }

    private fun startSynthesizedSiren() {
        fallbackJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            fallbackTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            fallbackTrack?.play()

            val shortBuffer = ShortArray(bufferSize / 2)
            var phase = 0.0
            var tick = 0

            while (isActive) {
                // Alternanza di frequenza tra 880Hz e 1760Hz ogni 200ms
                val currentFreq = if ((tick / 20) % 2 == 0) 880.0 else 1760.0
                val phaseIncrement = 2.0 * Math.PI * currentFreq / sampleRate

                for (i in shortBuffer.indices) {
                    shortBuffer[i] = (sin(phase) * 32767).toInt().toShort()
                    phase += phaseIncrement
                    if (phase > 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI
                    }
                }

                fallbackTrack?.write(shortBuffer, 0, shortBuffer.size)
                tick++
            }
        }
    }

    fun setDuckedVolume(ducked: Boolean) {
        val volume = if (ducked) 0.3f else 1.0f
        try {
            mediaPlayer?.setVolume(volume, volume)
            fallbackTrack?.setVolume(volume)
        } catch (_: Exception) {}
    }

    fun stopAlarmSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        fallbackJob?.cancel()
        fallbackJob = null

        try {
            fallbackTrack?.stop()
            fallbackTrack?.release()
        } catch (_: Exception) {}
        fallbackTrack = null
    }
}
