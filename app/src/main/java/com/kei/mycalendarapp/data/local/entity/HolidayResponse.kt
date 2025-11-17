package com.kei.mycalendarapp.data.local.entity



data class HolidayResponse(
    val code: Int,
    val type: Type,
    val holiday: Holiday?
)
