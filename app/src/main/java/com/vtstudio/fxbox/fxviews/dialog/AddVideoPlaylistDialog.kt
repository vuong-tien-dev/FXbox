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
import com.vtstudio.fxbox.adapters.PlaylistCheckboxAdapter
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.database.dao.PlaylistDao
import com.vtstudio.fxbox.databinding.AddToPlaylistBottomDialogBinding
import com.vtstudio.fxbox.media.models.Media
import com.vtstudio.fxbox.media.models.tiktok.Playlist
import com.vtstudio.fxbox.utils.ListUtils

class AddVideoPlaylistDialog @JvmOverloads constructor
    (context: Context, val media: Media, selections: ArrayList<Int> = ArrayList((1..9).toList())) :
    BottomSheetDialog(context, R.style.TransparentBottomSheetDialog) {

    private val mBinding: AddToPlaylistBottomDialogBinding = AddToPlaylistBottomDialogBinding.inflate(LayoutInflater.from(context))
    private var mAdapter: PlaylistCheckboxAdapter? = null
    private var mPlaylistDao: PlaylistDao = FxRoomDB.get(context).playlistDao()

    init {

        val playlistList = mPlaylistDao.getAllPlaylistsWithExcludes(Playlist.getAppDefaultPlaylistId())
        playlistList.removeIf {
            val isAppPlaylist: Boolean
            val reg = Regex("[a-zA-Z]+")
            isAppPlaylist = reg.find(it.id) != null
            isAppPlaylist
        }

        val addedPlaylistId = ArrayList<String>()
        val idStr = media.fxId.toString()

        playlistList.forEach {
            if (it.videoIdList.contains(idStr)) {
                addedPlaylistId.add(it.id)
            }
        }

        if(playlistList.isEmpty()) {
            mBinding.playlistList.setBorder(false, false, true, false);
        }

        mBinding.completeText.setOnClickListener {
            dismiss()
        }

        mBinding.newPlaylist.setOnClickListener {
            dismiss()
            DialogHelper.showCreatePlaylistDialog(context)
        }

        val drw = AppCompatResources.getDrawable(context, R.drawable.add_32dp)
        drw?.setBounds(0, 0, 0, 0)

        mAdapter = PlaylistCheckboxAdapter(playlistList)
        mAdapter?.checkedIdList = addedPlaylistId
        val layoutManger = LinearLayoutManager (context)
        mBinding.playlistList.adapter = mAdapter;
        mBinding.playlistList.layoutManager = layoutManger

        setContentView(mBinding.root)
    }


    override fun onDetachedFromWindow() {
        mAdapter?.checkedIdList?.let {
                selected ->
            mAdapter?.playlistList?.let {
                    playlist ->

                mAdapter?.notRecheckedIdList?.let {
                    notRecheckedId: List<String> ->
                    playlist.removeIf { notRecheckedId.contains(it.id)}
                }

                playlist.forEach {
                    val id = it.id;
                    val idList = it.videoIdList
                    if (selected.contains(id)) {
                        ListUtils.addUnique(idList, media.fxId.toString())
                    } else {
                        ListUtils.removeAllOccurrences(idList, media.fxId.toString())
                    }
                    mPlaylistDao.update(it)
                }
            }
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