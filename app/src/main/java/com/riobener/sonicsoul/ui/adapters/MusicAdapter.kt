package com.riobener.sonicsoul.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.music.TrackInfo
import com.riobener.sonicsoul.data.music.TrackSource
import kotlinx.android.synthetic.main.activity_main.*
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
        val isDownloaded = trackInfo.localPath != null && trackInfo.trackSource != TrackSource.LOCAL
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(trackInfo)
        }
        holder.itemView.music_options.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.itemView.music_options)
            // add the menu

            popupMenu.inflate(R.menu.song_options)
            if (isDownloaded) {
                popupMenu.menu.removeItem(R.id.SaveSong)
            } else {
                popupMenu.menu.removeItem(R.id.DeleteSong)
            }
            // implement on menu item click Listener
            popupMenu.setOnMenuItemClickListener {
                if (isDownloaded) {
                    onSongDelete?.invoke(trackInfo)
                } else {
                    onSongDownload?.invoke(trackInfo)
                }
                false
            }
            popupMenu.show()
        }
        holder.itemView.apply {
            trackInfo.imageSource?.let { image ->
                Glide.with(this).load(image).into(music_img)
                music_img.clipToOutline = true
            } ?: music_img.setImageResource(R.drawable.icon)
            music_title.text = trackInfo.title
            music_author.text = trackInfo.artist

            if (trackInfo.isPlaying) {
                Glide.with(this).load(R.drawable.waveform).into(waveform)
                waveform.visibility = View.VISIBLE
            } else {
                waveform.visibility = View.GONE
            }
            if (isDownloaded) {
                download_marker.visibility = View.VISIBLE
            } else {
                download_marker.visibility = View.GONE
            }
            if(trackInfo.trackSource == TrackSource.LOCAL){
                music_options.visibility = View.GONE
            }else{
                music_options.visibility = View.VISIBLE
            }
        }
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onItemClick: ((TrackInfo) -> Unit)? = null

    var onSongDownload: ((TrackInfo) -> Unit)? = null
    var onSongDelete: ((TrackInfo) -> Unit)? = null

}