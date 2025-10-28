package com.kei.mycalendarapp.presentation.calendarBinder

import android.view.View
import com.kei.mycalendarapp.presentation.calendarContainer.DayViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekDayBinder

/**
 * 周视图的日期绑定器
 * 负责创建和绑定周视图中的日期格子视图
 */
class WeekViewBinder: WeekDayBinder<DayViewContainer> {
    /**
     * 创建日期格子的视图容器
     *
     * @param view 日期格子的根视图
     * @return 返回创建的 DayViewContainer 实例
     */
    override fun create(view: View): DayViewContainer = DayViewContainer(view)

    /**
     * 将数据绑定到日期格子的视图容器
     *
     * @param container 日期格子的视图容器
     * @param data 要绑定到视图的日期数据
     */
    override fun bind(
        container: DayViewContainer,
        data: WeekDay
    ) {
        container.textView.text = data.date.dayOfMonth.toString()
    }
}