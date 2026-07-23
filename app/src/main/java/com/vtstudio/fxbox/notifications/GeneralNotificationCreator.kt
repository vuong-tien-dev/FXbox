package com.vtstudio.fxbox.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class GeneralNotificationCreator(private var context: Context?) {

    companion object {
        const val CHANNEL_ID = "GENERAL_NOTIFICATION"
    }

    private var builder: NotificationCompat.Builder? = null
    private var notificationManagerCompat: NotificationManagerCompat? = NotificationManagerCompat.from(context!!)

    init {
        createNotificationBuilder()
    }

    private fun createNotificationBuilder() {
        builder = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
            setOnlyAlertOnce(true) // Show notification only the first time
            setShowWhen(false)
            setSilent(true)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = context?.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    fun createNotification(
        title: String,
        id: Int,
        iconResId: Int,
        progress: Int,
        total: Int
    ) {
        builder?.apply {
            setSmallIcon(iconResId)
            setContentTitle(title)
            setProgress(progress, total, false)
        }

        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        builder?.build()?.let { notificationManagerCompat?.notify(id, it) }
    }

    fun release() {
        builder?.clearActions()
        context = null
        builder = null
        notificationManagerCompat = null
    }

    fun getNotification(): Notification? {
        return builder?.build()
    }
}
