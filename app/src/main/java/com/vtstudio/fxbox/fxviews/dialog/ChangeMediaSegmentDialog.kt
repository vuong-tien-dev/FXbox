package com.vtstudio.fxbox.fxviews.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.mutableStateMapOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.adapters.MediaSegmentCheckboxAdapter
import com.vtstudio.fxbox.adapters.PlaylistCheckboxAdapter
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.database.dao.PlaylistDao
import com.vtstudio.fxbox.database.dao.YTVideoDao
import com.vtstudio.fxbox.databinding.AddToPlaylistBottomDialogBinding
import com.vtstudio.fxbox.databinding.ChangeMediaSegmentDialogBinding
import com.vtstudio.fxbox.media.models.FxMediaVideo
import com.vtstudio.fxbox.media.models.Media
import com.vtstudio.fxbox.media.models.MediaSegment
import com.vtstudio.fxbox.media.models.tiktok.Playlist
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo
import com.vtstudio.fxbox.media.models.youtube.YTVideo
import com.vtstudio.fxbox.utils.ListUtils

class ChangeMediaSegmentDialog @JvmOverloads constructor
    (context: Context, val media: FxMediaVideo) :
    BottomSheetDialog(context, R.style.TransparentBottomSheetDialog) {

    private val mBinding: ChangeMediaSegmentDialogBinding = ChangeMediaSegmentDialogBinding.inflate(LayoutInflater.from(context))
    private var mAdapter: MediaSegmentCheckboxAdapter? = null
    private var listMediaSegment: ArrayList<MediaSegment>
    init {

        val id = media.fxId

        listMediaSegment = ArrayList(FxRoomDB.get(context).mediaSegmentDao().getAllByMediaId(id))

        if(listMediaSegment.isEmpty()) {
            mBinding.mediaSegmentList.setBorder(false, false, true, false);
        }

        mBinding.completeText.setOnClickListener {
            dismiss()
        }

        mBinding.newMediaSegmentIcon.setOnClickListener {
            dismiss()
            DialogHelper.showCreateMediaSegmentDialog(context, media)
        }

        val drw = AppCompatResources.getDrawable(context, R.drawable.add_32dp)
        drw?.setBounds(0, 0, 0, 0)

        mAdapter = MediaSegmentCheckboxAdapter(media, listMediaSegment)
        val layoutManger = LinearLayoutManager (context)
        mBinding.mediaSegmentList.adapter = mAdapter;
        mBinding.mediaSegmentList.layoutManager = layoutManger

        setContentView(mBinding.root)
    }


    override fun onDetachedFromWindow() {
        if(media is ShortsVideo) {
            val dao = FxRoomDB.get(context).shortsVideoDao();
            dao.update(media)
        } else if(media is YTVideo) {
            val dao = FxRoomDB.get(context).ytvideoDao();
            dao.update(media)
        } else {
            val dao = FxRoomDB.get(context).fxMediaVideoDao()
            dao.update(media)
        }
        super.onDetachedFromWindow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        window?.decorView?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(R.color.transparent)

    }

}