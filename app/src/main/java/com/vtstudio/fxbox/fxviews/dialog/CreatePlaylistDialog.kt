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
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.database.FxRoomDB
import com.vtstudio.fxbox.databinding.BaseDialogLayoutBinding
import com.vtstudio.fxbox.databinding.CreatePlaylistDialogBinding
import com.vtstudio.fxbox.media.models.tiktok.Playlist

class CreatePlaylistDialog constructor(context: Context): Dialog(context) {
    private var mBaseDialog: BaseDialogLayoutBinding
    private var mContentDialog: CreatePlaylistDialogBinding
    init {

        val inflater = LayoutInflater.from(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mBaseDialog = BaseDialogLayoutBinding.inflate(inflater)
        mContentDialog = CreatePlaylistDialogBinding.inflate(inflater)
        mBaseDialog.baseContent.addView(mContentDialog.root)

        mBaseDialog.baseTitle.text = context.getString(R.string.add_new_playlist)

        mBaseDialog.cancelButton.setOnClickListener {
            dismiss()
        }

        mBaseDialog.okButton.setOnClickListener {
            val dao = FxRoomDB.get(context).playlistDao()
            val name = mContentDialog.playlistNameEdt.text.toString()
            if(name.isNotBlank() && name.length > 1) {
                val timeCreated = System.currentTimeMillis()
                val newPlaylist =
                    Playlist(
                        timeCreated.toString(),
                        name,
                        null,
                        timeCreated
                    )
                dao.insert(newPlaylist)
                dismiss()
            }
        }
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