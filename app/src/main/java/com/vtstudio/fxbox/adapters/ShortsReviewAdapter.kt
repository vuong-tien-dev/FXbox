package com.vtstudio.fxbox.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vtstudio.fxbox.databinding.ShortsReviewItemLayoutBinding
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.utils.MediaViewUtils
import java.io.File

class ShortsReviewAdapter (var shortsReviewItems: ArrayList<ShortsVideo>?) : RecyclerView.Adapter<ShortsReviewAdapter.ShortsReviewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortsReviewHolder {
        val binding = ShortsReviewItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShortsReviewHolder(binding)
    }

    override fun getItemCount(): Int {
        return shortsReviewItems?.size ?: 0
    }

    override fun onBindViewHolder(holder: ShortsReviewHolder, position: Int) {
        val shorts = shortsReviewItems?.get(position)

        MediaViewUtils.loadImageFromMedia(shorts as ShortsVideo, holder.binding.shortsReviewThumbnail)
        shorts.shortsUser?.avatarPath?.let {
            Glide.with(holder.itemView.context)
                .load(File(it))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .override(holder.binding.authorAvt.width, holder.binding.authorAvt.height)
                .into(holder.binding.authorAvt)

        }
        shorts.shortsUser?.let {
            holder.binding.authorName.text = it.nickName
        }
    }

    class ShortsReviewHolder constructor(val binding: ShortsReviewItemLayoutBinding) : RecyclerView.ViewHolder (binding.root) {
    }
}