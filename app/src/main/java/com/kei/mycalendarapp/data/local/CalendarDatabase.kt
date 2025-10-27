package com.kei.mycalendarapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kei.mycalendarapp.data.local.entity.CalendarEvent


/**
 * 日历应用的Room数据库类
 *
 * @property entities 数据库中包含的实体类列表
 * @property version 数据库版本号
 * @property exportSchema 是否导出数据库模式
 */
@Database(
    entities = [CalendarEvent::class],
    version = 1,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}