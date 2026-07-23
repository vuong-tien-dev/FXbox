package com.vtstudio.fxbox.adapters

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.databinding.VerticalVideoItemBinding
import com.vtstudio.fxbox.media.MediaTool
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.ui.MyRequestOptions
import com.vtstudio.fxbox.utils.FormatUtils
import com.vtstudio.fxbox.utils.TimeUtils
import java.io.File

class HomeItemAdapter(private var listItem: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setListItem(listItem: List<Any>) {
        this.listItem = listItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 0) {
            return VideoHolder(VerticalVideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            throw IllegalArgumentException("Object type unsupported")
        }
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //Log.d("Home", "onBindViewHolder $position")
        val context = holder.itemView.context
        if(holder is VideoHolder) {
            val video = listItem[position] as FxMediaVideo
            var caption: String? = null
            var title: String? = null
            val thumbnail: String = MediaTool.getVideoThumbnail(video)
            val userAvatar: String = MediaTool.getUserAvatar(video)

            if(video is YTVideo) {
                caption = video.user?.channelName + " • " + FormatUtils.formatCount(video.playCount) + " " + context.getString(R.string.views) + " • " + TimeUtils.getTimeAgo(video.mediaStoreDayAdded, context)
                title = video.title
            } else if (video is ShortsVideo) {
                caption = video.shortsUser?.nickName + " • " + FormatUtils.formatCount(video.playCount) + " " + context.getString(R.string.views) + " • " + TimeUtils.getTimeAgo(video.mediaStoreDayAdded, context)
                title = video.description
            }

            holder.binding.videoCaption.text = caption
            holder.binding.videoTitle.text = title
            holder.binding.videoDuration.text = TimeUtils.formatDuration(video.duration)

            Glide.with(holder.binding.videoThumbnail).clear(holder.binding.videoThumbnail)
            Glide.with(holder.binding.userAvatar).clear(holder.binding.userAvatar)

             Glide.with(context).load(File(thumbnail))
                 .override(holder.binding.videoThumbnail.width, holder.binding.videoThumbnail.height)
                 .encodeFormat(Bitmap.CompressFormat.JPEG)
                 .apply(MyRequestOptions.getOptions())
                 .into(object : CustomTarget<Drawable>() {
                     override fun onResourceReady(
                         resource: Drawable,
                         transition: Transition<in Drawable>?
                     ) {
                         resource.let {
                                 res ->
                             holder.binding.videoThumbnail.let {
                                 view ->
                                 view.post {

                                     val h = res.intrinsicHeight
                                     val w = res.intrinsicWidth
                                     var rational = w.toFloat()/h.toFloat()
                                     rational = if (rational < 1.6f && rational > 1.3f) 16/9f else rational
                                     val itemWidth = holder.binding.root.width
                                     val itemParams = view.layoutParams
                                     itemParams.height = (itemWidth/(rational)).toInt()
                                     view.requestLayout()

                                     view.setImageDrawable(res)
                                 }
                             }
                         }
                     }

                     override fun onLoadCleared(placeholder: Drawable?) {

                     }

                 })

            Glide.with(context).load(File(userAvatar))
                .override(holder.binding.userAvatar.width, holder.binding.userAvatar.height)
                .apply(MyRequestOptions.getOptions())
                .fitCenter()
                .into(holder.binding.userAvatar)

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(listItem[position] is FxMediaVideo) 0 else 1
    }

   inner class VideoHolder(val binding: VerticalVideoItemBinding) : RecyclerView.ViewHolder(binding.root)
}