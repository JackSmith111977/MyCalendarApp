package com.kei.data.repository

import com.kei.data.dao.EventDao
import com.kei.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class EventRepository @Inject constructor( // 通过@Inject注解，使用constructor构造函数将EventDao实例注入到EventRepository中
    private val eventDao: EventDao
){
    fun getEventsForDay(date: Long): Flow<List<EventEntity>> {
        return eventDao.getEventsForDay(date)
    }

    suspend fun insertEvent(event: EventEntity): Long{ // suspend修饰符，表示该函数是挂起函数，可以在协程中调用
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity){
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity){
        eventDao.deleteEvent(event)
    }

    suspend fun getEventById(eventId: Long): EventEntity?{
        return eventDao.getEventById(eventId)
    }
}