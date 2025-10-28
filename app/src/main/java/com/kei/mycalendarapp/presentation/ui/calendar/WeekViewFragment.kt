package com.kei.mycalendarapp.presentation.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kei.mycalendarapp.databinding.FragmentWeekViewBinding
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kei.mycalendarapp.presentation.calendarBinder.WeekViewBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 周视图 Fragment
 * 负责显示和管理周日历视图，使用 KizitoNwose 的 WeekCalendarView 实现
 */
class WeekViewFragment: Fragment() {
    private var _binding: FragmentWeekViewBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentWeekViewBinding.inflate(inflater, container, false)
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
        setupWeekView()
    }

    /**
     * 设置周视图日历的相关配置
     * 包括日期范围、起始星期、dayBinder 等
     */
    private fun setupWeekView(){
        val currentDate = LocalDate.now()
        
        val weekCalendarView = binding.weekCalendarView
        weekCalendarView.dayBinder = WeekViewBinder()
        
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100) 
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        
        // 配置周日历的显示范围和起始星期
        weekCalendarView.setup(startMonth.atDay(1), endMonth.atEndOfMonth(), firstDayOfWeek)
        weekCalendarView.scrollToWeek(currentDate)
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