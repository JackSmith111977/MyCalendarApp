package com.kei.mycalendarapp.domain.manager

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * 权限管理器
 * 负责检查、请求和管理应用程序所需的各种权限
 */
class PermissionManager(private val context: Context) {

    companion object{
        private const val TAG = "PermissionManager"
        const val PERMISSION_GRANTED = 0 // 权限已授予
        const val PERMISSION_REQUEST_NEEDED = 1 // 需要请求权限
        const val PERMISSION_DENIED_PERMANENTLY = 2 // 权限被永久拒绝
    }

    /**
     * 检查通知权限状态
     * @return 权限状态 (PERMISSION_GRANTED, PERMISSION_REQUEST_NEEDED, PERMISSION_DENIED_PERMANENTLY)
     */
    fun checkNotificationPermission(): Int{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    PERMISSION_GRANTED
                }else -> {
                    PERMISSION_REQUEST_NEEDED
                }
            }
        } else {
            PERMISSION_GRANTED
        }
    }

    /**
     * 检查浮窗权限状态
     * @return true 如果已授予权限，否则 false
     */
    fun checkOverlayPermission(): Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Settings.canDrawOverlays(context)
        }else{
            true
        }
    }

    /**
     * 检查精确闹钟权限状态
     * @return true 如果已授予权限，否则 false
     */
    fun checkExactAlarmPermission(): Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        }else{
            true
        }
    }

    /**
     * 请求通知权限
     * @param launcher 用于启动权限请求的 ActivityResultLauncher
     */
    fun requestNotificationPermission(launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            launcher.launch(intent)
        }
    }

    /**
     * 请求浮窗权限
     * @param launcher 用于启动权限请求的 ActivityResultLauncher
     */
    fun requestOverlayPermission(launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            launcher.launch(intent)
        }
    }

    /**
     * 请求精确闹钟权限
     * @param launcher 用于启动权限请求的 ActivityResultLauncher
     */
    fun requestExactAlarmPermission(launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            }
            launcher.launch(intent)
        }
    }

    /**
     * 检查所有必需权限
     * @return true 如果所有权限都已授予，否则 false
     */
    fun checkAllRequiredPermissions(): Boolean {
        return checkOverlayPermission() &&
                checkExactAlarmPermission() &&
                checkNotificationPermission() == PERMISSION_GRANTED
    }

    /**
     * 获取缺失权限的描述信息
     * @return 包含缺失权限信息的列表
     */
    fun getMissingPermissionsDescription(): List<String> {
        val missingPermissions = mutableListOf<String>()

        if (!checkOverlayPermission()) {
            missingPermissions.add("浮窗权限")
        }

        if (!checkExactAlarmPermission()) {
            missingPermissions.add("精确闹钟权限")
        }

        if (checkNotificationPermission() != PERMISSION_GRANTED) {
            missingPermissions.add("通知权限")
        }

        return missingPermissions
    }
}