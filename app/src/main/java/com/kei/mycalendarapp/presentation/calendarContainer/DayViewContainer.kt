package com.kei.mycalendarapp.presentation.calendarContainer

import android.view.View
import android.widget.TextView
import com.kei.mycalendarapp.R
import com.kizitonwose.calendar.view.ViewContainer

/**
 * 日视图或周视图中单个日期格子的视图容器
 * 用于持有和管理日/周视图中每个日期格子的 UI 元素
 *
 * @param view 日期格子的根视图
 */
class DayViewContainer(view: View) : ViewContainer(view) {
    /**
     * 日期格子中显示日期数字的 TextView
     */
    val textView: TextView = view.findViewById<TextView>(R.id.calendarDayText)
    
    // 添加一个函数来设置点击监听器
    fun setOnClickListener(listener: (View) -> Unit) {
        textView.setOnClickListener(listener)
    }

    // With ViewBinding
    // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

}