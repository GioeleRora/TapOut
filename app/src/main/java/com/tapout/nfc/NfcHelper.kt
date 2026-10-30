package com.tapout.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle

class NfcHelper(private val context: Context) {

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun enableReaderMode(activity: Activity, onTagScanned: (String) -> Unit) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

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
     * Tenta l'attivazione automatica del chip NFC:
     * 1. Tentativo diretto via reflection su enable() interno.
     * 2. Se bloccato da Android OS security policy, apre all'istante il pannello nativo NFC flottante (Android 10+)
     *    in sovrimpressione sopra la sveglia per permettere l'accensione immediata in 1 tap.
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val panelIntent = android.content.Intent(android.provider.Settings.Panel.ACTION_NFC).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(panelIntent)
            } else {
                val settingsIntent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activity.startActivity(settingsIntent)
            }
        } catch (_: Exception) {
            val settingsIntent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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

        fun isTagValid(scannedUid: String, registeredUid: String?, isTestMode: Boolean): Boolean {
            if (isTestMode && scannedUid == "TEST_TAG_SIMULATION") {
                return true
            }
            if (registeredUid.isNullOrBlank()) {
                // Se nessun tag è registrato, qualsiasi tag scansionato disattiva per non bloccare l'utente
                return true
            }
            return scannedUid.equals(registeredUid.trim(), ignoreCase = true)
        }
    }
}
