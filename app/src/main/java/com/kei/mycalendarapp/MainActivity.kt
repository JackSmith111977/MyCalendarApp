package com.kei.mycalendarapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import com.kei.mycalendarapp.databinding.ActivityMainBinding
import com.kei.mycalendarapp.domain.manager.AlarmReminderManager
import com.kei.mycalendarapp.domain.manager.EventColorManager
import com.kei.mycalendarapp.domain.manager.EventUpdateManager
import com.kei.mycalendarapp.presentation.ui.fragment.DayViewFragment
import com.kei.mycalendarapp.presentation.ui.fragment.MonthViewFragment
import com.kei.mycalendarapp.presentation.ui.fragment.WeekViewFragment
import com.kei.mycalendarapp.presentation.ui.adapter.CalendarViewPagerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.kei.mycalendarapp.domain.manager.PermissionManager

/**
 * 主活动类，负责初始化应用程序的主要界面
 * 设置 ViewPager2 和 TabLayout 来实现日、周、月视图的切换
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager

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

        // 初始化权限管理器
        permissionManager = PermissionManager(this)

        setupViewPager()
        setupFloatingActionButton()

        // 检查并请求权限
        checkAndRequestPermissions()

        Log.d("权限状态", checkAlarmPermissionStatus())

    }

    private fun setupFloatingActionButton() {
        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun checkAndRequestPermissions(){
        // 检查精确闹钟权限
        if (!permissionManager.checkExactAlarmPermission()) {
            permissionManager.requestExactAlarmPermission(requestAlarmPermissionLauncher)
        }

        // 检查开启悬浮窗权限
        if (!permissionManager.checkOverlayPermission()) {
            permissionManager.requestOverlayPermission(requestOverlayPermissionLauncher)
        }

        // 检查通知权限
        if (permissionManager.checkNotificationPermission() != PermissionManager.PERMISSION_GRANTED) {
            // 可以选择请求通知权限
            permissionManager.requestNotificationPermission(requestNotificationPermissionLauncher)
        }
    }

    // 添加这些ActivityResultLauncher
    private val requestAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 处理闹钟权限请求结果
        if (permissionManager.checkExactAlarmPermission()) {
            Toast.makeText(this, "精确闹钟权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "精确闹钟权限被拒绝", Toast.LENGTH_SHORT).show()
        }
        Log.d("权限请求", "闹钟权限请求结果: ${result.resultCode}")
    }

    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 处理悬浮窗权限请求结果
        if (permissionManager.checkOverlayPermission()) {
            Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "悬浮窗权限被拒绝，请手动开启", Toast.LENGTH_LONG).show()
        }
        Log.d("权限请求", "悬浮窗权限请求结果")
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 处理通知权限请求结果
        when (permissionManager.checkNotificationPermission()) {
            PermissionManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show()
            }
            PermissionManager.PERMISSION_DENIED_PERMANENTLY -> {
                Toast.makeText(this, "通知权限被永久拒绝，请在设置中手动开启", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "通知权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("权限请求", "通知权限请求结果")
    }

    private fun checkAlarmPermissionStatus(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                "精确闹钟权限已获得"
            } else {
                "仅能使用非精确提醒"
            }
        } else {
            "精确闹钟权限已获得（Android 12以下版本）"
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
            // 1. 验证必填字段
            if (title.isEmpty()){
                editTextTitle.error = "请输入日程标题"
                return@setOnClickListener
            }

            if (date.isEmpty()){
                editTextDate.error = "请选择日程日期"
                return@setOnClickListener
            }

            // 2. 保存事件到数据库
            saveEventToDatabase(title, date, content, reminder)

            // 3. 关闭对话框
            dialog.dismiss()
        }

        // 显示对话框
        dialog.show()
    }

    private fun saveEventToDatabase(
        title: String,
        date: String,
        content: String,
        reminder: String
    ){
        // 在后台线程中执行数据库操作
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 解析日期
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
                val localDate = LocalDate.parse(date, dateFormatter)

                // 开始时间和结束时间
                val startTime = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = localDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // 解析提醒时间
                val reminderTime = if (reminder.isNotEmpty()){
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val localTime = LocalTime.parse(reminder, timeFormatter)
                    val reminderDateTime = localDate.atTime(localTime)
                    reminderDateTime.atZone((ZoneId.systemDefault())).toInstant().toEpochMilli()
                } else {
                    0L // 无提醒
                }

                // 创建颜色管理器实例
                val colorManager = EventColorManager()

                // 获取当天事件已有的颜色
                val existingColors = getExistingEventColorsForDate(getSelectedDate())

                // 为新事件选择颜色
                val eventColor = colorManager.getColorForEvent(existingColors)

                // 创建CalendarEvent对象
                val event = CalendarEvent(
                    title = title,
                    content = content,
                    startTime = startTime,
                    endTime = endTime,
                    eventColor = eventColor,
                    reminderTime = reminderTime,
                    isCompleted = false
                )

                // 获取数据库实例并插入事件
                val database = CalendarDatabase.getInstance(this@MainActivity)
                val eventId = database.eventDao().insertEvent(event)

                // 设置提醒
                val alarmReminderManager = AlarmReminderManager(applicationContext)
                if (reminderTime > 0){
                    alarmReminderManager.setReminder(event)
                }

                // 在主线程中显示成功消息
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "日程已添加，ID：$eventId", Toast.LENGTH_SHORT).show()

                    // 通知日程列表刷新
                    EventUpdateManager.getInstance().notifyEventAdded()

                }

            } catch (e: Exception){
                // 在主线程中显示错误信息
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "添加日程失败：${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "添加日程失败：${e.message}")
                }
            }
        }
    }

    private suspend fun CoroutineScope.getExistingEventColorsForDate(selectedDate: LocalDate?): List<Int> {
        // 1. 检查日期是否为空
        if (selectedDate == null){
            return emptyList()
        }

        // 2. 计算时间范围
        val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = selectedDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 3. 查询数据库获取指定日期的所有事件
        val database = CalendarDatabase.getInstance(this@MainActivity)
        val events = async {
            database.eventDao().getEventsInRange(startOfDay, endOfDay)
        }.await()

        // 4. 提取事件颜色
        return events.map { it.eventColor }
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