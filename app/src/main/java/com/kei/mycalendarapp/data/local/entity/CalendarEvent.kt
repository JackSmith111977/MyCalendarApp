package com.kei.mycalendarapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 日历事件实体类，用于表示日历中的一个事件或活动
 *
 * @property id 事件的唯一标识符，自动生成
 * @property title 事件标题
 * @property startTime 事件开始时间（时间戳格式）
 * @property endTime 事件结束时间（时间戳格式）
 * @property eventColor 事件颜色标识
 * @property reminderTime 事件提醒时间（时间戳格式），默认为0表示无提醒
 */
@Entity(tableName = "calendar_events")
data class CalendarEvent (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val startTime: Long,
    val endTime: Long,
    val eventColor: Int,
    val reminderTime: Long = 0,
    val isCompleted: Boolean = false
)