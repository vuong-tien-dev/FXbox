package com.vtstudio.fxbox.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.vtstudio.fxbox.R
import com.vtstudio.fxbox.listeners.OnProgressUpdateListener
import com.vtstudio.fxbox.media.MediaManger
import com.vtstudio.fxbox.notifications.GeneralNotificationCreator

class MediaService : Service() {
    companion object {
        const val ACTION_STORE_DATA = "store_data"
    }

    private var notificationCreator: GeneralNotificationCreator? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        notificationCreator = GeneralNotificationCreator(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (intent.action == ACTION_STORE_DATA) {
                notificationCreator?.let { creator ->
                    creator.getNotification()?.let { notification ->
                        startForeground(1, notification)
                        MediaManger.storeData(this
                        ) { progress, max ->
                            creator.createNotification(
                                getString(R.string.backup),
                                0,
                                R.drawable.sync,
                                progress,
                                max
                            )
                        }
                    }
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

}