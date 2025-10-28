package com.kei.mycalendarapp.presentation.calendarBinder

import android.view.View
import com.kei.mycalendarapp.presentation.calendarContainer.MonthDayViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder

/**
 * 月视图的日期绑定器
 * 负责创建和绑定月视图中的日期格子视图
 */
class MonthDayViewBinder : MonthDayBinder<MonthDayViewContainer> {
    /**
     * 创建日期格子的视图容器
     *
     * @param view 日期格子的根视图
     * @return 返回创建的 MonthDayViewContainer 实例
     */
    override fun create(view: View) = MonthDayViewContainer(view)

    /**
     * 将数据绑定到日期格子的视图容器
     *
     * @param container 日期格子的视图容器
     * @param data 要绑定到视图的日期数据
     */
    override fun bind(container: MonthDayViewContainer, data: CalendarDay) {
        container.textView.text = data.date.dayOfMonth.toString()
        
        // 可以在这里添加更多的样式逻辑，例如选中日期的高亮显示等
        // 根据日期是否属于当前月份设置不同的透明度
        if (data.position == com.kizitonwose.calendar.core.DayPosition.MonthDate) {
            container.textView.alpha = 1f
        } else {
            container.textView.alpha = 0.3f
        }
    }
}