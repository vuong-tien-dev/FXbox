package com.vtstudio.fxbox.broadcast;

import static com.vtstudio.fxbox.notifications.MediaNotificationCreator.ACTION_SEEK_TO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vtstudio.fxbox.media.player.FxPlayer;

import java.util.Objects;

public class PlayerActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent actionIntent = new Intent(context, FxPlayer.class)
                .setAction(intent.getAction());
        if(Objects.equals(intent.getAction(), ACTION_SEEK_TO))
        {
            actionIntent.putExtra(ACTION_SEEK_TO, intent.getLongExtra(ACTION_SEEK_TO, 0));
        }
        context.startService(actionIntent);
    }
}
