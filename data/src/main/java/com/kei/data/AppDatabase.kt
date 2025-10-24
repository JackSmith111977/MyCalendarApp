package com.kei.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kei.data.dao.EventDao
import com.kei.data.entity.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}