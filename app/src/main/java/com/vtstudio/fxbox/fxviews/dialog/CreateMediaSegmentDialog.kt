package com.vtstudio.fxbox.fxviews.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.BaseDialogLayoutBinding
import com.vtstudio.fxbox.databinding.CreateMediaSegmentDialogBinding
import com.vtstudio.fxbox.databinding.CreatePlaylistDialogBinding
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.MediaSegment
import com.vtstudio.fxbox.media.models.tiktok.Playlist
import com.vtstudio.fxbox.utils.TimeUtils

class CreateMediaSegmentDialog constructor(context: Context, val media: FxMediaVideo): Dialog(context) {
    private var mBaseDialog: BaseDialogLayoutBinding
    private var mContentDialog: CreateMediaSegmentDialogBinding
    init {

        val inflater = LayoutInflater.from(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mBaseDialog = BaseDialogLayoutBinding.inflate(inflater)
        mContentDialog = CreateMediaSegmentDialogBinding.inflate(inflater)
        mBaseDialog.baseContent.addView(mContentDialog.root)

        mBaseDialog.baseTitle.text = context.getString(R.string.add_new_media_segment)
        mContentDialog.segmentRangeSlider.setRange(0f, media.duration.toFloat(), 1f)
        mContentDialog.endTimeTxt.text = TimeUtils.formatDuration(media.duration)

        mBaseDialog.cancelButton.setOnClickListener {
            dismiss()
        }

        mBaseDialog.okButton.setOnClickListener {
            val dao = FxRoomDB.get(context).mediaSegmentDao()
            val title = mContentDialog.mediaSegmentTitleEdt.text.toString()
            val start =  mContentDialog.segmentRangeSlider.leftSeekBar.progress.toLong()
            val end =  mContentDialog.segmentRangeSlider.rightSeekBar.progress.toLong()
            if(title.isNotBlank() && title.length > 1) {
                val newMediaSegment = MediaSegment(0, title, start, end, media.fxId)
                dao.insert(newMediaSegment)
                dismiss()
            }
        }

        mContentDialog.segmentRangeSlider.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                mContentDialog.startTimeTxt.text = TimeUtils.formatDuration(leftValue.toLong())
                mContentDialog.endTimeTxt.text = TimeUtils.formatDuration(rightValue.toLong())
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

        })
        setContentView(mBaseDialog.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        window?.setLayout((size.x*0.8f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.decorView?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundResource(R.color.transparent)
    }
}