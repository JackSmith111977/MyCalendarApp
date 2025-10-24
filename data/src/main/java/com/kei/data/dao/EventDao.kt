package com.kei.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.kei.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 事件数据访问对象接口，提供对事件数据的增删改查操作
 */
@Dao
interface EventDao {
    /**
     * 根据指定日期获取当天的所有事件
     * 
     * @param date 用于筛选事件的日期时间戳
     * @return 包含指定日期所有事件的Flow列表
     */
    @Query("SELECT * FROM events WHERE date(startTime/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY startTime ASC")
    fun getEventsForDay(date: Long): Flow<List<EventEntity>>

    /**
     * 插入新的事件
     * 
     * @param event 要插入的事件实体
     * @return 插入事件的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    /**
     * 更新已存在的事件
     * 
     * @param event 要更新的事件实体
     */
    @Update
    suspend fun updateEvent(event: EventEntity)

    /**
     * 删除指定事件
     * 
     * @param event 要删除的事件实体
     */
    @Delete
    suspend fun deleteEvent(event: EventEntity)

    /**
     * 根据事件ID获取特定事件
     * 
     * @param eventId 要查询的事件ID
     * @return 查询到的事件实体，如果不存在则返回null
     */
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): EventEntity?
}