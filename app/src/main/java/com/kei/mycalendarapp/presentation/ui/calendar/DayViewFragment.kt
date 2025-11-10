package com.kei.mycalendarapp.presentation.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import com.kei.mycalendarapp.databinding.FragmentDayViewBinding
import com.kei.mycalendarapp.domain.manager.EventUpdateManager
import com.kei.mycalendarapp.presentation.ui.common.EventCardAdapter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
    }
    
    /**
     * 删除指定事件
     *
     * @param event 需要删除的事件对象
     */
    private fun deleteEvent(event: CalendarEvent) {
        // 添加删除事件逻辑
    }
    
    /**
     * 切换事件完成状态
     *
     * @param event 需要切换状态的事件对象
     * @param isCompleted 事件的完成状态
     */
    private fun toggleEventCompletion(event: CalendarEvent, isCompleted: Boolean) {
        // 添加切换事件完成状态逻辑
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