package com.vtstudio.fxbox.listeners;

public interface FxLifecycle {

    /**
     * Được gọi khi activity được tạo.
     */
    default void fxOnCreate() {
        // Do something when the activity is created.
    }

    /**
     * Được gọi khi activity được bắt đầu.
     */
    default void fxOnStart() {
        // Do something when the activity is started.
    }

    /**
     * Được gọi khi activity được tiếp tục.
     */
    default void fxOnResume() {
        // Do something when the activity is resumed.
    }

    /**
     * Được gọi khi activity được tạm dừng.
     */
    default void fxOnPause() {
        // Do something when the activity is paused.
    }

    /**
     * Được gọi khi activity được dừng.
     */
    default void fxOnStop() {
        // Do something when the activity is stopped.
    }

    /**
     * Được gọi khi activity bị hủy.
     */
    default void fxOnDestroy() {
        // Do something when the activity is destroyed.
    }
}