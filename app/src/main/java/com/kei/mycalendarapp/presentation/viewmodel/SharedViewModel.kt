package com.kei.mycalendarapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kei.mycalendarapp.data.local.CalendarDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

// 创建 SharedViewModel.kt 文件
class SharedViewModel : ViewModel() {
    private val _selectedDate = MutableLiveData<LocalDate>()
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _newEventAdded = MutableLiveData<LocalDate>()
    val newEventAdded: LiveData<LocalDate> = _newEventAdded

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun notifyNewEventAdded(date: LocalDate) {
        _newEventAdded.value = date
    }
}