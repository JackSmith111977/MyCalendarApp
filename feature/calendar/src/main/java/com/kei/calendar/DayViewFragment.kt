package com.kei.calendar

import android.media.metrics.Event
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kei.calendar.databinding.FragmentDayViewBinding


class DayViewFragment : Fragment() {
    // 可空的FragmentDayViewBinding对象，用于视图绑定
    private var _binding: FragmentDayViewBinding? = null
    // 通过getter返回非空的binding对象（!!操作符断言不为空）
    private val binding get() = _binding!!
    // 使用activityViewModels()委托获取DayViewModel实例，实现数据共享
    private val viewModel: DayViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 使用数据绑定类FragmentDayViewBinding inflate布局文件
        _binding = FragmentDayViewBinding.inflate(inflater, container, false)
        // 将inflate后的根视图返回给系统显示
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DayEventListAdapter{ event -> onEventClick(event) }
        binding.rvDayEvents.adapter = adapter
        binding.rvDayEvents.layoutManager = LinearLayoutManager(requireContext())

        viewModel.eventList.observe(viewLifecycleOwner){ events ->
            adapter.submitList(events)
        }
        viewModel.selectedDate.observe(viewLifecycleOwner){ date ->
            binding.tvDayTitle.text = date
            viewModel.loadEventsForDay(date)
        }
    }

    private fun onEventClick(event: Event){
        findNavController().navigate(DayViewFragmentDirections.actionViewToEventDetail(event.id))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}