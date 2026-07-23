package com.vtstudio.fxbox.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.vtstudio.fxbox.R;

public class DownloadNotificationCreator {
    public static final String CHANNEL_ID = "FX_DOWNLOAD_NOTIFICATION";

//    private static final String ACTION_PAUSE = "pause";
//    private static final String ACTION_RESUME = "resume";
//    private static final String ACTION_CANCEL = "cancel";
    private Context context;
    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManagerCompat;
    public DownloadNotificationCreator(Context context) {

        this.context = context;

        notificationManagerCompat = NotificationManagerCompat.from(context);

        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.download)
                .setOnlyAlertOnce(true)//show notification for only first time
                .setShowWhen(false)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }

    public void createChannel() {
        NotificationChannel channel = new NotificationChannel(DownloadNotificationCreator.CHANNEL_ID,
                context.getString(R.string.download_title), NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void createNotification(@NonNull String title, int id, long progress, long total, boolean isFailed, boolean isCompleted, boolean isNetworkErrol, boolean hideProgress) {

        builder.setContentTitle(title)
                .setProgress(0, 0, false);

        String content = "";
        if(isNetworkErrol) {
           content  = context.getString(R.string.download_is_waiting_network);
        } else if (isFailed){
            content  = context.getString(R.string.download_is_failed);
        } else if (isCompleted) {
            content  = context.getString(R.string.download_is_success);
        } else {
            builder.setProgress((int) total, (int) progress, total == 0);
        }

        builder.setContentText(content);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManagerCompat.notify(id, builder.build());
    }

    public void release () {
        builder.clearActions();
        context = null;
        builder = null;
        notificationManagerCompat = null;
    }

    public void createNotification(@NonNull String title, int id, long progress, long total, boolean isFailed, boolean isCompleted, boolean isNetworkErrol)
    {
        createNotification(title, id, progress, total, isFailed, isCompleted, isNetworkErrol, false);
    }

        public Notification getNotification ()
    {
        return builder.build();
    }
}
