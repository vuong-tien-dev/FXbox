package com.vtstudio.fxbox.fxviews.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.CreateMediaSegmentBottomSheetBinding
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.MediaSegment
import com.vtstudio.fxbox.utils.TimeUtils
import xyz.hasnat.sweettoast.SweetToast

class CreateMediaSegmentDialog @JvmOverloads constructor(
    context: Context,
    private val media: FxMediaVideo,
    private val onSegmentCreated: (() -> Unit)? = null
) : BottomSheetDialog(context, R.style.TransparentBottomSheetDialog) {

    private val binding = CreateMediaSegmentBottomSheetBinding.inflate(LayoutInflater.from(context))

    init {
        binding.segmentRangeSlider.setRange(0f, media.duration.toFloat(), 1f)
        binding.startTimeTxt.text = TimeUtils.formatDuration(0)
        binding.endTimeTxt.text = TimeUtils.formatDuration(media.duration)

        binding.cancelButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { saveSegment() }
        binding.segmentRangeSlider.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                binding.startTimeTxt.text = TimeUtils.formatDuration(leftValue.toLong())
                binding.endTimeTxt.text = TimeUtils.formatDuration(rightValue.toLong())
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) = Unit
            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) = Unit
        })
        setContentView(binding.root)
    }

    private fun saveSegment() {
        val title = binding.mediaSegmentTitleEdt.text?.toString()?.trim().orEmpty()
        val start = binding.segmentRangeSlider.leftSeekBar.progress.toLong()
        val end = binding.segmentRangeSlider.rightSeekBar.progress.toLong()

        if (title.length < 2) {
            SweetToast.warning(context, context.getString(R.string.segment_name_invalid))
            return
        }
        if (end <= start) {
            SweetToast.warning(context, context.getString(R.string.segment_range_invalid))
            return
        }

        FxRoomDB.get(context).mediaSegmentDao().insert(
            MediaSegment(0, title, start, end, media.fxId)
        )
        onSegmentCreated?.invoke()
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.decorView?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(R.color.transparent)
    }
}
