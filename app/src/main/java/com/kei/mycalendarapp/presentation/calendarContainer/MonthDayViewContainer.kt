package com.kei.mycalendarapp.presentation.calendarContainer

import android.view.View
import android.widget.TextView
import com.kei.mycalendarapp.R
import com.kizitonwose.calendar.view.ViewContainer

/**
 * 月视图中单个日期格子的视图容器
 * 用于持有和管理月视图中每个日期格子的 UI 元素
 *
 * @param view 日期格子的根视图
 */
class MonthDayViewContainer(view: View) : ViewContainer(view) {
    /**
     * 日期格子中显示日期数字的 TextView
     */
    val textView: TextView = view.findViewById(com.kei.mycalendarapp.R.id.calendarDayText)
    
    // 添加一个函数来设置点击监听器
    fun setOnClickListener(listener: (View) -> Unit) {
        textView.setOnClickListener(listener)
    }
}