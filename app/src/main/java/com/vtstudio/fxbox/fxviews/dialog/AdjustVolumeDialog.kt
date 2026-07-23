package com.vtstudio.fxbox.fxviews.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.AdjustVolumeDialogLayoutBinding
import com.vtstudio.fxbox.databinding.BaseDialogLayoutBinding
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.media.player.FxPlayer

@SuppressLint("SetTextI18n")
class AdjustVolumeDialog  constructor(context: Context, player: FxPlayer, media: FxMediaVideo): Dialog(context) {
    val MAX_ADJUST_VOLUME = 2
    private var mBaseDialog: BaseDialogLayoutBinding
    private var mContentDialog: AdjustVolumeDialogLayoutBinding
    init {

        val inflater = LayoutInflater.from(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mBaseDialog = BaseDialogLayoutBinding.inflate(inflater)
        mContentDialog = AdjustVolumeDialogLayoutBinding.inflate(inflater)
        mBaseDialog.baseContent.addView(mContentDialog.root)

        mBaseDialog.baseTitle.text = context.getString(R.string.action_adjust_volume)

        mBaseDialog.cancelButton.setOnClickListener {
            dismiss()
        }

        mBaseDialog.okButton.setOnClickListener {
            Log.d("AdjustVolumeDialog", "volume before: ${media.volume}")
            media.volume = ((mContentDialog.volumeControlBar.progress.toFloat()/mContentDialog.volumeControlBar.max))*MAX_ADJUST_VOLUME + 1;
            Log.d("AdjustVolumeDialog", "volume after: ${media.volume}")
            when (media) {
                is ShortsVideo -> {
                    FxRoomDB.get(context).shortsVideoDao().update(media)
                }

                is YTVideo -> {
                    FxRoomDB.get(context).ytvideoDao().update(media)
                }

                else -> {
                    FxRoomDB.get(context).fxMediaVideoDao().update(media)
                }
            }

            player.refreshVolume()
            dismiss()
        }

        mContentDialog.volumeControlBar.progress = (((media.volume - 1)/MAX_ADJUST_VOLUME)*100).toInt()
        mContentDialog.maxVolumeTv.text = (MAX_ADJUST_VOLUME + 1).toString()
        setContentView(mBaseDialog.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        window?.setLayout((size.x*0.8f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.decorView?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundResource(
            R.color.transparent)
    }
}