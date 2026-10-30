package com.tapout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_schedules")
data class BlockScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Giorni attivi (1=Lun ... 7=Dom)
    val isEnabled: Boolean = true,
    val label: String = "Fascia Focus"
)
