package com.kei.mycalendarapp.presentation.calendarBinder

import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.presentation.calendarContainer.MonthDayViewContainer
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.xhinliang.lunarcalendar.LunarCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.MonthDay

/**
 * 月视图的日期绑定器
 * 负责创建和绑定月视图中的日期格子视图
 */
class MonthDayViewBinder(private val db: CalendarDatabase) : MonthDayBinder<MonthDayViewContainer> {
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

        // 设置农历信息
        val lunarText = getLunarText(data.date)
        container.lunarTextView.text = lunarText
        // 设置农历文本样式
        container.lunarTextView.textSize = 12f
        container.lunarTextView.setTextColor(Color.GRAY)
        container.lunarTextView.alpha = 0.8f // 设置半透明效果

        // 查询并显示事件标记点
        loadAndDisplayEventIndicator(container, data.date)

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
        if (data.date == today && selectedDate != today) {
            // 今天的日期使用与选中日期相同形状的背景高亮显示
            container.textView.setBackgroundResource(R.drawable.modern_today_background)
            container.textView.setTextColor(Color.BLACK)
        }

        // 可以在这里添加更多的样式逻辑，例如选中日期的高亮显示等
        // 根据日期是否属于当前月份设置不同的透明度
        if (data.position == DayPosition.MonthDate) {
            container.textView.alpha = 1f
        } else {
            container.textView.alpha = 0.3f
        }
    }

    /**
     * 加载并显示指定日期的事件标记点
     */
    private fun loadAndDisplayEventIndicator(container: MonthDayViewContainer, date: LocalDate) {
        // 默认隐藏事件标记点
        container.eventIndicator.visibility = GONE

        // 异步查询数据库获取事件数据
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 计算一天的开始和结束时间
                val startTime = date.atStartOfDay()
                val endTime = date.atTime(23, 59, 59)

                val events = db.eventDao().getEventsInRange(
                    startTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    endTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
                )

                // 切换到主线程更新UI
                CoroutineScope(Dispatchers.Main).launch {
                    // 根据事件情况显示对应的标记点
                    when {
                        events.isEmpty() -> {
                            // 无事件，不显示标记点
                            container.eventIndicator.visibility = GONE
                        }
                        events.all { it.isCompleted } -> {
                            // 全部完成，显示绿点
                            container.eventIndicator.setImageResource(R.drawable.event_dot_green)
                            container.eventIndicator.visibility = VISIBLE
                        }
                        events.any { !it.isCompleted } -> {
                            // 有未完成事件，显示红点
                            container.eventIndicator.setImageResource(R.drawable.event_dot_red)
                            container.eventIndicator.visibility = VISIBLE
                        }
                        else -> {
                            // 默认情况，显示灰点
                            container.eventIndicator.setImageResource(R.drawable.event_dot_gray)
                            container.eventIndicator.visibility = VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 出错时隐藏标记点
                CoroutineScope(Dispatchers.Main).launch {
                    container.eventIndicator.visibility = GONE
                }
            }
        }
    }

    private fun getLunarText(date: LocalDate): String? {
        val lunarCalendar = LunarCalendar.obtainCalendar(
            date.year,
            date.monthValue,
            date.dayOfMonth
        )
        return lunarCalendar.lunarDay
    }

    fun getSelectedDate(): LocalDate?{
        return selectedDate
    }
}
