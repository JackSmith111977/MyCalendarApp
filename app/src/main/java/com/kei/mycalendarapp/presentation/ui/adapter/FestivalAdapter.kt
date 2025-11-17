package com.kei.mycalendarapp.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kei.mycalendarapp.R

class FestivalAdapter(private var festivals: List<String>) : RecyclerView.Adapter<FestivalAdapter.FestivalViewHolder> () {
    inner class FestivalViewHolder(view: View): RecyclerView.ViewHolder(view){
        val festivalText: TextView = view.findViewById(R.id.festivalText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_festival, parent, false)
        return FestivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: FestivalViewHolder, position: Int) {
        holder.festivalText.text = festivals[position]
    }

    override fun getItemCount(): Int {
        return festivals.size
    }

    fun updateFestivals(newFestivals: List<String>){
        festivals = newFestivals
        notifyDataSetChanged()
    }
}