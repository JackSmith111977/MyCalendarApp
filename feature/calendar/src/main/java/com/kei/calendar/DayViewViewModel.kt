package com.kei.calendar


import android.media.metrics.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DayViewViewModel(private val eventRepository: EventRepository): ViewModel() {
    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    fun loadEventForDay(date: String){
        viewModelScope.launch{
            eventRepository.getEventsForDay(date).collect{ events ->
                _eventList.value = events
            }
        }
    }
}