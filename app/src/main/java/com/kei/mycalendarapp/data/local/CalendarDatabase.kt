package com.kei.mycalendarapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import com.kei.mycalendarapp.data.local.EventDao


/**
 * 日历应用的Room数据库类
 *
 * @property entities 数据库中包含的实体类列表
 * @property version 数据库版本号
 * @property exportSchema 是否导出数据库模式
 */
@Database(
    entities = [CalendarEvent::class],
    version = 2,
    exportSchema = true
)
abstract class CalendarDatabase : RoomDatabase() {
    /**
     * 获取事件数据访问对象
     *
     * @return EventDao 事件数据访问对象实例
     */
    abstract fun eventDao(): EventDao

    companion object{
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        // 数据库迁移
        val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(db: SupportSQLiteDatabase) {
                // 为新字段添加列
                db.execSQL("ALTER TABLE calendar_events ADD COLUMN content TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE calendar_events ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * 获取CalendarDatabase的单例实例
         *
         * @param context 应用上下文
         * @return CalendarDatabase 数据库实例
         */
        fun getInstance(context: Context): CalendarDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}