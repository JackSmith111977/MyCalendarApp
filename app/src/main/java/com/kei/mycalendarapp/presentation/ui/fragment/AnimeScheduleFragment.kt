package com.kei.mycalendarapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.data.local.CalendarDatabase
import com.kei.mycalendarapp.data.local.entity.BangumiItem
import com.kei.mycalendarapp.data.local.entity.CalendarEvent
import com.kei.mycalendarapp.domain.manager.EventColorManager
import com.kei.mycalendarapp.domain.manager.EventUpdateManager
import com.kei.mycalendarapp.domain.utils.BangumiApiService
import com.kei.mycalendarapp.presentation.ui.adapter.AnimeAdapter
import com.kei.mycalendarapp.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AnimeScheduleFragment: Fragment() {

    companion object{
        private const val ARG_DATE = "ARG_DATE"

        fun newInstance(date: LocalDate): AnimeScheduleFragment{
            val fragment = AnimeScheduleFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date.toString())
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var animeAdapter: AnimeAdapter
    private lateinit var bangumiApiService: BangumiApiService
    private lateinit var eventColorManager: EventColorManager
    lateinit var sharedViewModel: SharedViewModel
    private var currentDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        arguments?.getString(ARG_DATE)?.let {
            currentDate = LocalDate.parse(it)
        } ?: run {
            currentDate = LocalDate.now()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anime_schedule, container, false)

        recyclerView = view.findViewById(R.id.animeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        animeAdapter = AnimeAdapter(emptyList()){anime ->
            addToCalendar(anime)
        }
        recyclerView.adapter = animeAdapter

        bangumiApiService = BangumiApiService()

        currentDate?.let { loadAnimeForDate(it) } ?: loadAnimeForDate(LocalDate.now())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 监听日期变化
        sharedViewModel.selectedDate.observe(viewLifecycleOwner){ date ->
            loadAnimeForDate(date)
        }

    }

    private fun loadAnimeForDate(date: LocalDate) {
        currentDate = date
        bangumiApiService.getBangumiForDate(date){ animeList ->
            activity?.runOnUiThread {
                if (animeList != null){
                    animeAdapter.updateAnimeList(animeList)
                }else{
                    Toast.makeText(requireContext(), "获取动漫列表失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addToCalendar(anime: BangumiItem){
        // 获取选中的日期
        val selectedDate = sharedViewModel.selectedDate.value ?: LocalDate.now()

        // 解析播出时间（使用选中的日期，而不是动漫的原始播出日期）
        val airDateTime = LocalDateTime.of(selectedDate, LocalTime.of(9, 0))

        // 设置提醒时间为播出前15分钟
        val reminderTime = airDateTime.minusMinutes(15)

        val title = anime.name_cn.ifEmpty { anime.name }
        val content = buildString {
            append("番剧名称: ${anime.name_cn.ifEmpty { anime.name }}\n")
            append("原名: ${anime.name}\n")
            append("播出日期: ${anime.air_date}\n")
            append("Bangumi链接: ${anime.url}")
        }

        addEvent(
            title = title,
            description = content,
            date = selectedDate,  // 使用选中的日期
            reminderTime = reminderTime
        )
    }

    private fun addEvent(
        title: String,
        description: String,
        date: LocalDate,
        reminderTime: LocalDateTime
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = CalendarDatabase.getInstance(requireContext())
                val eventDao = database.eventDao()
                eventColorManager = EventColorManager()

                // 检查是否存在同名事件
                val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val existingEventCount = eventDao.getEventCountByTitleAndDate(title, startOfDay, endOfDay)

                if (existingEventCount > 0) {
                    // 如果已经存在同名事件，显示提示信息
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "该动画已在当天日程中", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val existsColors = getExistingEventColorsForDate(date)

                // 创建事件
                val event = CalendarEvent(
                    id = 0,
                    title = title,
                    content = description,
                    startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    endTime = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    eventColor = eventColorManager.getColorForEvent(existsColors),
                    reminderTime = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    isCompleted = false
                )

                eventDao.insertEvent(event)

                // 切换到主线程显示 Toast
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "${event.title}已添加到日程", Toast.LENGTH_SHORT).show()
                    EventUpdateManager.getInstance().notifyEventAdded()
                    sharedViewModel.notifyNewEventAdded(date)
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private suspend fun CoroutineScope.getExistingEventColorsForDate(selectedDate: LocalDate?): List<Int> {
        // 1. 检查日期是否为空
        if (selectedDate == null){
            return emptyList()
        }

        // 2. 计算时间范围
        val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = selectedDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 3. 查询数据库获取指定日期的所有事件
        val database = CalendarDatabase.getInstance(requireContext())
        val events = async {
            database.eventDao().getEventsInRange(startOfDay, endOfDay)
        }.await()

        // 4. 提取事件颜色
        return events.map { it.eventColor }
    }
}