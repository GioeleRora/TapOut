package com.tapout.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tapout.data.local.entity.NfcTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NfcTagDao {

    @Query("SELECT * FROM nfc_tags ORDER BY id ASC")
    fun getAllTags(): Flow<List<NfcTagEntity>>

    @Query("SELECT * FROM nfc_tags ORDER BY id ASC")
    suspend fun getAllTagsList(): List<NfcTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: NfcTagEntity): Long

    @Delete
    suspend fun deleteTag(tag: NfcTagEntity)

    @Query("DELETE FROM nfc_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("SELECT COUNT(*) FROM nfc_tags")
    suspend fun getTagCount(): Int

    @Query("SELECT * FROM nfc_tags WHERE uid = :uid LIMIT 1")
    suspend fun getTagByUid(uid: String): NfcTagEntity?
}
