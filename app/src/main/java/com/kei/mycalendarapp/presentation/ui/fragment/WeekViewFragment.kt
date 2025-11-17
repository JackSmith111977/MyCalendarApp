package com.kei.mycalendarapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.databinding.FragmentWeekViewBinding
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kei.mycalendarapp.presentation.calendarBinder.WeekViewBinder
import com.kei.mycalendarapp.presentation.ui.adapter.ModulePagerAdapter
import com.kei.mycalendarapp.presentation.viewmodel.SharedViewModel
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * 周视图 Fragment
 * 负责显示和管理周日历视图，使用 KizitoNwose 的 WeekCalendarView 实现
 */
class WeekViewFragment: Fragment() {
    private var _binding: FragmentWeekViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleAdapter: ModulePagerAdapter

    // 添加年月标题变量声明
    private lateinit var monthYearHeader: TextView
    private lateinit var moduleTabLayout: TabLayout
    private lateinit var moduleViewPager: ViewPager2
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

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
        _binding = FragmentWeekViewBinding.inflate(inflater, container, false)
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
        setupWeekView()
        setupModuleSection()
    }

    private fun setupModuleSection() {
        moduleTabLayout = binding.root.findViewById(R.id.moduleTabLayout)
        moduleViewPager = binding.root.findViewById(R.id.moduleViewPager)

        // 设置viewPager
        moduleAdapter = ModulePagerAdapter(requireActivity())
        moduleViewPager.adapter = moduleAdapter

        // 关联 TabLayout 和 ViewPager
        TabLayoutMediator(moduleTabLayout, moduleViewPager){ tab, position ->
            when(position){
                0 -> {
                    tab.text = "节日"
                }
            }
        }.attach()
    }

    /**
     * 设置周视图日历的相关配置
     * 包括日期范围、起始星期、dayBinder 等
     */
    private fun setupWeekView(){
        val currentDate = LocalDate.now()

        // 初始化年月标题
        monthYearHeader = binding.root.findViewById<TextView>(R.id.monthYearHeader)
        
        // 步骤3：在Fragment中设置点击回调
        val weekViewBinder = WeekViewBinder()
        weekViewBinder.onDateClickListener = { weekDay ->
            // 处理点击事件
            Toast.makeText(context, "Clicked: ${weekDay.date}", Toast.LENGTH_SHORT).show()
            // 更新选中日期的视觉效果
            weekViewBinder.selectDate(weekDay.date)
            // 通知日历重新绘制以更新选中状态
            binding.weekCalendarView.notifyCalendarChanged()

            // 通过ViewModel更新选中的日期
            sharedViewModel.setSelectedDate(weekDay.date)
            Log.d("WeekViewFragment", "Clicked: ${weekDay.date}")
        }
        
        val weekCalendarView = binding.weekCalendarView
        weekCalendarView.dayBinder = weekViewBinder

        // 添加滚动监听器以同步星期标题
        weekCalendarView.weekScrollListener = { weekStart ->
            // 更新星期标题以匹配当前周
            updateDayOfWeekTitles(weekStart.days.first().date)
            // 更新年月标题
            updateMonthYearHeader(weekStart.days.first().date)
        }

        // 获取标题容器并设置星期标题
        setDayOfWeekTitles()
        
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100) 
        val firstDayOfWeek = firstDayOfWeekFromLocale()
        
        // 配置周日历的显示范围和起始星期
        weekCalendarView.setup(startMonth.atDay(1), endMonth.atEndOfMonth(), firstDayOfWeek)
        weekCalendarView.scrollToWeek(currentDate)

        // 初始化年月标题
        updateMonthYearHeader(currentDate)
    }

    /**
     * 更新年月标题
     *
     * @param date 当前周的起始日期
     */
    private fun updateMonthYearHeader(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月")
        val monthYearText = date.format(formatter)

        // 如果文本没变化，不更新
        if (monthYearHeader.text.toString() == monthYearText){
            return
        }

        monthYearHeader.text = monthYearText
    }

    /**
     * 更新星期标题以匹配指定日期所在的周
     *
     * @param date 指定日期，用于计算该日期所在周的起始日期
     */
    private fun updateDayOfWeekTitles(date: LocalDate) {
        // 获取星期标题容器
        val titlesContainer = binding.root.findViewById<ViewGroup>(R.id.titlesContainer)

        // 计算指定日期所在周的起始日期
        // 获取本地化的星期起始日（例如在中国是星期一，在美国是星期日）
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        // 计算逻辑：
        // 1. date.dayOfWeek.ordinal：获取指定日期是星期几（星期一为0，星期日为6）
        // 2. date.minusDays(date.dayOfWeek.ordinal.toLong())：从指定日期减去对应的天数，得到该周的星期一
        // 3. plusDays(firstDayOfWeek.ordinal.toLong())：加上本地化星期起始日的偏移量
        // 4. minusDays(if (firstDayOfWeek.ordinal <= date.dayOfWeek.ordinal) 0 else 7)：
        //    如果本地化星期起始日在指定日期的星期几之前或当天，则不需要调整
        //    否则需要减去7天以得到正确的周起始日
        val weekStart = date.minusDays(date.dayOfWeek.ordinal.toLong())
            .plusDays(firstDayOfWeek.ordinal.toLong())
            .minusDays(if (firstDayOfWeek.ordinal <= date.dayOfWeek.ordinal) 0 else 7)

        // 遍历星期标题容器中的所有子视图并更新文本
        titlesContainer.children
            .map { it as TextView }
            // 为每个星期标题设置对应的星期名称
            .forEachIndexed { index, textView ->
                // 计算当前星期标题对应的日期
                // weekStart是周起始日，加上index天数得到该星期第index+1天的日期
                val currentDay = weekStart.plusDays(index.toLong())

                // 获取本地化的星期名称（SHORT格式，如"周一"、"Mon"等）
                val title = currentDay.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                // 设置星期标题文本
                textView.text = title
            }
    }


    /**
     * 设置星期标题
     */
    private fun setDayOfWeekTitles() {
        // 获取标题容器
        val titlesContainer = binding.root.findViewById<ViewGroup>(R.id.titlesContainer)
        
        // 遍历每个标题容器并设置文本
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek()[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
                textView.gravity = Gravity.CENTER
            }
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
        // 获取 WeekViewBinder 实例并返回其选中的日期
        val weekViewBinder = binding.weekCalendarView.dayBinder as? WeekViewBinder // 将 dayBinder 安全转换为WeekViewBinder
        return weekViewBinder?.getSelectedDate()
    }
}