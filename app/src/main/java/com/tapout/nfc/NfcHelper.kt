package com.tapout.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class NfcHelper(private val context: Context) {

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    /**
     * Attiva ReaderMode ad alte prestazioni con FLAG_READER_SKIP_NDEF_CHECK
     * per rilevare istantaneamente l'UID del chip senza attese NDEF.
     */
    fun enableReaderMode(activity: Activity, onTagScanned: (String) -> Unit) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        adapter.enableReaderMode(
            activity,
            { tag: Tag? ->
                tag?.let {
                    val uidHex = bytesToHex(it.id)
                    activity.runOnUiThread {
                        onTagScanned(uidHex)
                    }
                }
            },
            flags,
            Bundle()
        )
    }

    fun disableReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    /**
     * Foreground Dispatch come fallback nel caso in cui il dispositivo/ROM
     * consegni il tag tramite Intent a livello di sistema operativo.
     */
    fun enableForegroundDispatch(activity: Activity) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        )

        try {
            adapter.enableForegroundDispatch(activity, pendingIntent, filters, null)
        } catch (_: Exception) {}
    }

    fun disableForegroundDispatch(activity: Activity) {
        try {
            nfcAdapter?.disableForegroundDispatch(activity)
        } catch (_: Exception) {}
    }

    /**
     * Tenta l'attivazione automatica del chip NFC o apre il pannello rapido nativo in 1 tap.
     */
    fun tryEnableNfcOrOpenPrompt(activity: Activity) {
        val adapter = nfcAdapter ?: return
        if (adapter.isEnabled) return

        try {
            val enableMethod = adapter.javaClass.getMethod("enable")
            val success = enableMethod.invoke(adapter) as? Boolean
            if (success == true || adapter.isEnabled) {
                return
            }
        } catch (_: Exception) {}

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val panelIntent = Intent(android.provider.Settings.Panel.ACTION_NFC).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(panelIntent)
            } else {
                val settingsIntent = Intent(android.provider.Settings.ACTION_NFC_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(settingsIntent)
            }
        } catch (_: Exception) {
            val settingsIntent = Intent(android.provider.Settings.ACTION_NFC_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(settingsIntent)
        }
    }

    companion object {
        fun bytesToHex(bytes: ByteArray): String {
            val sb = StringBuilder()
            for (i in bytes.indices) {
                sb.append(String.format("%02X", bytes[i]))
                if (i < bytes.size - 1) {
                    sb.append(":")
                }
            }
            return sb.toString()
        }

        /**
         * Normalizza l'UID rimuovendo separatori (: , - , spazi) e convertendo in maiuscolo.
         * Rende impossibile qualsiasi errore di matching dovuto a formattazione.
         */
        fun normalizeUid(uid: String): String {
            return uid.replace(":", "")
                .replace("-", "")
                .replace(" ", "")
                .trim()
                .uppercase()
        }

        /**
         * Verifica se il tag scansionato corrisponde a uno qualunque dei tag registrati.
         */
        fun isTagValid(scannedUid: String, registeredUids: List<String>, isTestMode: Boolean): Boolean {
            if (isTestMode && scannedUid == "TEST_TAG_SIMULATION") {
                return true
            }
            if (registeredUids.isEmpty()) {
                // Se nessun tag è registrato, qualsiasi tag fisico scansionato disattiva per sicurezza
                return true
            }
            val normScanned = normalizeUid(scannedUid)
            return registeredUids.any { normalizeUid(it) == normScanned }
        }

        fun isTagValid(scannedUid: String, registeredUid: String?, isTestMode: Boolean): Boolean {
            return if (registeredUid.isNullOrBlank()) {
                isTagValid(scannedUid, emptyList(), isTestMode)
            } else {
                isTagValid(scannedUid, listOf(registeredUid), isTestMode)
            }
        }

        /**
         * Estrae l'UID esadecimale da un Intent NFC consegnato dal sistema operativo.
         */
        fun extractUidFromIntent(intent: Intent): String? {
            val idBytes = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                ?: run {
                    val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                    }
                    tag?.id
                }
            return idBytes?.let { bytesToHex(it) }
        }

        /**
         * Feedback aptico istantaneo per confermare la rilevazione del tag.
         */
        fun vibrateFeedback(context: Context, isSuccess: Boolean) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                } ?: return

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (isSuccess) {
                        // Doppio impulso di successo
                        val timings = longArrayOf(0, 120, 80, 160)
                        val amplitudes = intArrayOf(0, 255, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        // Singolo impulso prolungato di errore
                        vibrator.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isSuccess) 200L else 350L)
                }
            } catch (_: Exception) {}
        }
    }
}
