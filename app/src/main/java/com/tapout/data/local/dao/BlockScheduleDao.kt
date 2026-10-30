package com.tapout.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tapout.data.local.entity.BlockScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockScheduleDao {
    @Query("SELECT * FROM block_schedules ORDER BY startHour ASC, startMinute ASC")
    fun getAllSchedules(): Flow<List<BlockScheduleEntity>>

    @Query("SELECT * FROM block_schedules WHERE isEnabled = 1")
    suspend fun getActiveSchedules(): List<BlockScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BlockScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: BlockScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: BlockScheduleEntity)

    @Query("DELETE FROM block_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)
}
