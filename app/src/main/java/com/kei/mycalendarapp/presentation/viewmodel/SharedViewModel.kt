package com.kei.mycalendarapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

// 创建 SharedViewModel.kt 文件
class SharedViewModel : ViewModel() {
    private val _selectedDate = MutableLiveData<LocalDate>()
    val selectedDate: LiveData<LocalDate> = _selectedDate

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }
}