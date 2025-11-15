package com.kei.mycalendarapp.domain.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.LocalDate
import java.time.ZoneOffset

class ImportManager(private val context: Context) {

    private val eventColorManager = EventColorManager()

    companion object{
        private const val TAG = "ImportManager"
    }

    fun importEventsFromJson(
        uri: Uri,
        targetDate: LocalDate,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        scope.launch {
            try {
                // 读取文件内容
                val inputStream = withContext(Dispatchers.IO){
                    context.contentResolver.openInputStream(uri)
                }

                if(inputStream == null){
                    onError("无法打开文件")
                    return@launch
                }

                // 解析Json数据
                val events = parseJsonEvents(inputStream)

                // 调整事件日期并保存到数据库
                saveEventToDatabase(events, targetDate)

                onSuccess()
            }catch (e: Exception){
                Log.e(TAG, "导入事件失败: ${e.message}")
                onError("导入事件失败: ${e.message}")
            }
        }
    }

    fun importEventsToOriginalDates(
        uri: Uri,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        scope.launch {
            try{
                // 读取文件内容
                val inputStream = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)
                }

                if (inputStream == null) {
                    onError("无法打开文件")
                    return@launch
                }

                // 解析Json数据
                val events = parseJsonEvents(inputStream)

                // 保存事件到其原始日期
                saveEventToOriginalDates(events)

                onSuccess()
            }catch (e: Exception){
                Log.e(TAG, "导入事件失败: ${e.message}")
                onError("导入事件失败: ${e.message}")
            }
        }
    }

    private suspend fun CoroutineScope.saveEventToOriginalDates(events: List<CalendarEvent>) {
        withContext(Dispatchers.IO){
            val database = CalendarDatabase.getInstance(context)
            val eventDao = database.eventDao()

            events.forEach { event ->
                // 检查事件是否已经存在
                val existingEvents = eventDao.getEventsInRange(event.startTime, event.endTime)

                // 检查是否已经存在相同标题和时间的事件
                val isDuplicate = existingEvents.any { existingEvent ->
                    existingEvent.title == event.title &&
                            existingEvent.startTime == event.startTime &&
                            existingEvent.endTime == event.endTime
                }

                // 不存在重复事件，则插入新事件
                if(!isDuplicate){
                    // 创建新的事件对象，保持原始日期和时间
                    val newEvent = event.copy(
                        id = 0,
                        title = event.title,
                        content = event.content,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        eventColor = eventColorManager.getRandomColor(),
                        reminderTime = event.reminderTime,
                        isCompleted = event.isCompleted
                    )

                    // 插入到数据库
                    eventDao.insertEvent(newEvent)
                }
            }


        }
    }

    private suspend fun CoroutineScope.saveEventToDatabase(
        events: List<CalendarEvent>,
        targetDate: LocalDate
    ) {
        withContext(Dispatchers.IO){
            val database = CalendarDatabase.getInstance(context)
            val eventDao = database.eventDao()

            // 计算目标日期的时间戳范围
            val targetStartOfDay = targetDate.atStartOfDay()
            val targetEndOfDay = targetDate.atTime(23, 59, 59)

            val targetStartTimestamp = targetStartOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()
            val targetEndTimestamp = targetEndOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()

            // 保存调整后的事件
            events.forEach { event ->
                // 保持原始事件的时长
                val duration = event.endTime - event.startTime

                // 创建新的事件对象，日期调整为目标日期，但保持原始的时间和时长
                val newEvent = event.copy(
                    id = 0, // 设置为0，Room会自动生成新的id
                    startTime = targetStartTimestamp,
                    endTime = targetStartTimestamp + duration,
                    title = event.title,
                    content = event.content,
                    eventColor = eventColorManager.getRandomColor(),
                    reminderTime = if (event.reminderTime > 0){
                        targetStartTimestamp + (event.reminderTime - event.startTime)
                    }else {
                        0L
                    },
                    isCompleted = event.isCompleted
                )

                // 插入到数据库
                eventDao.insertEvent(newEvent)
            }
        }
    }

    private suspend fun parseJsonEvents(inputStream: InputStream): List<CalendarEvent>{
        return withContext(Dispatchers.IO){
            try{
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val listType = object : TypeToken<List<CalendarEvent>>() {}.type
                gson.fromJson(jsonString, listType) as List<CalendarEvent>
            } finally {
                inputStream.close()
            }
        }
    }
}