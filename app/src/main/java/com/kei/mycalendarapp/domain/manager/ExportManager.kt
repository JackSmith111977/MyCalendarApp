package com.kei.mycalendarapp.domain.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.kei.mycalendarapp.data.local.CalendarDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class ExportManager(private val context: Context) {

    companion object{
        private const val TAG = "ExportingManager"
    }

    /**
     * 获取指定日期的事件数据并转换为JSON格式
     * @param date 指定日期
     * @param scope 协程作用域
     * @param onSuccess 成功回调，返回JSON格式的字节数组
     * @param onError 失败回调
     */
    fun getEventsDataAsJson(
        date: LocalDate,
        scope: CoroutineScope,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                // 获取数据库实例
                val database = CalendarDatabase.getInstance(context)
                val eventDao = database.eventDao()

                // 计算指定日期开始和结束时间戳
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.atTime(23, 59, 59)

                val startTimestamp = startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()
                val endTimestamp = endOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()

                // 查询该日期的事件
                val events = eventDao.getEventsInRangeOrderByReminder(startTimestamp, endTimestamp)

                // 转换为JSON格式
                val gson = Gson()
                val jsonString = gson.toJson(events)
                val byteArray = jsonString.toByteArray(Charsets.UTF_8)

                onSuccess(byteArray)
            } catch (e: Exception) {
                Log.e(TAG, "获取事件数据失败：${e.message}")
                onError(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 创建指定视图的截图
     * @param view 要截图的视图
     * @return 截图的Bitmap对象
     */
    fun takeScreenshot(recyclerView: RecyclerView): Bitmap {
        // 启用绘制缓存
        recyclerView.measure(
            View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        recyclerView.layout(0, 0, recyclerView.measuredWidth, recyclerView.measuredHeight)

        // 创建足够大的Bitmap来容纳所有内容
        val bitmap = createBitmap(recyclerView.width, recyclerView.measuredHeight)
        val canvas = Canvas(bitmap)
        recyclerView.draw(canvas)
        return bitmap
    }

    /**
     * 将Bitmap转换为PNG格式的字节数组
     * @param bitmap 要转换的Bitmap
     * @return PNG格式的字节数组
     */
    fun convertBitmapToPngByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }



}