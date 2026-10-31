package com.tapout.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tapout.data.local.dao.AlarmDao
import com.tapout.data.local.dao.BlockedAppDao
import com.tapout.data.local.dao.BlockScheduleDao
import com.tapout.data.local.dao.NfcTagDao
import com.tapout.data.local.entity.AlarmEntity
import com.tapout.data.local.entity.BlockedAppEntity
import com.tapout.data.local.entity.BlockScheduleEntity
import com.tapout.data.local.entity.NfcTagEntity

@Database(
    entities = [
        AlarmEntity::class,
        BlockedAppEntity::class,
        BlockScheduleEntity::class,
        NfcTagEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun blockScheduleDao(): BlockScheduleDao
    abstract fun nfcTagDao(): NfcTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tapout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
