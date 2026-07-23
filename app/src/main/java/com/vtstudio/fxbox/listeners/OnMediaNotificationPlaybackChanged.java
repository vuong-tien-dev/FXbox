package com.vtstudio.fxbox.listeners;

public interface OnMediaNotificationPlaybackChanged {
    default void onPlay() {
        // Provide a default implementation for onPlay()
    }

    default void onPlay(int pos, boolean fromNotification) {
        // Provide a default implementation for onPlay()
    }

    default void onResume() {
        // Provide a default implementation for fxOnResume()
    }

    default void onPause() {
        // Provide a default implementation for fxOnPause()
    }

    default void onStop() {
        // Provide a default implementation for fxOnStop()
    }

    default void onPrevious() {
        // Provide a default implementation for onPrevious()
    }

    default void onNext() {
        // Provide a default implementation for onNext()
    }

    default void onEnd() {
        // Provide a default implementation for onEnd()
    }

    default void onSeekTo(long pos)
    {

    }
}
