package com.kei.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kei.data.entity.EventEntity
import com.kei.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * 日视图的 ViewModel，用于管理日视图界面的状态和数据逻辑
 * 
 * @param eventRepository 事件数据仓库，用于获取和操作事件数据
 */
@HiltViewModel
class DayViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(getCurrentLocalDate())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow(DayViewUiState())
    val uiState: StateFlow<DayViewUiState> = _uiState.asStateFlow()

    init {
        loadEventsForDate(_selectedDate.value)
    }

    /**
     * 加载指定日期的事件数据
     * 
     * @param date 需要加载事件的日期
     */
    fun loadEventsForDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch{
            try {
                // 使用 Calendar 来处理日期，避免 API 级别问题
                val calendar = Calendar.getInstance()
                calendar.set(date.year, date.month - 1, date.day) // Month is 0-based in Calendar
                val timestamp = calendar.timeInMillis

                eventRepository.getEventsForDay(timestamp).collect { events ->
                    _uiState.update {
                        it.copy(
                            events = events,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }catch (e: Exception){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "加载日程失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 跳转到前一天
     */
    fun goToPreviousDay(){
        val currentDate = _selectedDate.value
        // 由于我们使用的是自定义的 LocalDate，需要手动实现减一天的逻辑
        val calendar = Calendar.getInstance()
        calendar.set(currentDate.year, currentDate.month - 1, currentDate.day)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        
        val previousDate = LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Calendar month is 0-based
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        loadEventsForDate(previousDate)
    }

    /**
     * 跳转到后一天
     */
    fun goToNextDay(){
        val currentDate = _selectedDate.value
        // 由于我们使用的是自定义的 LocalDate，需要手动实现加一天的逻辑
        val calendar = Calendar.getInstance()
        calendar.set(currentDate.year, currentDate.month - 1, currentDate.day)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        
        val nextDate = LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Calendar month is 0-based
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        loadEventsForDate(nextDate)
    }

    /**
     * 删除指定事件
     * 
     * @param event 需要删除的事件
     */
    suspend fun deleteEvent(event: EventEntity){
        eventRepository.deleteEvent(event)
        loadEventsForDate(_selectedDate.value)
    }

    /**
     * 获取当前日期
     * 
     * @return 当前日期的 LocalDate 对象
     */
    private fun getCurrentLocalDate(): LocalDate {
        val calendar = Calendar.getInstance()
        return LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // Calendar month is 0-based
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    
    /**
     * 自定义的日期类，用于替代需要高 API 级别的 java.time.LocalDate
     * 
     * @param year 年份
     * @param month 月份 (1-12)
     * @param day 日期 (1-31)
     */
    data class LocalDate(val year: Int, val month: Int, val day: Int)
}

/**
 * 表示日视图界面的状态
 * 
 * @param events 当前日期的事件列表
 * @param isLoading 数据加载状态
 * @param error 错误信息
 */
data class DayViewUiState(
    val events: List<EventEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)