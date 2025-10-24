package com.kei.calendar

import android.media.metrics.Event
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DayEventListAdapter {
    class DayEventListAdapter(private val onItemClick: (Event) -> Unit) : ListAdapter<Event, DayEventListAdapter.ViewHolder>(EventDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemDayEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val event = getItem(position)
            holder.bind(event, onItemClick)
        }
        class ViewHolder(private val binding: ItemDayEventBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(event: Event, onItemClick: (Event) -> Unit) {
                binding.event = event
                binding.root.setOnClickListener { onItemClick(event) }
                binding.executePendingBindings()
            }
        }
        class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem == newItem
        }
    }
}