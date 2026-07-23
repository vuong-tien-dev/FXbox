package com.vtstudio.fxbox.fragments

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.FragmentYoutubeBottomBinding
import com.vtstudio.fxbox.databinding.HorizontalVideoItemBinding
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.media.utils.ModelUtils
import com.vtstudio.fxbox.ui.MyRequestOptions
import java.io.File
import kotlin.math.roundToInt


class YoutubeBottomFragment : Fragment() {
    private var _binding: FragmentYoutubeBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYoutubeBottomBinding.inflate(inflater, container, false)
        binding.recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        binding.youtubeDetails.ytUserDetails.ytUserName.post {
            binding.youtubeDetails.ytUserDetails.ytUserName.maxWidth = (binding.youtubeDetails.ytUserDetails.ytWrapper.width*0.6f).roundToInt()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(_binding == null) return

        binding.recyclerView.adapter = ListAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    fun setTitle(title: String) {
        if(_binding == null) return

        binding.youtubeDetails.ytTitle.text = title
    }

    fun setTitle(title: SpannableString) {
        if(_binding == null) return

        binding.youtubeDetails.ytTitle.text = title
    }

    fun setUserName(userName: String) {
        if(_binding == null) return

        binding.youtubeDetails.ytUserDetails.ytUserName.text = userName
    }

    fun setViewCountAndCreateTime(viewCountAndCreateTime: String) {
        if(_binding == null) return

        binding.youtubeDetails.ytCountAndCreateTime.text = viewCountAndCreateTime
    }

    fun setUserSubscriberCount(subscriberCountText: String) {
        if(_binding == null) return

        binding.youtubeDetails.ytUserDetails.ytUserSubscriberCount.text = subscriberCountText
    }

    fun setUserImageFile(file: File) {
        if(_binding == null) return

        Glide.with(this)
            .asBitmap()
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .dontAnimate()
            .placeholder(R.drawable.default_avatar_user)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource)
                        .generate { palette ->
                            palette?.let {
                                if (_binding != null) {

                                    val dominantColor = it.getDominantColor(Color.BLACK)

                                    val gradientDrawable = GradientDrawable()
                                    gradientDrawable.orientation =
                                        GradientDrawable.Orientation.TOP_BOTTOM
                                    val colorBlend = blendColors(
                                        dominantColor,
                                        Color.BLACK,
                                        0.3f
                                    ) // Pha trộn màu với màu đen (30% dominantColor)
                                    gradientDrawable.colors = intArrayOf(
                                        colorBlend,
                                        Color.BLACK // Màu đen
                                    )
                                    gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                                    gradientDrawable.setGradientCenter(
                                        0.5f,
                                        0.3f
                                    ) // Đặt tâm gradient tại 30% từ đầu

                                    _binding?.youtubeDetails?.root?.background = gradientDrawable
                                }
                                _binding?.youtubeDetails?.ytUserDetails?.ytUserAvatar?.setImageBitmap(
                                    resource
                                )
                            }
                        }

                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }

    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1 - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun isBindingInit(): Boolean {
        return _binding != null
    }

    inner class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        private var videos: List<YTVideo> = mutableListOf()

        init {
            val db = FxRoomDB.get(context)
            videos = ModelUtils.fromYTDetailsList(db.ytvDetailsDao().allDetails)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding =
                HorizontalVideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return videos.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Log.d("Home", "onBindViewHolder: $position")
            Glide.with(holder.binding.videoThumbnail)
                .load(File(videos[position].ytVideoThumbnailPath))
                .dontAnimate()
                .apply(MyRequestOptions.getOptions())
                .into(holder.binding.videoThumbnail)
            holder.binding.videoTitle.text = videos[position].title
            holder.binding.videoCaption.text = videos[position].user?.channelName
        }

        inner class ViewHolder(val binding: HorizontalVideoItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
