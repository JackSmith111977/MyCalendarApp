package com.kei.mycalendarapp.presentation.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kei.mycalendarapp.presentation.ui.fragment.AnimeScheduleFragment
import com.kei.mycalendarapp.presentation.ui.fragment.TodayFestivalFragment
import com.kei.mycalendarapp.presentation.ui.fragment.WeekViewFragment
import java.time.LocalDate

/**
 * 模块化ViewPager适配器
 *
 * @param fragmentActivity FragmentActivity
 * @param currentDate 当前日期
 */
class ModulePagerAdapter(fragmentActivity: FragmentActivity, private var currentDate: LocalDate = LocalDate.now()) : FragmentStateAdapter(fragmentActivity){

    /**
     * 获取模块数量
     *
     * @return 模块数量
     */
    override fun getItemCount(): Int = 2

    /**
     * 创建指定位置的Fragment
     *
     * @param position 位置
     * @return 创建的Fragment
     */
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> TodayFestivalFragment.newInstance(currentDate)
            1 -> AnimeScheduleFragment.newInstance(currentDate)
            else -> TodayFestivalFragment.newInstance(currentDate)
        }
    }
}