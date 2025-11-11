package com.kei.mycalendarapp.presentation.ui.calendar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import com.kei.mycalendarapp.databinding.FragmentDayViewBinding
import com.kei.mycalendarapp.domain.manager.EventUpdateManager
import com.kei.mycalendarapp.presentation.ui.common.EventCardAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * 日视图 Fragment
 * 负责显示和管理日历的日视图，显示特定日期的详细信息
 */
class DayViewFragment: Fragment() {
    private var _binding: FragmentDayViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var dayTitle: TextView
    private lateinit var noEventsText: TextView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventAdapter: EventCardAdapter

    /**
     * 事件更新观察者
     * 当收到事件更新通知时，重新加载当天的事件列表
     */
    private val eventUpdateObserver = Observer<Boolean> { _ ->
        // 当收到事件更新时，刷新日程列表
        loadEventsForDate()
    }

    /**
     * 创建 Fragment 的视图层次结构
     *
     * @param inflater 用于 inflate 视图的 LayoutInflater
     * @param container 此 Fragment 的父 ViewGroup
     * @param savedInstanceState 保存的状态信息，如果 Fragment 是重新创建的，则不为 null
     * @return 返回此 Fragment 的根视图
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDayViewBinding.inflate(inflater, container, false)
        val view = _binding!!.root

        dayTitle = binding.dayHeaderText
        noEventsText = binding.noEventText
        eventsRecyclerView = binding.eventRecyclerView

        return view
    }

    /**
     * 设置 RecyclerView 及其适配器
     * 初始化事件列表的显示组件
     */
    private fun setupRecyclerView() {
        eventAdapter = EventCardAdapter(
            events = emptyList(),
            onEditClick = { event ->
                editEvent(event)
            },
            onDeleteClick = { event ->
                deleteEvent(event)
            },
            onCompleteToggle = { event, isCompleted ->
                toggleEventCompletion(event, isCompleted)
            }
        )

        eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventAdapter
        }
    }

    /**
     * 加载指定日期的事件
     * 触发从数据库加载事件数据的操作
     */
    private fun loadEventsForDate(){
        loadEventFromDatabase()
    }

    /**
     * 更新事件列表显示
     * 根据事件列表是否为空来控制空提示文本的显示
     *
     * @param events 要显示的事件列表
     */
    private fun updateEventList(events: List<CalendarEvent>){
        eventAdapter.updateEvents(events)
        noEventsText.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * 从数据库加载事件数据
     * 异步查询数据库获取当天的所有事件，并更新UI显示
     */
    private fun loadEventFromDatabase(){
        lifecycleScope.launch {
            try {
                // 获取数据库实例
                val database = CalendarDatabase.getInstance(requireContext())
                val eventDao = database.eventDao()

                // 计算当天的开始和结束日期
                val currentDate = LocalDate.now()
                val startOfDay = currentDate.atStartOfDay()
                val endOfDay = currentDate.atTime(23, 59, 59)

                val startTimeStamp = startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()
                val endTimeStamp = endOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()

                // 查询数据库并获取事件
                val events = eventDao.getEventsInRange(startTimeStamp, endTimeStamp)

                // 更新UI
                updateEventList(events)
            } catch (e: Exception){
                // 处理异常
                Toast.makeText(context, "加载事件失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 编辑指定事件
     *
     * @param event 需要编辑的事件对象
     */
    private fun editEvent(event: CalendarEvent) {
        // 添加编辑事件逻辑
        // 创建对话框
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_event, null)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)

        // 获取对话框的视图元素
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val eventIdText = dialogView.findViewById<TextView>(R.id.eventIdText)
        val editTextDate = dialogView.findViewById<TextInputEditText>(R.id.editTextDate)
        val editTextTitle = dialogView.findViewById<TextInputEditText>(R.id.editTextTitle)
        val editTextContent = dialogView.findViewById<TextInputEditText>(R.id.editTextContent)
        val editTextReminder = dialogView.findViewById<TextInputEditText>(R.id.editTextReminder)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        // 设置对话框标题为"修改日程"
        dialogTitle.text = "修改日程"

        // 回显事件信息 - 将当前事件的信息显示在对话框中供用户编辑
        eventIdText.text = event.id.toString()
        editTextTitle.setText(event.title)
        editTextContent.setText(event.content)

        // 格式化显示日期 - 将事件开始时间格式化为"yyyy年MM月dd日"格式显示
        val eventDate = Date(event.startTime)
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        editTextDate.setText(dateFormat.format(eventDate))

        // 格式化并显示提醒时间 - 如果事件设置了提醒时间，则格式化为"HH:mm"格式显示
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (event.reminderTime != 0L){
            editTextReminder.setText(timeFormatter.format(Date(event.reminderTime)))
        }

        // 设置日期选择器 - 点击日期输入框时弹出日期选择器
        editTextDate.setOnClickListener {
            showDatePicker(editTextDate)
        }

        // 设置提醒时间选择器 - 点击提醒时间输入框时弹出时间选择器
        editTextReminder.setOnClickListener {
            showTimePicker(editTextReminder)
        }

        // 创建并显示对话框
        val dialog = dialogBuilder.create()

        // 设置对话框按钮点击事件
        // 取消按钮 - 关闭对话框
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 确认按钮 - 处理事件更新逻辑
        buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // 获取数据库实例和事件数据访问对象
                    val database = CalendarDatabase.getInstance(requireContext())
                    val eventDao = database.eventDao()

                    // 获取用户输入的数据
                    val title = editTextTitle.text.toString()
                    val content = editTextContent.text.toString()
                    val reminder = editTextReminder.text.toString()

                    // 解析日期 - 将用户输入的日期字符串转换为LocalDate对象
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                    val localDate = LocalDate.parse(editTextDate.text.toString(), dateFormatter)
                    // 计算事件开始时间和结束时间的时间戳
                    val startTime = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val endTime = localDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    // 解析提醒时间 - 如果设置了提醒时间，则计算提醒时间的时间戳
                    val reminderTime = if (reminder.isNotEmpty()){
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                        val localTime = LocalTime.parse(reminder, timeFormatter)
                        val reminderDateTime = localDate.atTime(localTime)
                        reminderDateTime.atZone((ZoneId.systemDefault())).toInstant().toEpochMilli()
                    }else {
                        0L // 如果未设置提醒时间，则设为0
                    }

                    // 更新事件对象 - 使用新的数据创建更新后的事件对象
                    val updatedEvent = event.copy(
                        title = title,
                        content = content,
                        startTime = startTime,
                        endTime = endTime,
                        reminderTime = reminderTime
                    )

                    // 验证必填字段 - 检查标题是否为空
                    if (title.isEmpty()){
                        editTextTitle.error = "请输入日程标题"
                        return@launch
                    }

                    // 验证必填字段 - 检查日期是否为空
                    if (content.isEmpty()){
                        editTextDate.error = "请选择日程日期"
                        return@launch
                    }

                    // 更新事件到数据库
                    eventDao.updateEvent(updatedEvent)

                    // 通知事件更新 - 通知相关的观察者事件已更新
                    EventUpdateManager.getInstance().notifyEventAdded()

                    // 关闭对话框并显示成功提示
                    dialog.dismiss()
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception){
                    // 捕获异常并显示错误提示
                    Toast.makeText(context, "修改失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showTimePicker(editTextReminder: TextInputEditText) {
        val timePicker = TimePickerDialog(
            requireContext(),
            {_, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editTextReminder.setText(selectedTime)
            },
            9,
            0,
            true
        )
        timePicker.show()
    }

    private fun showDatePicker(editTextDate: TextInputEditText) {
        val today = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            {_, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                editTextDate.setText(selectedDate.format(formatter))
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
        datePicker.show()
    }
    
    /**
     * 删除指定事件
     *
     * @param event 需要删除的事件对象
     */
    private fun deleteEvent(event: CalendarEvent) {
        // 添加删除事件逻辑
        // 创建确认对话框
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("确认删除")
        dialogBuilder.setMessage("确定要删除事件 \"${event.title}\" 吗？ 此操作无法撤销。")

        // 设置确认按钮
        dialogBuilder.setPositiveButton("删除"){_, _ ->
            lifecycleScope.launch {
                try {
                    // 获取数据库实例
                    val database = CalendarDatabase.getInstance(requireContext())
                    val eventDao = database.eventDao()

                    // 从数据库中删除事件
                    eventDao.deleteEvent(event)

                    // 通知事件更新
                    EventUpdateManager.getInstance().notifyEventAdded()

                    // 显示删除成功提示
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception){
                    // 捕获异常并显示错误提示
                    Toast.makeText(context, "删除失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 设置取消按钮
        dialogBuilder.setNegativeButton("取消"){dialog, _ ->
            dialog.dismiss()
        }

        // 显示对话框
        dialogBuilder.create().show()
    }
    
    /**
     * 切换事件完成状态
     *
     * @param event 需要切换状态的事件对象
     * @param isCompleted 事件的完成状态
     */
    private fun toggleEventCompletion(event: CalendarEvent, isCompleted: Boolean) {
        // 添加切换事件完成状态逻辑
        lifecycleScope.launch {
            try {
                val database = CalendarDatabase.getInstance(requireContext())
                val eventDao = database.eventDao()

                val updatedEvent = event.copy(isCompleted = isCompleted)

                eventDao.updateEvent(updatedEvent)

                // 通知事件更新
                EventUpdateManager.getInstance().notifyEventAdded()
                val message = if(isCompleted) "事件已完成" else "事件标记为未完成"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception){
                Toast.makeText(context, "切换失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 当 Fragment 的视图创建完成后调用
     * 在这里进行视图相关的初始化操作
     *
     * @param view 此 Fragment 的根视图
     * @param savedInstanceState 保存的状态信息
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 观察事件更新
        EventUpdateManager.getInstance().eventUpdated.observe(
            viewLifecycleOwner,
            eventUpdateObserver
        )

        
        // 初始化 ViewModel
        val database = CalendarDatabase.getInstance(requireContext())
        val eventDao = database.eventDao()
        
        setupRecyclerView()
        
        // 加载初始数据
        loadEventsForDate()
        setupDayView()
    }

    /**
     * 设置日视图的相关配置
     * 包括设置当前日期标题等
     */
    private fun setupDayView(){
        val currentDate = LocalDate.now()
        // 设置日视图头部显示的日期文本
        binding.dayHeaderText.text = DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(currentDate)
    }

    /**
     * 当 Fragment 的视图被销毁时调用
     * 清理资源，避免内存泄漏
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}