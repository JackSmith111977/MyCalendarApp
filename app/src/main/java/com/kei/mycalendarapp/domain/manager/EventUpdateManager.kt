package com.kei.mycalendarapp.domain.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * 事件更新管理器
 * 使用单例模式管理事件更新通知，防止频繁更新UI
 */
class EventUpdateManager private constructor(){
    private val _eventUpdated = MutableLiveData<Boolean>()
    val eventUpdated: LiveData<Boolean> = _eventUpdated

    /**
     * 上次通知时间戳
     * 用于防抖处理，避免过于频繁的更新通知
     */
    private var lastNotifyTime = 0L
    
    /**
     * 防抖间隔时间（毫秒）
     * 两次通知之间的最小时间间隔
     */
    private val debounceInterval = 500L

    /**
     * 通知事件已添加
     * 通过防抖机制控制通知频率，避免短时间内重复通知
     */
    fun notifyEventAdded(){
        val currentTime = System.currentTimeMillis()
        // 如果当前时间间隔小于防抖间隔，则不执行通知
        if (currentTime - lastNotifyTime > debounceInterval){
            _eventUpdated.value = true
            lastNotifyTime = currentTime
        }
    }

    companion object{
        @Volatile
        private var INSTANCE: EventUpdateManager? = null

        /**
         * 获取 EventUpdateManager 的单例实例
         *
         * @return EventUpdateManager 的单例对象
         */
        fun getInstance(): EventUpdateManager{
            return INSTANCE ?: synchronized(this){
                val instance = EventUpdateManager()
                INSTANCE = instance
                instance
            }
        }
    }
}