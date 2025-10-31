package com.kei.mycalendarapp.presentation.calendarBinder

import android.graphics.Color
import android.view.View
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.presentation.calendarContainer.DayViewContainer
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate

/**
 * 周视图的日期绑定器
 * 负责创建和绑定周视图中的日期格子视图
 */
class WeekViewBinder: WeekDayBinder<DayViewContainer> {
    var onDateClickListener: ((WeekDay) -> Unit)? = null
    private var selectedDate: LocalDate? = null

    fun selectDate(date: LocalDate) {
        selectedDate = date
    }

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

        // 设置点击监听器
        container.setOnClickListener {
            selectedDate = data.date
            onDateClickListener?.invoke(data)
        }

        // 获取今天的日期
        val today = LocalDate.now()
        
        // 更新选中的视觉效果
        if (selectedDate == data.date) {
            container.textView.setBackgroundResource(R.drawable.modern_selected_date_ripple)
            // 选中日期使用白色文字以便在洋红色背景上清晰可见
            container.textView.setTextColor(Color.WHITE)
        } else if (data.date == today) {
            // 今天的日期使用与选中日期相同形状的背景高亮显示
            container.textView.setBackgroundResource(R.drawable.modern_today_background)
            container.textView.setTextColor(Color.BLACK)
        } else {
            container.textView.background = null
            // 非选中日期使用黑色文字
            container.textView.setTextColor(Color.BLACK)
        }
    }
}