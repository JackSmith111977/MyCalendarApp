package com.kei.mycalendarapp.presentation.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.databinding.FragmentDayViewBinding
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.WeekCalendarView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 日视图 Fragment
 * 负责显示和管理日历的日视图，显示特定日期的详细信息
 */
class DayViewFragment: Fragment() {
    private var _binding: FragmentDayViewBinding? = null
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
        _binding = FragmentDayViewBinding.inflate(inflater, container, false)
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

        // TODO: 这里将实现具体的日视图逻辑




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