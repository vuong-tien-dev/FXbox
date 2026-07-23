package com.vtstudio.fxbox.helpers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.broadcast.PlayerActionReceiver;

public class NotificationHelper {

    // Hàm này tạo và đẩy thông báo MediaStyle
    public static void pushMediaNotification(Context context) {
        // 1. Tạo Notification Channel (áp dụng cho Android 8.0+)
        String channelId = "media_channel_id";
        String channelName = "Media Notifications";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
        );
        notificationManager.createNotificationChannel(channel);

        // 2. Tạo PendingIntent cho các hành động media
        // (Lưu ý: Bạn cần tạo một BroadcastReceiver (ví dụ: MediaActionReceiver) để xử lý các action này)
        Intent previousIntent = new Intent(context, PlayerActionReceiver.class);
        previousIntent.setAction("ACTION_PREVIOUS");
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                previousIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent playIntent = new Intent(context, PlayerActionReceiver.class);
        playIntent.setAction("ACTION_PLAY_PAUSE");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                playIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent nextIntent = new Intent(context, PlayerActionReceiver.class);
        nextIntent.setAction("ACTION_NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                nextIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Xây dựng thông báo với MediaStyle
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_icon_minium) // Thay bằng icon thông báo của bạn
                .setContentTitle("Bài hát đang phát")
                .setContentText("Tên nghệ sĩ")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Thêm các action: Previous, Play/Pause, Next
                .addAction(R.drawable.previous, "Previous", previousPendingIntent)
                .addAction(R.drawable.play, "Play/Pause", playPendingIntent)
                .addAction(R.drawable.next, "Next", nextPendingIntent)
                // Sử dụng MediaStyle để hiển thị các nút điều khiển ở chế độ compact
                .setStyle(new MediaStyle()
                        // Hiển thị nút hành động thứ 2 (index bắt đầu từ 0) khi thông báo ở chế độ compact
                        .setShowActionsInCompactView(1)
                );

        // 4. Đẩy thông báo
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManagerCompat.notify(1, builder.build());
    }
}
