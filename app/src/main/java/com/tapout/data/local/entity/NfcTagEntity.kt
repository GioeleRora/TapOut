package com.tapout.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc_tags")
data class NfcTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uid: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
