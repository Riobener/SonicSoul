package com.riobener.sonicsoul.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.music.TrackInfo
import kotlinx.android.synthetic.main.music_items.view.*

class MusicAdapter : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private val callBack = object : DiffUtil.ItemCallback<TrackInfo>() {
        override fun areItemsTheSame(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
            return oldItem.externalId == newItem.externalId
        }
        override fun areContentsTheSame(oldItem: TrackInfo, newItem: TrackInfo): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, callBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.music_items, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val trackInfo = differ.currentList[position]
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(trackInfo)
        }

        holder.itemView.apply{
            trackInfo.imageSource?.let{ image ->
                Glide.with(this).load(image).into(music_img)
                music_img.clipToOutline = true
            }
            music_title.text = trackInfo.title
            music_author.text = trackInfo.artist

            if(trackInfo.isPlaying){
                Glide.with(this).load(R.drawable.waveform).into(waveform)
                waveform.visibility = View.VISIBLE
            }else{
                waveform.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onItemClick: ((TrackInfo) -> Unit)? = null

    fun setOnItemClickListener(listener: (TrackInfo) -> Unit) {
        onItemClick = listener
    }

}