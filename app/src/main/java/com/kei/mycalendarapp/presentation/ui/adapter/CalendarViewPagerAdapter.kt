package com.kei.mycalendarapp.presentation.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kei.mycalendarapp.presentation.ui.fragment.DayViewFragment
import com.kei.mycalendarapp.presentation.ui.fragment.MonthViewFragment
import com.kei.mycalendarapp.presentation.ui.fragment.WeekViewFragment

/**
 * 日历视图的 ViewPager 适配器
 * 管理日、周、月三种视图 Fragment 的创建和显示
 *
 * @param fragmentActivity 关联的 FragmentActivity
 */
class CalendarViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    /**
     * 获取 ViewPager 中 Fragment 的数量
     *
     * @return 返回 Fragment 的数量，固定为 3（日、周、月视图）
     */
    override fun getItemCount(): Int = 3 // 日、周、月三个视图

    /**
     * 根据位置创建对应的 Fragment
     *
     * @param position Fragment 的位置索引
     * @return 返回对应位置的 Fragment 实例
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DayViewFragment()
            1 -> WeekViewFragment()
            2 -> MonthViewFragment()
            else -> MonthViewFragment()
        }
    }
}