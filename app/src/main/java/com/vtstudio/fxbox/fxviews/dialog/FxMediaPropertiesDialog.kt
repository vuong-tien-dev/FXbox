package com.vtstudio.fxbox.fxviews.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowManager
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.utils.MediaViewUtils
import com.vtstudio.fxbox.databinding.BaseDialogLayoutBinding
import com.vtstudio.fxbox.databinding.MediaPropertiesBinding
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.Media
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SetTextI18n", "SimpleDateFormat")
class FxMediaPropertiesDialog constructor(context: Context, val media: Media) : Dialog(context) {
    private var mBinding: BaseDialogLayoutBinding = BaseDialogLayoutBinding.inflate(layoutInflater)
    private var mContentBinding: MediaPropertiesBinding =
        MediaPropertiesBinding.inflate(layoutInflater)

    init {

//        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val mSize = Point ()
//        windowManager.defaultDisplay.getSize(mSize)
//
//        mBinding.root.minWidth = mSize.x*0.7f.toInt()
//        mBinding.root.minHeight = mSize.y*0.7f.toInt()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mBinding.okButton.visibility = View.GONE
        mBinding.baseContent.addView(mContentBinding.root)


        mBinding.baseTitle.text = context.getString(R.string.properties)
        mBinding.cancelButton.setOnClickListener(View.OnClickListener {
            dismiss()
        })

        MediaViewUtils.loadImageFromMedia(media, mContentBinding.mediaThumbnail)
        mContentBinding.mediaName.text = media.mediaStoreName

        mContentBinding.mediaFxIdTitle.text = context.getString(R.string.fx_id)
        mContentBinding.mediaTypeTitle.text = context.getString(R.string.media_type)
        mContentBinding.mediaSizeTitle.text = context.getString(R.string.size)
        mContentBinding.mediaDurationTitle.text = context.getString(R.string.duration)
        mContentBinding.mediaDayAddedTitle.text = context.getString(R.string.day_added)
        mContentBinding.mediaIsLimitedTitle.text = context.getString(R.string.is_limted)

        mContentBinding.mediaFxIdContent.text = media.fxId.toString()

        if (media is FxMediaVideo) {
            mContentBinding.mediaTypeContent.text = context.getString(R.string.device_video)
            mContentBinding.mediaSizeContent.text = "${(media.size / 1024)} Kb"
            mContentBinding.mediaDurationContent.text = TimeUtils.getTimeText(
                media.duration,
                context
            )
            mContentBinding.mediaDayAddedContent.text =
                SimpleDateFormat("dd/MM/yyyy").format(Date(media.mediaStoreDayAdded))
            mContentBinding.mediaIsLimitedContent.text = media.isLimited.toString()
        }

        if (media is ShortsVideo) {
            mContentBinding.mediaTypeContent.text = when {
                media.isImageList -> context.getString(R.string.media_type_photo_list)
                else -> context.getString(R.string.media_type_shorts_video)
            }
        }

        setContentView(mBinding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        window?.setLayout((size.x * 0.9f).toInt(), LayoutParams.WRAP_CONTENT)
        window?.decorView?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(R.color.transparent)
    }
}