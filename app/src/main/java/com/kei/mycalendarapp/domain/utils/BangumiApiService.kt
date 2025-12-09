package com.kei.mycalendarapp.domain.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kei.mycalendarapp.data.local.entity.BangumiItem
import com.kei.mycalendarapp.data.local.entity.BangumiResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.time.LocalDate

class BangumiApiService {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val TAG = "BangumiApiService"

    fun getBangumiCalendar(callback: (List<BangumiResponse>?) -> Unit){
        val url = "https://api.bgm.tv/calendar"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "MyCalendarApp/1.0")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "网络请求失败${e.message}", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body.string()
                    if (response.isSuccessful){
                        val listType = object : TypeToken<List<BangumiResponse>>() {}.type
                        val bangumiList: List<BangumiResponse> = gson.fromJson(body, listType)
                        callback(bangumiList)
                    }else{
                        Log.e(TAG, "获取番剧日历失败${response.code} ${response.message} ${body}")
                        callback(null)
                    }
                } catch (e: Exception){
                    Log.e(TAG, "解析JSON数据失败${e.message}", e)
                    callback(null)
                }
            }
        })
    }

    fun getBangumiForDate(date: LocalDate, callback: (List<BangumiItem>?) -> Unit){
        getBangumiCalendar { bangumiList ->
            if( bangumiList != null){
                val dayOfWeek = date.dayOfWeek.value
                val dayBangumi = bangumiList.find{ it.weekday.id == dayOfWeek}
                callback(dayBangumi?.items)
            } else{
                callback(null)
            }
        }
    }
}