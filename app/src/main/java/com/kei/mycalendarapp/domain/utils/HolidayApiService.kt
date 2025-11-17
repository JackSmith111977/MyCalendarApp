package com.kei.mycalendarapp.domain.utils

import android.util.Log
import com.google.gson.Gson
import com.kei.mycalendarapp.data.local.entity.HolidayResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.net.CookieManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HolidayApiService {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getHolidayInfo(date: LocalDate, callback: (HolidayResponse?) -> Unit){
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateString = date.format(formatter)
        val url = "https://timor.tech/api/holiday/info/$dateString"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .addHeader("Connection", "keep-alive")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
                Log.e("HolidayApiService", "网络请求失败${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful){
                        Log.d("HolidayApiService", "获取的节假日信息${response.body.string()}")
                        callback(null)
                        return
                    }

                    val jsonData = response.body.string()
                    try{
                        val holidayResponse = gson.fromJson(jsonData, HolidayResponse::class.java)
                        Log.d("HolidayApiService", "获取的节假日信息${holidayResponse}")
                        callback(holidayResponse)
                    }catch (e: Exception){
                        Log.d("HolidayApiService", "获取的节假日信息${jsonData}")
                        Log.e("HolidayApiService", "解析JSON数据失败${e.message}", e)
                    }
                }
            }
        })
    }
}