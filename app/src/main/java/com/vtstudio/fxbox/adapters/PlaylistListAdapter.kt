package com.vtstudio.fxbox.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.PlaylistItemVerticalBinding
import com.vtstudio.fxbox.listeners.OnItemListClickListener
import com.vtstudio.fxbox.media.MediaTool
import com.vtstudio.fxbox.media.models.tiktok.Playlist
import com.vtstudio.fxbox.ui.MyRequestOptions
import java.io.File

class PlaylistListAdapter(var playlistList: List<Playlist> = mutableListOf<Playlist>()) : RecyclerView.Adapter<PlaylistListAdapter.PlaylistHolder>() {
    private var onItemClickListener: OnItemListClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistListAdapter.PlaylistHolder {
        val binding = PlaylistItemVerticalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistHolder(binding)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemListClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onBindViewHolder(holder: PlaylistListAdapter.PlaylistHolder, position: Int) {
        val playlist = playlistList[position]
        val binding = holder.binding
        val context = holder.itemView.context

        if(playlist.videoIdList.isNotEmpty()) {
            val media = MediaTool.getMediaByFxId(context, playlist.videoIdList[0].toLong())

            media?.let {
                val imagePath = MediaTool.getVideoThumbnail(it)
                Glide.with(context).load(File(imagePath)).apply(MyRequestOptions.getOptions()).into(binding.playlistItemThumbnail)
            }
        }

        binding.playlistItemName.text = playlist.name
        binding.playlistItemVideoCount.text = context.getString(R.string.video_count, playlist.videoIdList.size)
        binding.root.setOnClickListener {
                _  -> onItemClickListener?.onClick(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return playlistList.size ?: 0
    }

    class PlaylistHolder(val binding: PlaylistItemVerticalBinding) : RecyclerView.ViewHolder(binding.root){

    }
}