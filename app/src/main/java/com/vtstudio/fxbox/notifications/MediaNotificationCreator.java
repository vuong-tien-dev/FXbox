package com.vtstudio.fxbox.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.broadcast.PlayerActionReceiver;
import com.vtstudio.fxbox.listeners.OnMediaNotificationPlaybackChanged;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.MediaTrack;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.utils.BitmapUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MediaNotificationCreator {
    public static final String MUSIC_ACTION = "fx_music_action";
    public static final String ACTION_KEY = "action_key";
    public static final String CHANNEL_ID = "FX_MUSIC_NOTIFICATION";
    public static final String ACTION_PREVIOUS = "previous";

    public static final String ACTION_NONE = "none";
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_SEEK_TO = "seek_to";
    public static final String ACTION_STOP = "stop";
    public static final int NOTIFICATION_ID = 888;
    private Notification notification;
    public Notification getNotification() {
        return notification;
    }

    private OnMediaNotificationPlaybackChanged onMediaNotificationPlaybackChanged;
    private Context context;
    private NotificationCompat.Builder builder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionCompat.Callback callback;
    private PlaybackStateCompat.Builder playBackStateBuilder;
    private PendingIntent pendingIntentPrevious;
    private PendingIntent pendingIntentPlay;
    private PendingIntent pendingIntentStop;
    private PendingIntent pendingIntentNext;
    private Intent intentPlay;
    private Intent intentPause;
    private androidx.media.app.NotificationCompat.MediaStyle mediaStyle;
    private NotificationManagerCompat notificationManagerCompat;
    private ExecutorService executor;

    public MediaNotificationCreator(Context context) {
        this.context = context;

        executor = Executors.newSingleThreadExecutor();

        notificationManagerCompat = NotificationManagerCompat.from(context);

        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon_minium_white)
                .setContentText(context.getString(R.string.fxplayer_is_running))
                .setOnlyAlertOnce(true)//show notification for only first time
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        metadataBuilder = new MediaMetadataCompat.Builder();

        playBackStateBuilder = new PlaybackStateCompat.Builder();
        playBackStateBuilder.setActions(PlaybackStateCompat.ACTION_SEEK_TO |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_PLAY);

        mediaSessionCompat = new MediaSessionCompat(context, "tag");
        callback = new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                if (onMediaNotificationPlaybackChanged != null) {
                    onMediaNotificationPlaybackChanged.onSeekTo(pos);
                }
            }

            @Override
            public void onSkipToPrevious() {
                Log.d("PlayerNotification", "onSkipToPrevious");
                if (onMediaNotificationPlaybackChanged != null) {
                    onMediaNotificationPlaybackChanged.onPrevious();
                }
            }

            @Override
            public void onSkipToNext() {
                if (onMediaNotificationPlaybackChanged != null) {
                    onMediaNotificationPlaybackChanged.onNext();
                }
            }

            @Override
            public void onPause() {
                if (onMediaNotificationPlaybackChanged != null) {
                    onMediaNotificationPlaybackChanged.onPause();
                }
            }

            @Override
            public void onPlay() {
                if (onMediaNotificationPlaybackChanged != null) {
                    onMediaNotificationPlaybackChanged.onPlay();
                }
            }

        };

        mediaSessionCompat.setCallback(callback);
        mediaSessionCompat.setActive(true);

        mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();

        notification = builder.build();
    }

    public synchronized void createNotification(MediaTrack track, boolean isPlaying, int state, int current) {

        String title = track.getTrackTitle();
        String author = track.getTrackAuthor();

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.getTrackDuration())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, author)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);

        playBackStateBuilder.setState(state, current, 1.0f)
                .setBufferedPosition(track.getTrackDuration());

        mediaSessionCompat.setPlaybackState(playBackStateBuilder.build());

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            builder.clearActions();

            if (pendingIntentPrevious == null) {
                Intent intentPrevious = new Intent(context, PlayerActionReceiver.class)
                        .setPackage(context.getPackageName())
                        .setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                        intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            }

            if (pendingIntentNext == null) {
                Intent intentNext = new Intent(context, PlayerActionReceiver.class)
                        .setPackage(context.getPackageName())
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 2,
                        intentNext, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            }

            if (pendingIntentStop == null) {
                Intent intentStop = new Intent(context, PlayerActionReceiver.class)
                        .setPackage(context.getPackageName())
                        .setAction(ACTION_STOP);
                pendingIntentStop = PendingIntent.getBroadcast(context, 3,
                        intentStop, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            }


            if (intentPlay == null) {
                intentPlay = new Intent(context, PlayerActionReceiver.class)
                        .setPackage(context.getPackageName())
                        .setAction(ACTION_PLAY);
            }

            if (intentPause == null) {
                intentPause = new Intent(context, PlayerActionReceiver.class)
                        .setPackage(context.getPackageName())
                        .setAction(ACTION_PAUSE);
            }

            int drw_previous;
            drw_previous = R.drawable.previous;

            int drw_next;
            drw_next = R.drawable.next;

            int drw_stop;
            drw_stop = R.drawable.stop;

            Intent intentPlayOrPause;
            int drw_play_or_pause;

            if (isPlaying) {
                intentPlayOrPause = intentPause;
                drw_play_or_pause = R.drawable.pause;
            } else {
                intentPlayOrPause = intentPlay;
                drw_play_or_pause = R.drawable.play;
            }

            pendingIntentPlay = PendingIntent.getBroadcast(context, 1,
                    intentPlayOrPause, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            mediaStyle.setShowActionsInCompactView(0, 1, 2);

            builder.addAction(drw_previous, "Previous", pendingIntentPrevious)
                    .addAction(drw_play_or_pause, "Play", pendingIntentPlay)
                    .addAction(drw_next, "Next", pendingIntentNext)
                    .addAction(drw_stop, "Cancel", pendingIntentStop);
        }

        BitmapUtils.getNotificationIconAsync((FxMediaVideo) track, context, executor, icon -> {
            if(icon != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, icon);
                } else {
                    builder.setLargeIcon(icon);
                }
            }

            mediaSessionCompat.setMetadata(metadataBuilder.build());

            notification = builder.setContentTitle(title)
                    .setContentText(author)
                    .setStyle(mediaStyle.setMediaSession(mediaSessionCompat.getSessionToken()))
                    .build();

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManagerCompat.notify(NOTIFICATION_ID, notification);
        });

        // tạo thông báo
    }
    ///////////////////////////////////////

    public void setOnMediaNotificationPlaybackChanged(OnMediaNotificationPlaybackChanged onMediaNotificationPlaybackChanged) {
        this.onMediaNotificationPlaybackChanged = onMediaNotificationPlaybackChanged;
    }

    public void release (){

        //
        mediaSessionCompat.release();

        // remove refs
        context = null;
        builder = null;
        pendingIntentNext = null;
        pendingIntentPrevious = null;
        pendingIntentStop = null;
        pendingIntentPlay = null;
        mediaStyle = null;
        mediaSessionCompat = null;
        metadataBuilder = null;
        onMediaNotificationPlaybackChanged = null;
        notificationManagerCompat = null;
        playBackStateBuilder = null;
        callback = null;
        if(executor != null){
            executor.shutdown();
            executor = null;
        }
    }
}