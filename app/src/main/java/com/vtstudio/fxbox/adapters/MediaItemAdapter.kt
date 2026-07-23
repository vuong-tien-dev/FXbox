package com.vtstudio.fxbox.adapters

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.databinding.HorizontalVideoItemBinding
import com.vtstudio.fxbox.databinding.LoadMoreLayoutBinding
import com.vtstudio.fxbox.listeners.OnItemListClickListener
import com.vtstudio.fxbox.listeners.OnLoadMoreListener
import com.vtstudio.fxbox.media.MediaTool
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.ui.MyRequestOptions
import com.vtstudio.fxbox.utils.FormatUtils
import com.vtstudio.fxbox.utils.TimeUtils
import com.vtstudio.fxbox.utils.ViewsUtils
import java.io.File

class MediaItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mediaList: List<FxMediaVideo> = ArrayList()

    @set:SuppressLint("NotifyDataSetChanged")
    var isCanLoadMore = true
        set(value) {
            val wasCanLoadMore = field
            field = value
            if (wasCanLoadMore != value) {
                if (value) {
                    notifyItemInserted(itemCount - 1)
                } else {
                    notifyItemRemoved(itemCount)
                }
            }
        }
    private var onItemClickListener: OnItemListClickListener? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var selectedPosition = -1

    fun setOnItemClickListener(onItemClickListener: OnItemListClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val binding = HorizontalVideoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MediaSearchItemHolder(binding)
        }
        Log.d("PlaylistActivity", "LoadMore support")
        val binding =
            LoadMoreLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadMoreItemHolder(binding)
    }

    override fun onBindViewHolder(base: RecyclerView.ViewHolder, position: Int) {
        if (base is MediaSearchItemHolder) {
            val isSelected = position == selectedPosition
            Log.d("media_search_adapter", "bind $position")
            val mediaItem = mediaList[position] ?: return

            val context = base.itemView.context
            // solved data
            var name = MediaTool.getMediaTitle(mediaItem)
            val authorName = MediaTool.getUserName(mediaItem)

            var caption = ""
            if (mediaItem is YTVideo) {
                caption =
                    authorName + " • " + FormatUtils.formatCount(mediaItem.playCount) + " " + context.getString(
                        R.string.views
                    ) + " • " + TimeUtils.getTimeAgo(mediaItem.mediaStoreDayAdded, context)
                name = mediaItem.title
            } else if (mediaItem is ShortsVideo) {
                caption =
                    authorName + " • " + FormatUtils.formatCount(mediaItem.playCount) + " " + context.getString(
                        R.string.views
                    ) + " • " + TimeUtils.getTimeAgo(mediaItem.mediaStoreDayAdded, context)
            }

            base.itemCaption.text = caption
            base.itemName.text = name
            val thumbnail = MediaTool.getVideoThumbnail(mediaItem)
            Glide.with(context).load(File(thumbnail))
                .override(base.itemThumbnail.width, base.itemThumbnail.height)
                .apply(MyRequestOptions.getOptions())
                .into(base.itemThumbnail)
            base.itemView.setOnClickListener { _: View? -> notifyOnItemClick(base.bindingAdapterPosition) }

            val animation = AnimationUtils.loadAnimation(base.itemView.context, R.anim.scale_in)
            animation.duration = 50
            base.itemView.startAnimation(animation)

            if (isSelected) {
                base.itemView.setBackgroundResource(R.color.colorAccent)
            } else {
                ViewsUtils.addRipple(base.itemView)
            }
        }
    }

    fun notifyItemSelectedChange(newPosition: Int) {
        if (newPosition == selectedPosition) return

        Log.d("Selected", "Selected item $selectedPosition changed $newPosition")

        val currentPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(currentPosition)
        notifyItemChanged(selectedPosition)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val current = selectedPosition
            selectedPosition = -1
            notifyItemChanged(current)
        }, 1500)
    }

    override fun onViewRecycled(base: RecyclerView.ViewHolder) {
        if (base is MediaSearchItemHolder) {
            val holder = base
            try {
                Glide.with(holder.itemView).clear(holder.itemThumbnail)
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is LoadMoreItemHolder && onLoadMoreListener != null && isCanLoadMore) {
            Log.d("PlaylistActivity", "onViewAttachedToWindow LoadMoreItemHolder")
            onLoadMoreListener!!.onLoadMoreEvent(true, false)
        }
    }

    private fun notifyOnItemClick(position: Int) {
        if (onItemClickListener != null) onItemClickListener!!.onClick(position)
    }

    override fun getItemCount(): Int {
        return mediaList.size + if (isCanLoadMore) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= mediaList.size) 1 else 0
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener?) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    inner class MediaSearchItemHolder(val binding: HorizontalVideoItemBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        val itemThumbnail: ImageView = binding.videoThumbnail
        val itemName: TextView = binding.videoTitle
        val itemCaption: TextView = binding.videoCaption

    }

    class LoadMoreItemHolder(private val binding: LoadMoreLayoutBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}