package com.vtstudio.fxbox.media.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.media.models.Media;

import java.util.List;

public interface OnQueryMediaListener {
    List<Media> onQueryMedia (@NonNull FxRoomDB db, @NonNull MediaRequest request, @Nullable String playlistId, int currentSize);
}
