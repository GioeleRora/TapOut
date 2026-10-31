package com.tapout.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object AudioPickerHelper {

    private var previewPlayer: MediaPlayer? = null

    fun getDisplayName(context: Context, uriString: String): String {
        if (uriString.isBlank()) {
            return "Suoneria Predefinita di Sistema"
        }
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" || uriString.startsWith("/")) {
                val file = if (uriString.startsWith("/")) File(uriString) else File(uri.path ?: "")
                val name = file.name
                if (name.isNotBlank()) {
                    // Rimuove prefissi interni come "alarm_1234567890_" se presenti
                    val clean = name.replaceFirst(Regex("^alarm_\\d+_"), "")
                    if (clean.isNotBlank()) clean else name
                } else {
                    "Canzone Personalizzata"
                }
            } else if (uri.scheme == "content") {
                var name: String? = null
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex)
                        }
                    }
                }
                name ?: uri.lastPathSegment ?: "Canzone Personalizzata"
            } else {
                File(uriString).name.ifBlank { "Canzone Personalizzata" }
            }
        } catch (_: Exception) {
            "Canzone Personalizzata"
        }
    }

    /**
     * Copia il brano selezionato nella memoria interna protetta dell'app.
     * In questo modo la suoneria è garantita al 100%:
     * - Sopravvive al riavvio del telefono
     * - Non scade mai il permesso URI di Android Scoped Storage
     * - Continua a suonare anche se l'utente sposta o elimina il file originale dalla cartella Download/Musica
     */
    fun saveAudioToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "custom_alarms").apply { mkdirs() }
            val originalName = getDisplayName(context, sourceUri.toString())
                .replace(" ", "_")
                .replace(Regex("[^a-zA-Z0-9._-]"), "")
            val cleanName = if (originalName.contains(".")) originalName else "$originalName.mp3"
            val destFile = File(dir, "alarm_${System.currentTimeMillis()}_$cleanName")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun playPreview(context: Context, uriString: String, onCompletion: () -> Unit = {}) {
        stopPreview()
        if (uriString.isBlank()) return
        try {
            val uri = if (uriString.startsWith("/")) {
                Uri.fromFile(File(uriString))
            } else {
                Uri.parse(uriString)
            }
            previewPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setOnCompletionListener {
                    onCompletion()
                }
                prepare()
                start()
            }
        } catch (_: Exception) {
            stopPreview()
            onCompletion()
        }
    }

    fun stopPreview() {
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
        } catch (_: Exception) {}
        previewPlayer = null
    }

    fun isPreviewPlaying(): Boolean {
        return try {
            previewPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }
}
