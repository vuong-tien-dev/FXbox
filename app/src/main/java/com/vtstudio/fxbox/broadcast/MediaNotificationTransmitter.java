package com.vtstudio.fxbox.broadcast;

import static com.vtstudio.fxbox.notifications.MediaNotificationCreator.ACTION_KEY;
import static com.vtstudio.fxbox.notifications.MediaNotificationCreator.ACTION_SEEK_TO;
import static com.vtstudio.fxbox.notifications.MediaNotificationCreator.MUSIC_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class MediaNotificationTransmitter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent actionIntent = new Intent(MUSIC_ACTION)
                .putExtra(ACTION_KEY, intent.getAction());
        if(Objects.equals(intent.getAction(), ACTION_SEEK_TO))
        {
            actionIntent.putExtra(ACTION_SEEK_TO, intent.getLongExtra(ACTION_SEEK_TO, 0));
        }
        context.sendBroadcast(actionIntent);
    }
}