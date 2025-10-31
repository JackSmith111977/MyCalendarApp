package com.kei.mycalendarapp.presentation.calendarBinder

import android.graphics.Color
import android.view.View
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.presentation.calendarContainer.MonthDayViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.MonthDay

/**
 * 月视图的日期绑定器
 * 负责创建和绑定月视图中的日期格子视图
 */
class MonthDayViewBinder : MonthDayBinder<MonthDayViewContainer> {
    var onDateClickListener: ((CalendarDay) -> Unit)? = null
    // 步骤4：实现日期选择状态
    private var selectedDate: LocalDate? = null

    fun selectDate(date: LocalDate){
        selectedDate = date
    }

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
        // 步骤2：在Binder中设置点击监听器
        container.textView.text = data.date.dayOfMonth.toString()

        // 设置点击监听器
        container.setOnClickListener {
            selectedDate = data.date
            onDateClickListener?.invoke(data)
            // 重新绘制日历更新选中的状态
        }

        // 获取今天的日期
        val today = LocalDate.now()

        // 更新选中的视觉效果
        if(selectedDate == data.date){
            container.textView.setBackgroundResource(R.drawable.modern_selected_date_ripple)
            // 选中日期使用白色文字以便在洋红色背景上清晰可见
            container.textView.setTextColor(Color.WHITE)
        } else if (data.date == today) {
            // 今天的日期使用与选中日期相同形状的背景高亮显示
            container.textView.setBackgroundResource(R.drawable.modern_today_background)
            container.textView.setTextColor(Color.BLACK)
        } else {
            container.textView.background = null
            // 非选中日期根据是否属于当前月份设置不同的文字颜色
            if (data.position == DayPosition.MonthDate) {
                // 当前月份的日期使用深色文字
                container.textView.setTextColor(Color.BLACK)
            } else {
                // 非当前月份的日期使用浅色文字
                container.textView.setTextColor(Color.GRAY)
            }
        }

        // 可以在这里添加更多的样式逻辑，例如选中日期的高亮显示等
        // 根据日期是否属于当前月份设置不同的透明度
        if (data.position == DayPosition.MonthDate) {
            container.textView.alpha = 1f
        } else {
            container.textView.alpha = 0.3f
        }
    }
}