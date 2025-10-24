package com.kei.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 事件实体类，用于表示日历中的一个事件条目
 * 
 * @property id 事件的唯一标识符，主键且自动生成，默认值为0
 * @property title 事件的标题，不能为空
 * @property description 事件的描述信息，可为空
 * @property startTime 事件的开始时间戳
 * @property endTime 事件的结束时间戳
 * @property reminderTime 事件的提醒时间戳，可为空
 * @property color 事件的颜色标识
 * @property createdAt 事件的创建时间戳，默认为系统当前时间
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val startTime: Long, // 开始时间戳
    val endTime: Long,   // 结束时间戳
    val reminderTime: Long?, // 提醒时间
    val color: Int,      // 日程颜色
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)