package com.tapout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Sveglia",
    val daysOfWeek: String = "", // es. "1,2,3,4,5" per Lun-Ven o vuoto per una volta
    val isEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val soundUri: String = "", // vuoto per suoneria di sistema
    val requireNfc: Boolean = true,
    val requireDance: Boolean = true, // Se true, consente o richiede la Dance Mission da 30s
    val requireSmileKiss: Boolean = false // Nuova modalità: 5 Sorrisi + 5 Baci
)
