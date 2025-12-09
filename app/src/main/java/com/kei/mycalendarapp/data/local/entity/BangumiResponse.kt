package com.kei.mycalendarapp.data.local.entity

import com.kizitonwose.calendar.core.WeekDay

data class BangumiResponse(
    val weekday: Weekday,
    val items: List<BangumiItem>
)

data class Weekday(
    val en: String,
    val cn: String,
    val ja: String,
    val id: Int
)

data class BangumiItem(
    val id: Int,
    val url: String,
    val type: Int,
    val name: String,
    val name_cn: String,
    val summary: String,
    val air_date: String,
    val air_weekday: Int,
    val rating: Rating?,
    val images: Images
)

data class Rating(
    val total: Int,
    val count: Map<String, Int>,
    val score: Double
)

data class Images(
    val large: String,
    val common: String,
    val medium: String,
    val small: String,
    val grid: String
)
