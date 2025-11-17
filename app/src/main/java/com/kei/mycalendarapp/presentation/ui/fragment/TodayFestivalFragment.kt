package com.kei.mycalendarapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.domain.utils.HolidayApiService
import com.kei.mycalendarapp.presentation.ui.adapter.FestivalAdapter
import com.kei.mycalendarapp.presentation.viewmodel.SharedViewModel
import java.time.LocalDate

class TodayFestivalFragment: Fragment() {

    companion object{
        private const val ARG_DATE = "arg_date"

        fun newInstance(date: LocalDate): TodayFestivalFragment{
            val fragment = TodayFestivalFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date.toString())
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var holidayApiService: HolidayApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedViewModel: SharedViewModel
    private var festivalAdapter: FestivalAdapter? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.selectedDate.observe(viewLifecycleOwner){date ->
            updateFestivalsForDate(date)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today_festival, container, false)
        recyclerView = view.findViewById(R.id.festivalRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        festivalAdapter = FestivalAdapter(emptyList())
        recyclerView.adapter = festivalAdapter
        holidayApiService = HolidayApiService()

        currentDate?.let { updateFestivalsForDate(it) } ?:updateFestivalsForDate(LocalDate.now())

        return view
    }

    fun updateFestivalsForDate(date: LocalDate){
        currentDate = date
        holidayApiService.getHolidayInfo(date){ response ->
            activity?.runOnUiThread {
                val festivals = mutableListOf<String>()

                // 添加对空响应的检查
                if (response == null) {
                    // 网络请求失败或API返回空响应
                    festivals.add("网络连接失败，请检查网络设置")
                    festivalAdapter?.updateFestivals(festivals)
                    return@runOnUiThread
                }

                if(response?.holiday != null){
                    // 是节日
                    festivals.add(response.holiday.name)
                }else if (response?.type != null){
                    // 不是节日
                    if (response.type.type == 1){
                        // 周末
                        festivals.add(response.type.name)
                    }else{
                        // 工作日
                        festivals.add("今天是${response.type.name}")
                    }
                }else{
                    festivals.add("无法获取节日信息")
                }

                festivalAdapter?.updateFestivals(festivals)
            }
        }
    }
}