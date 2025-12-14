package com.kei.mycalendarapp.presentation.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.databinding.FragmentMonthViewBinding
import com.kei.mycalendarapp.domain.manager.EventUpdateManager
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kei.mycalendarapp.presentation.calendarBinder.MonthDayViewBinder
import com.kei.mycalendarapp.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 月视图 Fragment
 * 负责显示和管理月份日历视图，使用 KizitoNwose 的 CalendarView 实现
 */
class MonthViewFragment : Fragment() {
    private var _binding: FragmentMonthViewBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var db: CalendarDatabase

    // 添加年月标题
    private lateinit var monthYearHeader: TextView
    private lateinit var pendingEventsCount: TextView
    private lateinit var completedEventsCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
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
        _binding = FragmentMonthViewBinding.inflate(inflater, container, false)
        return binding.root
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
        db = CalendarDatabase.getInstance(requireContext())
        setupMonthView()
    }

    /**
     * 设置月视图日历的相关配置
     * 包括日期范围、起始星期、dayBinder 等
     */
    private fun setupMonthView() {
        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        // 初始化年月标题
        monthYearHeader = binding.root.findViewById(R.id.monthYearHeader)
        pendingEventsCount = binding.root.findViewById(R.id.pendingEventsCount)
        completedEventsCount = binding.root.findViewById(R.id.completedEventsCount)
        
        updateMonthYearHeader(currentMonth)
        loadEventStatistics(currentMonth)

        // 设置年月标题点击事件
        monthYearHeader.setOnClickListener {
            showMonthYearPickerDialog(currentMonth)
        }

        // 步骤3：在Fragment中设置点击回调
        val monthDayViewBinder = MonthDayViewBinder(db)
        monthDayViewBinder.onDateClickListener = { calendarDay ->
            // 处理点击事件
            Toast.makeText(context, "Clicked: ${calendarDay.date}", Toast.LENGTH_SHORT).show()
            // 更新选中日期的视觉效果
            monthDayViewBinder.selectDate(calendarDay.date)
            // 通知日历重新绘制以更新选中状态
            binding.monthCalendarView.notifyCalendarChanged()
        }

        binding.monthCalendarView.dayBinder = monthDayViewBinder
        // 配置月日历的显示范围和起始星期
        binding.monthCalendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.monthCalendarView.scrollToMonth(currentMonth)

        binding.monthCalendarView.monthScrollListener = { month ->
            updateMonthYearHeader(month.yearMonth)
            loadEventStatistics(month.yearMonth)
        }

        // 在setupMonthView方法末尾添加以下代码
        EventUpdateManager.getInstance().eventUpdated.observe(
            viewLifecycleOwner,
            eventUpdateObserver
        )
    }

    private val eventUpdateObserver = Observer<Boolean> { _ ->
        // 当收到事件更新通知时，刷新月视图
        binding.monthCalendarView.notifyCalendarChanged()
    }

    /**
     * 加载事件统计数据
     */
    private fun loadEventStatistics(yearMonth: YearMonth) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 计算月初和月末的时间戳
                val startDate = yearMonth.atDay(1).atStartOfDay()
                val endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59)

                val events = db.eventDao().getEventsInRange(
                    startDate.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    endDate.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
                )

                // 统计已完成和未完成事件
                val completedEvents = events.filter { it.isCompleted }.size
                val pendingEvents = events.size - completedEvents

                // 切换到主线程更新UI
                CoroutineScope(Dispatchers.Main).launch {
                    pendingEventsCount.text = pendingEvents.toString()
                    completedEventsCount.text = completedEvents.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新年月标题
     */
    private fun updateMonthYearHeader(yearMonth: YearMonth){
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月")
        monthYearHeader.text = yearMonth.format(formatter)
    }

    /**
     * 显示年月选择对话框
     */
    private fun showMonthYearPickerDialog(currentMonth: YearMonth) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)

        // 设置年份范围
        val years = (currentMonth.year - 100..currentMonth.year + 100).toList()
        yearPicker.minValue = currentMonth.year - 100
        yearPicker.maxValue = currentMonth.year + 100
        yearPicker.value = currentMonth.year

        // 设置月份范围
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = currentMonth.monthValue

        AlertDialog.Builder(requireContext())
            .setTitle("选择年月")
            .setView(dialogView)
            .setPositiveButton("确定") { dialog, _ ->
                val selectedYearMonth = YearMonth.of(yearPicker.value, monthPicker.value)
                binding.monthCalendarView.scrollToMonth(selectedYearMonth)
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 当 Fragment 的视图被销毁时调用
     * 清理资源，避免内存泄漏
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getSelectedDate(): LocalDate?{
        val monthViewBinder = binding.monthCalendarView.dayBinder as? MonthDayViewBinder // 将 dayBinder 安全转换为 MonthDayViewBinder
        return  monthViewBinder?.getSelectedDate()
    }
}