package com.tapout.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tapout.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT packageName FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getBlockedPackageNames(): List<String>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: BlockedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<BlockedAppEntity>)

    @Update
    suspend fun update(app: BlockedAppEntity)

    @Query("UPDATE blocked_apps SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateBlockedStatus(packageName: String, isBlocked: Boolean)

    @Delete
    suspend fun delete(app: BlockedAppEntity)
}
