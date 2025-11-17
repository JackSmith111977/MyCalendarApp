package com.kei.mycalendarapp.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 事件卡片适配器类
 * 用于在 RecyclerView 中显示日程事件列表，每个事件以卡片形式展示
 *
 * @param events 初始事件列表
 * @param onEditClick 编辑按钮点击回调函数
 * @param onDeleteClick 删除按钮点击回调函数
 * @param onCompleteToggle 完成状态切换回调函数
 */
class EventCardAdapter(
    private var events: List<CalendarEvent>, // 事件列表数据
    private val onEditClick: (CalendarEvent) -> Unit, // 编辑事件回调
    private val onDeleteClick: (CalendarEvent) -> Unit, // 删除事件回调
    private val onCompleteToggle: (CalendarEvent, Boolean) -> Unit // 完成状态切换回调
): RecyclerView.Adapter<EventCardAdapter.EventViewHolder>() {
    /**
     * 创建 ViewHolder 实例
     *
     * @param parent 父 ViewGroup
     * @param viewType 视图类型
     * @return 返回新创建的 EventViewHolder 实例
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventCardAdapter.EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_card, parent, false) // 从布局文件创建视图
        return EventViewHolder(view)
    }

    /**
     * 绑定数据到 ViewHolder
     *
     * @param holder 要绑定数据的 ViewHolder
     * @param position 数据在列表中的位置
     */
    override fun onBindViewHolder(holder: EventCardAdapter.EventViewHolder, position: Int) {
        holder.bind(events[position]) // 绑定指定位置的数据到 ViewHolder
    }

    /**
     * 获取事件列表的大小
     *
     * @return 返回事件列表的大小
     */
    override fun getItemCount(): Int {
        return events.size // 返回事件列表大小
    }

    /**
     * 更新事件列表数据
     *
     * @param newEvents 新的事件列表
     */
    fun updateEvents(newEvents: List<CalendarEvent>){
        events = newEvents
        notifyDataSetChanged() // 通知数据变更，刷新界面
    }

    /**
     * 事件卡片 ViewHolder 类
     * 用于持有和管理事件卡片中的各个 UI 元素
     */
    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val colorIndicator: View = itemView.findViewById(R.id.eventColorIndicator) // 事件颜色指示器
        private val titleText: TextView = itemView.findViewById(R.id.eventTitle) // 事件标题文本
        private val contentText: TextView = itemView.findViewById(R.id.eventContent) // 事件内容文本
        private val timeText: TextView = itemView.findViewById(R.id.eventTime) // 事件时间文本
        private val reminders: TextView = itemView.findViewById(R.id.eventReminder) // 提醒时间文本
        private val editButton: ImageButton = itemView.findViewById(R.id.editEventButton) // 编辑按钮
        private val completeCheckbox: CheckBox = itemView.findViewById(R.id.eventCompletedCheckbox) // 完成状态复选框
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteEventButton) // 删除按钮

        /**
         * 绑定事件数据到视图元素
         *
         * @param event 要绑定的事件数据
         */
        fun bind(event: CalendarEvent){
            // 设置事件颜色
            colorIndicator.setBackgroundColor(event.eventColor) // 根据事件颜色设置指示器背景色

            // 设置事件标题
            titleText.text = event.title // 显示事件标题

            // 设置内容摘要(超过十个字省略后面的字)
            contentText.text = if (event.content.length > 10){
                "${event.content.take(10)}..." // 截取前10个字符并添加省略号
            }else{
                event.content // 内容不足10个字符则完整显示
            }

            // 设置时间
            val startTime = formatTime(event.startTime) // 格式化开始时间
            val endTime = formatTime(event.endTime) // 格式化结束时间
            timeText.text = "$startTime - $endTime" // 显示时间范围

            // 设置提醒时间
            reminders.text = formatReminderTime(event.reminderTime) // 显示提醒时间

            // 设置完成状态
            completeCheckbox.isChecked = event.isCompleted // 根据事件状态设置复选框选中状态

            // 设置点击事件
            editButton.setOnClickListener { onEditClick(event) } // 点击编辑按钮触发回调
            deleteButton.setOnClickListener { onDeleteClick(event) } // 点击删除按钮触发回调
            completeCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCompleteToggle(event, isChecked) // 切换完成状态时触发回调
            }
        }

        /**
         * 格式化时间戳为 HH:mm 格式的字符串
         *
         * @param timeStamp 时间戳
         * @return 格式化后的时间字符串，如果时间戳为0则返回"无"
         */
        private fun formatTime(timeStamp: Long): String{
            return if (timeStamp == 0L){
                "无" // 时间戳为0时显示"无"
            }else{
                val date = Date(timeStamp)
                val format = SimpleDateFormat("HH:mm", Locale.getDefault()) // 使用 HH:mm 格式
                format.format(date) // 格式化时间戳
            }
        }

        /**
         * 格式化提醒时间戳为 HH:mm 格式的字符串
         *
         * @param timeStamp 提醒时间戳
         * @return 格式化后的提醒时间字符串，如果时间戳为0则返回"无"
         */
        private fun formatReminderTime(timeStamp: Long): String{
            return if (timeStamp == 0L){
                "无" // 时间戳为0时显示"无"
            }else{
                val date = Date(timeStamp)
                val format = SimpleDateFormat("HH:mm", Locale.getDefault()) // 使用 HH:mm 格式
                format.format(date) // 格式化时间戳
            }
        }
    }
}