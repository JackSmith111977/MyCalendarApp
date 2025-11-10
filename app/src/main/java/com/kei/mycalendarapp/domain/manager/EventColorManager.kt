package com.kei.mycalendarapp.domain.manager

import android.graphics.Color
import kotlin.random.Random

/** 颜色管理器 */
class EventColorManager {
    private val eventColors = listOf(
        // 一组醒目的事件颜色
        Color.parseColor("#FF5252"),   // 红色
        Color.parseColor("#FF4081"),   // 粉色
        Color.parseColor("#E040FB"),   // 紫色
        Color.parseColor("#7C4DFF"),   // 深紫色
        Color.parseColor("#536DFE"),   // 靛蓝色
        Color.parseColor("#448AFF"),   // 蓝色
        Color.parseColor("#40C4FF"),   // 浅蓝色
        Color.parseColor("#18FFFF"),   // 青色
        Color.parseColor("#64FFDA"),   // 水绿色
        Color.parseColor("#69F0AE"),   // 绿色
        Color.parseColor("#B2FF59"),   // 浅绿色
        Color.parseColor("#EEFF41"),   // 黄绿色
        Color.parseColor("#FFFF00"),   // 黄色
        Color.parseColor("#FFD740"),   // 琥珀色
        Color.parseColor("#FFAB40"),   // 橙色
        Color.parseColor("#FF6E40")    // 深橙色
    )

    /** 获取一个随机颜色 */
    fun getRandomColor(): Int {
        return eventColors[Random.nextInt(eventColors.size)]
    }

    /** 获取一个事件颜色，该颜色不在已存在的颜色列表中 */
    fun getColorForEvent(existingColors: List<Int>): Int{
        // 如果已存在的颜色数量已经超过了可用的颜色数量，则返回一个随机颜色
        if (existingColors.size >= eventColors.size) {
            return getRandomColor()
        }

        // 获取未使用的颜色
        val availableColors = eventColors.filter {
            !existingColors.contains(it)
        }

        // 从可用的颜色中随机选择
        return availableColors[Random.nextInt(availableColors.size)]

    }
}