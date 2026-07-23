package com.vtstudio.fxbox.fxviews.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.listeners.OnProgressUpdateListener
import com.vtstudio.fxbox.media.MediaManger
import java.util.concurrent.Executors

class BackupDialog constructor(context: Context) : LoadingDialog(context) {

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    @Synchronized
    fun startBackup() {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val success = MediaManger.storeData(context, OnProgressUpdateListener { progress, max ->
                handler.post { setText(context.getString(R.string.backup) + " ${progress}/${max}") }
                if (progress == max) {
                    dismiss()
                }
            })
        }
        executor.shutdown()
    }
}