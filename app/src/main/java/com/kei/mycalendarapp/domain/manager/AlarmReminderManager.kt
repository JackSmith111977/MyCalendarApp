package com.kei.mycalendarapp.domain.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.geometry.Rect
import com.kei.mycalendarapp.data.local.entity.CalendarEvent

/**
 * 闹钟提醒管理器
 *
 * 负责设置和取消事件提醒，根据系统版本和权限支持精确或模糊提醒
 *
 * @param context 上下文对象，用于访问系统服务
 */
class AlarmReminderManager(private val context: Context) {

    /**
     * 为指定事件设置提醒
     *
     * 根据系统版本和权限决定使用精确提醒还是模糊提醒
     * 如果事件提醒时间小于等于0，则不设置提醒
     *
     * @param event 需要设置提醒的日历事件
     */
    fun setReminder(event: CalendarEvent){
        if(event.reminderTime <= 0) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val isExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            alarmManager.canScheduleExactAlarms()
        }else{
            true
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EVENT_ID, event.id)
            putExtra(ReminderReceiver.EVENT_TITLE, event.title)
            putExtra(ReminderReceiver.EVENT_CONTENT, event.content)
            putExtra(ReminderReceiver.IS_EXACT_ALARM, isExactAlarm) // 传递提醒类型信息
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (isExactAlarm){
            // 精确提醒
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, event.reminderTime, pendingIntent)
        }else{
            // 模糊提醒
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                event.reminderTime,
                event.reminderTime + 5 * 60 * 1000,
                pendingIntent
            )
        }
    }

    /**
     * 取消指定事件的提醒
     *
     * @param context 上下文对象
     * @param eventId 需要取消提醒的事件ID
     */
    fun cancelReminder(context: Context, eventId: Long){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}