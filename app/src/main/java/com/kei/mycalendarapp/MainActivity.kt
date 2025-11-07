package com.kei.mycalendarapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.kei.mycalendarapp.databinding.ActivityMainBinding
import com.kei.mycalendarapp.presentation.ui.calendar.DayViewFragment
import com.kei.mycalendarapp.presentation.ui.calendar.MonthViewFragment
import com.kei.mycalendarapp.presentation.ui.calendar.WeekViewFragment
import com.kei.mycalendarapp.presentation.ui.common.CalendarViewPagerAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        setupFloatingActionButton()
    }

    private fun setupFloatingActionButton() {
        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun showAddEventDialog() {

        // 1. 创建对话框
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_event, null)
        builder.setView(dialogView)

        val dialog = builder.create()

        // 2. 查找对话框中的UI组件
        val editTextDate = dialogView.findViewById<TextInputEditText>(R.id.editTextDate)
        val editTextTitle = dialogView.findViewById<TextInputEditText>(R.id.editTextTitle)
        val editTextContent = dialogView.findViewById<TextInputEditText>(R.id.editTextContent)
        val editTextReminder = dialogView.findViewById<TextInputEditText>(R.id.editTextReminder)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        // 3. 设置默认日期为今天，若有选择的日期，则使用选择的日期
        val selectedDate = getSelectedDate()
        val defaultDate = selectedDate ?: LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        editTextDate.setText(defaultDate.format(formatter))

        // 设置日期选择器
        editTextDate.setOnClickListener {
            showDatePicker(editTextDate)
        }

        // 设置提醒时间选择器
        editTextReminder.setOnClickListener {
            showTimePicker(editTextReminder)
        }

        // 设置取消按钮
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 设置确认按钮
        buttonConfirm.setOnClickListener {
            val title = editTextTitle.text.toString()
            val content = editTextContent.text.toString()
            val date = editTextDate.text.toString()
            val reminder = editTextReminder.text.toString()

            // 处理添加事件逻辑
            Toast.makeText(this, "日程已添加", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // 显示对话框
        dialog.show()
    }

    private fun showTimePicker(editTextReminder: TextInputEditText) {
        val timePicker = TimePickerDialog(
            this,
            {_, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editTextReminder.setText(selectedTime)
            },
            9,
            0,
            true
        )
        timePicker.show()
    }

    private fun showDatePicker(editTextDate: TextInputEditText) {
        val today = LocalDate.now()
        val datePicker = DatePickerDialog(
            this,
            {_, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                editTextDate.setText(selectedDate.format(formatter))
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
        datePicker.show()
    }

    private fun getSelectedDate(): LocalDate?{
        val viewPager = binding.viewPager
        val currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager.currentItem)

        return when(currentFragment){
            is DayViewFragment -> {
                // 从日视图中获取选中日期
                null
            }
            is WeekViewFragment -> {
                // 从周视图中获取选中日期
                currentFragment.getSelectedDate()
            }
            is MonthViewFragment -> {
                // 从月视图中获取选中日期
                currentFragment.getSelectedDate()
            }
            else -> null
        }
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