package com.vtstudio.fxbox.fxviews.dialog

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.utils.MediaViewUtils
import com.vtstudio.fxbox.adapters.SelectionAdapter
import com.vtstudio.fxbox.databinding.VideoSelectionLayoutBinding
import com.vtstudio.fxbox.listeners.OnSelectionItemListener
import com.vtstudio.fxbox.media.models.Media
import com.vtstudio.fxbox.utils.WindowUtils

class FxVideoSelectionDialog @JvmOverloads constructor
    (context: Context, media: Media, selections: ArrayList<Int> = ArrayList((1..9).toList())) :
    BottomSheetDialog(context, R.style.TransparentBottomSheetDialog) {

    companion object {
        const val ACTION_MUTE: Int = 1
        const val ACTION_ADJUST_VOLUME: Int = 2
        const val ACTION_CAPTURE: Int = 3
        const val ACTION_CHANGE_VIDEO_SEGMENT: Int = 4
        const val ACTION_MODE_AUTO_SWIPE: Int = 5
        const val ACTION_ENTER_WINDOW_VIDEO_MODE = 6
        const val ACTION_LIMIT: Int = 7
        const val ACTION_SYNC_DATA: Int = 8
        const val ACTION_ADD_TO_PLAYLIST: Int = 9
        const val ACTION_DELETE_MEDIA: Int = 10
        const val ACTION_SEE_PROPERTIES: Int = 11
        const val ACTION_EXPORT: Int = 12
        const val ACTION_PUSH_TO_DESKTOP: Int = 13
        const val ACTION_DISABLE_MUTE: Int = -1
        const val ACTION_REMOVE_LIMIT: Int = -7
        const val ACTION_DISABLE_MODE_AUTO_SWIPE: Int = -5

        @JvmStatic
        fun getDefaultSelections(): ArrayList<Int> = arrayListOf(
            ACTION_MUTE,
            ACTION_ADJUST_VOLUME,
            ACTION_CAPTURE,
            ACTION_EXPORT,
            ACTION_PUSH_TO_DESKTOP,
            ACTION_CHANGE_VIDEO_SEGMENT,
            ACTION_MODE_AUTO_SWIPE,
            ACTION_ENTER_WINDOW_VIDEO_MODE,
            ACTION_LIMIT,
            ACTION_SYNC_DATA,
            ACTION_ADD_TO_PLAYLIST,
            ACTION_DELETE_MEDIA,
            ACTION_SEE_PROPERTIES
        )
    }

    private var mBinding: VideoSelectionLayoutBinding? = null
    private var mSelections: ArrayList<Int> = selections
    private var mAdapter: SelectionAdapter? = null
    private var mMedia: Media? = media
    private var mListenerWrapper: OnSelectionItemListener
    private var mListener: OnSelectionItemListener? = null
    private var mOnCancel: DialogInterface.OnCancelListener? = null
    private var mOnShowWrapper: OnShowListener? = null

    init {
        mBinding = VideoSelectionLayoutBinding.inflate(LayoutInflater.from(context))
        mAdapter = SelectionAdapter(mSelections)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        mBinding?.selectionList?.layoutManager = layoutManager
        mBinding?.selectionList?.adapter = mAdapter

        mMedia?.let { it ->
            mBinding?.let { binding ->
                MediaViewUtils.loadImageFromMedia(it, binding.mediaThumbnail)
                binding.mediaName.text = it.mediaStoreName
                binding.mediaParent.text = it.mediaStoreParent
            }
        }

        mListenerWrapper = OnSelectionItemListener { action ->

            val isSolved = mListener?.onSelected(action) ?: true

            if (isSolved) {
                val index = mAdapter?.selections?.indexOf(action)
                if (index != null) {
                    mAdapter?.selections?.removeAt(index)
                    mAdapter?.selections?.add(index, getOppositeAction(action))
                    mAdapter?.notifyItemChanged(index)
                }
                if (isDismissSelections(action)) dismiss()
            }

            isSolved
        }

        setContentView(mBinding?.root as View)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.let { w ->
            w.decorView.let {
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.setBackgroundResource(R.color.transparent)
                it.setOnApplyWindowInsetsListener { _, insets -> insets }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                w.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
    }

    override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
        mOnShowWrapper = if (listener != null) OnShowListener { p0 ->
            this.window?.decorView?.rootView?.fitsSystemWindows = false
            listener.onShow(p0)
        }
        else null
        super.setOnShowListener(mOnShowWrapper)
    }

    override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
        this.mOnCancel = listener
        super.setOnCancelListener {
            mOnCancel?.onCancel(this)
            mOnCancel = null
        }
    }

    private fun getOppositeAction(action: Int): Int {
        return when (action) {
            ACTION_MUTE -> ACTION_DISABLE_MUTE
            ACTION_DISABLE_MUTE -> ACTION_MUTE
            ACTION_LIMIT -> ACTION_REMOVE_LIMIT
            ACTION_REMOVE_LIMIT -> ACTION_LIMIT
            ACTION_MODE_AUTO_SWIPE -> ACTION_DISABLE_MODE_AUTO_SWIPE
            ACTION_DISABLE_MODE_AUTO_SWIPE -> ACTION_MODE_AUTO_SWIPE
            else -> action
        }
    }

    private fun isDismissSelections(action: Int): Boolean = action in arrayOf(
        ACTION_LIMIT,
        ACTION_ADJUST_VOLUME,
        ACTION_REMOVE_LIMIT,
        ACTION_MODE_AUTO_SWIPE,
        ACTION_DISABLE_MODE_AUTO_SWIPE,
        ACTION_SYNC_DATA,
        ACTION_ADD_TO_PLAYLIST,
        ACTION_CHANGE_VIDEO_SEGMENT,
        ACTION_DELETE_MEDIA,
        ACTION_SEE_PROPERTIES,
        ACTION_EXPORT,
        ACTION_PUSH_TO_DESKTOP
    )

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAdapter?.onSelectionItemListener = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mAdapter?.onSelectionItemListener = mListenerWrapper
    }

    fun setOnSelectionClickListener(listener: OnSelectionItemListener) {
        this.mListener = listener
    }

    fun setContentMedia(media: Media) {
        this.mMedia = media
    }
}