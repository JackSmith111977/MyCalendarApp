package com.kei.mycalendarapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kei.mycalendarapp.data.local.entity.CalendarEvent

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: CalendarEvent): Long

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("SELECT * FROM calendar_events WHERE startTime BETWEEN :start And :end")
    suspend fun getEventsInRange(start: Long, end: Long): List<CalendarEvent>

    @Query("SELECT * FROM calendar_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): CalendarEvent?

    @Query("SELECT * FROM calendar_events WHERE startTime BETWEEN :start And :end ORDER BY CASE WHEN reminderTime = 0 THEN 1 ELSE 0 END, reminderTime ASC")
    suspend fun getEventsInRangeOrderByReminder(start: Long, end: Long): List<CalendarEvent>

    @Query("SELECT COUNT(*) FROM calendar_events WHERE title = :title AND startTime >= :startTime AND startTime <= :endTime")
    suspend fun getEventCountByTitleAndDate(title: String, startTime: Long, endTime: Long): Int
}