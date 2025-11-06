package com.kei.mycalendarapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kei.mycalendarapp.databinding.ActivityMainBinding
import com.kei.mycalendarapp.presentation.ui.common.CalendarViewPagerAdapter

/**
 * 主活动类，负责初始化应用程序的主要界面
 * 设置 ViewPager2 和 TabLayout 来实现日、周、月视图的切换
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    /**
     * Activity 创建时调用的方法
     * 初始化界面并设置边缘到边缘显示效果
     *
     * @param savedInstanceState 保存的实例状态，用于恢复之前的状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
    }

    /**
     * 设置 ViewPager 和 TabLayout 的关联
     * 配置适配器和标签文本
     */
    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val tabLayout = binding.viewTabLayout

        val adapter = CalendarViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false

        // 将 TabLayout 与 ViewPager 关联，并设置每个标签的文本
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "日"
                1 -> "周"
                2 -> "月"
                else -> ""
            }
        }.attach()
    }
}