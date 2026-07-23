package com.vtstudio.fxbox.listeners;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.player.FxPlayer;

public interface OnEventFromActivityListener {
    default void onViewsInitCompletely (Bundle bundle, int requestCode){};
    default void onFxPlayerBound (@NonNull FxPlayer player, int requestCode) {};
    default void onBottomNavMarginResult(int margin, int requestCode){};
}
