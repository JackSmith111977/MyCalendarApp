package com.kei.mycalendarapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kei.mycalendarapp.databinding.FragmentMonthViewBinding
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kei.mycalendarapp.presentation.calendarBinder.MonthDayViewBinder
import java.time.LocalDate
import java.time.YearMonth

/**
 * 月视图 Fragment
 * 负责显示和管理月份日历视图，使用 KizitoNwose 的 CalendarView 实现
 */
class MonthViewFragment : Fragment() {
    private var _binding: FragmentMonthViewBinding? = null
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

        // 步骤3：在Fragment中设置点击回调
        val monthDayViewBinder = MonthDayViewBinder()
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