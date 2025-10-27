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
}