package com.vtstudio.fxbox.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.databinding.SelectionItemLayBinding
import com.vtstudio.fxbox.fxviews.dialog.FxVideoSelectionDialog
import com.vtstudio.fxbox.listeners.OnSelectionItemListener

class SelectionAdapter(selections: ArrayList<Int>) :
    RecyclerView.Adapter<SelectionAdapter.SelectionHolder>() {

    var selections: ArrayList<Int>? = selections
    private var mListener: OnSelectionItemListener? = null

    var onSelectionItemListener: OnSelectionItemListener? = null
        set (value) {
            mListener = value
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectionAdapter.SelectionHolder {
        val binding = SelectionItemLayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectionHolder (binding)
    }

    override fun onBindViewHolder(holder: SelectionAdapter.SelectionHolder, position: Int) {
        val selection = selections?.get(position) ?: 0
        val context = holder.itemView.context

        var imageResourceId = 0
        var stringResourceId = 0

        when (selection) {
            FxVideoSelectionDialog.ACTION_MUTE -> {
                imageResourceId = R.drawable.action_mute
                stringResourceId = R.string.action_mute
            }

            FxVideoSelectionDialog.ACTION_ADJUST_VOLUME -> {
                imageResourceId = R.drawable.action_adjust_volume
                stringResourceId = R.string.action_adjust_volume
            }

            FxVideoSelectionDialog.ACTION_DISABLE_MUTE -> {
                imageResourceId = R.drawable.action_unmute
                stringResourceId = R.string.action_unmute
            }

            FxVideoSelectionDialog.ACTION_CAPTURE -> {
                imageResourceId = R.drawable.action_capture_frame
                stringResourceId = R.string.action_capture
            }

            FxVideoSelectionDialog.ACTION_MODE_AUTO_SWIPE -> {
                imageResourceId = R.drawable.action_auto_swipe_mode
                stringResourceId = R.string.action_auto_swipe
            }

            FxVideoSelectionDialog.ACTION_CHANGE_VIDEO_SEGMENT -> {
                imageResourceId = R.drawable.action_change_video_segment
                stringResourceId = R.string.action_change_media_segment
            }

            FxVideoSelectionDialog.ACTION_DISABLE_MODE_AUTO_SWIPE -> {
                imageResourceId = R.drawable.action_auto_swipe_mode
                stringResourceId = R.string.action_disable_auto_swipe
            }

            FxVideoSelectionDialog.ACTION_ENTER_WINDOW_VIDEO_MODE -> {
                imageResourceId = R.drawable.mode_picture_in_picture
                stringResourceId = R.string.action_enter_window_video_mode
            }

            FxVideoSelectionDialog.ACTION_LIMIT -> {
                imageResourceId = R.drawable.limit_32_dp
                stringResourceId = R.string.action_limit
            }

            FxVideoSelectionDialog.ACTION_REMOVE_LIMIT -> {
                imageResourceId = R.drawable.not_limited
                stringResourceId = R.string.action_unlimit
            }

            FxVideoSelectionDialog.ACTION_SYNC_DATA -> {
                imageResourceId = R.drawable.sync
                stringResourceId = R.string.action_sync_data
            }

            FxVideoSelectionDialog.ACTION_ADD_TO_PLAYLIST -> {
                imageResourceId = R.drawable.add_to_playlist
                stringResourceId = R.string.action_add_to_playlist
            }

            FxVideoSelectionDialog.ACTION_DELETE_MEDIA -> {
                imageResourceId = R.drawable.delete
                stringResourceId = R.string.action_delete
            }

            FxVideoSelectionDialog.ACTION_SEE_PROPERTIES -> {
                imageResourceId = R.drawable.action_see_properties
                stringResourceId = R.string.action_see_properties
            }

            FxVideoSelectionDialog.ACTION_EXPORT -> {
                imageResourceId = R.drawable.action_export
                stringResourceId = R.string.action_export
            }
        }

        Glide.with(context).load(imageResourceId).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.binding.imageItem);
        holder.binding.textItem.setText(stringResourceId)
        holder.itemView.setOnClickListener {
            mListener?.onSelected(selection)
        }
    }

    /*
    const val ACTION_MUTE: Int = 1
        const val ACTION_CAPTURE: Int = 2
        const val ACTION_MODE_AUTO_SWIPE: Int = 3
        const val ACTION_LIMIT: Int = 4
        const val ACTION_SEE_PROPERTIES: Int = 5
     */

    override fun getItemCount(): Int {
        return selections?.size ?: 0
    }

    class SelectionHolder constructor (val binding: SelectionItemLayBinding) : RecyclerView.ViewHolder (binding.root) {
    }
}