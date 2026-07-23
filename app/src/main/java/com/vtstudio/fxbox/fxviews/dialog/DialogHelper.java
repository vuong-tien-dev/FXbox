package com.vtstudio.fxbox.fxviews.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.player.FxPlayer;

public class DialogHelper {
    public static void showCreateMediaSegmentDialog (@NonNull Context context, @NonNull FxMediaVideo media) {
        CreateMediaSegmentDialog dialog = new CreateMediaSegmentDialog(context, media);
        dialog.show();
    }

    public static void showCreatePlaylistDialog (@NonNull Context context) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog(context);
        dialog.show();
    }

    public static void showAdjustVolumeDialog (@NonNull Context context, @NonNull FxPlayer player, @NonNull FxMediaVideo mediaVideo) {
        AdjustVolumeDialog dialog = new AdjustVolumeDialog(context, player, mediaVideo);
        dialog.show();
    }
}
