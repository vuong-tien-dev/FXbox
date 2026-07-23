package com.vtstudio.fxbox.fxviews.listview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.ViewUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.databinding.ListViewLayoutBinding
import com.vtstudio.fxbox.databinding.VideoQualityItemBinding
import com.vtstudio.fxbox.fxviews.listview.VideoQualityListView.VideoPlayer.Companion.getVideoQualityList
import com.vtstudio.fxbox.ui.SpaceItemDecoration
import com.vtstudio.fxbox.utils.ViewsUtils


class VideoQualityListView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    qualityList: List<String> = getVideoQualityList()
) : FrameLayout(context, attrs, defStyleAttr) {

    class VideoPlayer {
        companion object {
            const val QUALITY_144P = "144p"
            const val QUALITY_240P = "240p"
            const val QUALITY_360P = "360p"
            const val QUALITY_480P = "480p"
            const val QUALITY_720P = "720p"
            const val QUALITY_1080P = "1080p"

            @JvmStatic
            fun getVideoQualityList(): List<String> = arrayListOf(
                QUALITY_144P,
                QUALITY_240P,
                QUALITY_360P,
                QUALITY_480P,
                QUALITY_720P,
                QUALITY_1080P
            )
        }
    }

    private var binding: ListViewLayoutBinding =
        ListViewLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private var layoutManager: LayoutManager =
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    private var mListener: OnQualitySubmitListener? = null

    init {
        binding.recyclerView.addItemDecoration(SpaceItemDecoration(20))
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = ListAdapter(qualityList)
    }

    fun setLayoutManager(layoutManager: LayoutManager) {
        this.layoutManager = layoutManager
    }

    fun setOnQualitySubmit(listener: OnQualitySubmitListener) {
        this.mListener = listener
    }


    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    fun setQualityList(qualityList: List<String>) {
        val adapter = binding.recyclerView.adapter as ListAdapter
        adapter.qualityList = qualityList
        adapter.isSelected = false
        adapter.currentSelected = -1
        adapter.notifyDataSetChanged()
    }

    inner class ListAdapter constructor(var qualityList: List<String>) :
        RecyclerView.Adapter<ListAdapter.ViewHolder>() {

        var isSelected: Boolean = false
        var currentSelected = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding =
                VideoQualityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            if (viewType == 0) {
                ViewsUtils.setTouchScaleEffect(itemBinding.root, 0.95f)
            }
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            return qualityList.size + 1
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val p = position - 1

            if (position == 0) {

                if (isSelected) {
                    holder.binding.qualityText.text =
                        holder.itemView.context.getString(R.string.submit)
                    holder.binding.root.setCardBackgroundColor(holder.itemView.context.getColor(R.color.colorAccent))
                    holder.binding.root.setOnClickListener {
                        if(currentSelected > 0) {
                            mListener?.onSelected(qualityList[currentSelected - 1])
                            Log.d("VideoQuality", qualityList[currentSelected - 1])
                        }
                    }

                } else {
                    holder.binding.qualityText.text =
                        holder.itemView.context.getString(R.string.choose_quality)
                    holder.binding.root.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))
                }

            } else {
                if (currentSelected == position) {
                    holder.binding.root.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))
                } else {
                    holder.binding.root.setCardBackgroundColor(holder.itemView.context.getColor(R.color.rgb_160_a70))
                }

                holder.binding.qualityText.text = qualityList[p]
                holder.binding.root.setOnClickListener {
                    notifyItemChanged(currentSelected)
                    currentSelected = position
                    notifyItemChanged(currentSelected)

                    if (!isSelected) {
                        isSelected = true
                        notifyItemChanged(0)
                    }
                }
            }

        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) 0 else 1
        }

        inner class ViewHolder(val binding: VideoQualityItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    interface OnQualitySubmitListener {
        fun onSelected(qualityList: String)
    }
}