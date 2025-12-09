package com.kei.mycalendarapp.domain.utils

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.Volatile

class CacheManager private constructor(context: Context){
    companion object{
        private const val CACHE_SIZE = (200 * 1024 * 1024).toLong() // 200 MB
        private const val CACHE_TIME = 60 * 60 * 24 * 7 // 7天

        @Volatile
        private var INSTANCE: CacheManager? = null

        fun getInstance(context: Context): CacheManager{
            return INSTANCE?: synchronized(this){
                INSTANCE ?: CacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val cache: Cache = Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE)

    private val cacheInterceptor = Interceptor { chain ->
        val request = chain.request()
        Log.d("CacheManager", "发起请求：${request.url}")

        val originalResponse = chain.proceed(request)

        // 检查是否来自缓存
        val isFromCache =
            originalResponse.header("X-Android-Response-Source")?.contains("CACHE") ?: false
        if (isFromCache) {
            Log.d("CacheManager", "来自缓存：${request.url}")
        } else {
            Log.d("CacheManager", "来自网络：${request.url}")
        }
        // 删除不存在的引用，直接构建缓存控制头部
        originalResponse.newBuilder()
            .header("Cache-Control", "public, max-age=$CACHE_TIME")
            .removeHeader("Pragma")
            .build()
    }

    private val offlineCacheInterceptor = Interceptor { chain ->
        var request = chain.request()

        if (!isNetworkAvailable(chain)) {
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_TIME)
                .build()
        }

        chain.proceed(request)
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addNetworkInterceptor(cacheInterceptor)
        .addInterceptor(offlineCacheInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun isNetworkAvailable(chain: Interceptor.Chain): Boolean{
        val connectivityManager = chain.connection()?.route()?.address?.toString()
        // 检查网络状态的实际实现
        return true
    }
}