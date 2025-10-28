package com.kei.mycalendarapp.presentation.calendarBinder

import android.view.View
import com.kei.mycalendarapp.presentation.calendarContainer.DayViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.MonthDayBinder

/**
 * 日视图的日期绑定器
 * 负责创建和绑定日视图中的日期格子视图
 */
class DayViewBinder: MonthDayBinder<DayViewContainer> {
    /**
     * 创建日期格子的视图容器
     *
     * @param view 日期格子的根视图
     * @return 返回创建的 DayViewContainer 实例
     */
    override fun create(view: View) = DayViewContainer(view)

    /**
     * 将数据绑定到日期格子的视图容器
     *
     * @param container 日期格子的视图容器
     * @param data 要绑定到视图的日期数据
     */
    override fun bind(container: DayViewContainer, data: CalendarDay){
        container.textView.text = data.date.dayOfMonth.toString()
    }
}