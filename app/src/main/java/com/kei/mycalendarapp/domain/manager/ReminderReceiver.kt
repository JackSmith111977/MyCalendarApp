package com.kei.mycalendarapp.domain.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.kei.mycalendarapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 事件提醒接收器
 *
 * 当有事件提醒触发时，该接收器会处理提醒逻辑，包括显示浮动通知、播放提示音和振动等操作
 */
class ReminderReceiver: BroadcastReceiver() {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * 伴生对象，定义了用于传递数据的常量键名
     */
    companion object{
        /** 事件ID键名 */
        const val EVENT_ID = "event_id"
        /** 事件标题键名 */
        const val EVENT_TITLE = "event_title"
        /** 事件内容键名 */
        const val EVENT_CONTENT = "event_content"
        /** 是否为精确闹钟键名 */
        const val IS_EXACT_ALARM = "is_exact_alarm"
        /** 通知渠道ID */
        private const val CHANNEL_ID = "event_reminder_channel"
        /** 通知渠道名称 */
        private const val CHANNEL_NAME = "Event Reminder"
        /** 通知ID */
        private const val NOTIFICATION_ID = 1001
    }

    /**
     * 接收广播事件并处理提醒逻辑
     *
     * @param context 上下文对象
     * @param intent 包含事件信息的意图对象
     */
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EVENT_ID, -1)
        val eventTitle = intent.getStringExtra(EVENT_TITLE) ?: "Event Reminder"
        val eventContent = intent.getStringExtra(EVENT_CONTENT) ?: ""
        val isExactAlarm = intent.getBooleanExtra(IS_EXACT_ALARM, true) // 默认为精确闹钟

        // 显示 Toast
        val reminderType = if (isExactAlarm) "精确" else "非精确"
        Toast.makeText(context, "事件提醒[$reminderType]：$eventTitle", Toast.LENGTH_LONG).show()

        // 显示浮框提醒
        showFloatingNotification(context, eventTitle, eventId)

        // 振动
        vibrate(context)

        // 播放铃声
        playNotificationSound(context)
    }

    /**
     * 播放通知声音
     *
     * @param context 上下文对象
     */
    private fun playNotificationSound(context: Context) {
        try {
            // 获取系统默认通知铃声
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // 创建MediaPlayer实例并设置数据源
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, notificationUri)
                // 设置音频流类型
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setAudioAttributes(audioAttributes)
                } else {
                    // 为保持向后兼容，API 21以下版本仍使用旧方法
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                }
                // 设置循环播放
                isLooping = true
                // 准备并开始播放
                prepareAsync()

                // 设置准备完成的监听器
                setOnPreparedListener {
                    start()
                }

                // 设置30秒后停止播放
                Handler(Looper.getMainLooper()).postDelayed({
                    stopNotificationSound()
                }, 30000)
            }
        } catch (e: Exception){
            fallbackToRingtone(context)
            e.printStackTrace()
        }

    }

    /**
     * 停止并释放媒体播放器资源
     */
    private fun stopNotificationSound() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying){
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 备用方案：使用Ringtone播放通知声音
     *
     * @param context 上下文对象
     */
    private fun fallbackToRingtone(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 显示浮动通知窗口
     *
     * @param context 上下文对象
     * @param eventTitle 事件标题
     * @param eventId 事件ID
     */
    private fun showFloatingNotification(
        context: Context,
        eventTitle: String,
        eventId: Long
    ) {
        val permissionManager = PermissionManager(context)

        // 检查浮框权限
        if (!permissionManager.checkOverlayPermission()) {
            if (!Settings.canDrawOverlays(context)) {
                // 没有权限，尝试发送标准通知作为替代方案
                // showStandardNotification(context, eventTitle, eventId)
                Toast.makeText(context, "请授予悬浮窗权限以显示浮窗提醒", Toast.LENGTH_LONG).show()
                return
            }
        }

        // 创建浮框视图
        val layoutInflater = LayoutInflater.from(context)
        val floatingView = layoutInflater.inflate(R.layout.notification_floating_layout, null)

        // 设置浮框参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP
        params.x = 0
        params.y = 100

        // 获取 WindowManager
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 添加浮框到窗口
        windowManager.addView(floatingView, params)

        // 设置标题和时间
        val titleTextView = floatingView.findViewById<TextView>(R.id.notification_title)
        val timeTextView = floatingView.findViewById<TextView>(R.id.notification_time)
        val stopButton = floatingView.findViewById<Button>(R.id.stop_button)

        titleTextView.text = eventTitle
        timeTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // 设置停止按钮点击事件
        stopButton.setOnClickListener {
            // 停止振动
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()

            // 停止播放铃声
            stopNotificationSound()

            // 移除浮框
            windowManager.removeView(floatingView)
        }

        // 5分钟后自动移除浮框并停止振动
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                // 停止振动
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.cancel()

                // 移除浮框
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                // 忽略异常
            }
        }, 5 * 60 * 1000) // 5分钟
    }


    /**
     * 设备振动提醒
     *
     * @param context 上下文对象
     */
    private fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        val vibrationPattern = longArrayOf(0, 500, 200, 500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val effect = VibrationEffect.createWaveform(vibrationPattern, 0)
            vibrator.vibrate(effect)
        } else{
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationPattern, 0)
        }
    }

}