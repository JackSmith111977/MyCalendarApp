package com.kei.mycalendarapp.presentation.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kei.mycalendarapp.R
import com.kei.mycalendarapp.data.local.entity.BangumiItem
import com.kei.mycalendarapp.data.local.entity.BangumiResponse

/**
 * 番剧列表适配器
 */
class AnimeAdapter(
    private var animeList: List<BangumiItem>,
    private val onAddToCalendarListener: (BangumiItem) -> Unit
): RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    inner class AnimeViewHolder(view: View): RecyclerView.ViewHolder(view){
        val animeImageView: ImageView = view.findViewById(R.id.animeImageView)
        val animeTitleTextView: TextView = view.findViewById(R.id.animeTitleTextView)
        val animeInfoTextView: TextView = view.findViewById(R.id.animeInfoTextView)
        val addEventButton: ImageButton = view.findViewById(R.id.addEventButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime_card, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animeList[position]

        // 显示番剧标题
        holder.animeTitleTextView.text = anime.name_cn.ifEmpty { anime.name }

        // 显示番剧信息
        val info = "播出日期: ${anime.air_date}\nID: ${anime.id}"
        holder.animeInfoTextView.text = info

        // 加载番剧封面
        Log.d("AnimeAdapter", "Image URL: ${anime.images.medium}")
        Glide.with(holder.animeImageView.context)
            .load(anime.images.medium)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.animeImageView)

        // 设置添加到日程的点击事件
        holder.addEventButton.setOnClickListener {
            onAddToCalendarListener(anime)
        }
    }

    override fun getItemCount(): Int {
        return animeList.size
    }

    fun updateAnimeList(newList: List<BangumiItem>){
        animeList = newList
        notifyDataSetChanged()
    }


}