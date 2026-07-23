package com.vtstudio.fxbox.listeners;

public interface PipModeListener {
    default void onStateChange(boolean isEnter) {}
    default void onEnterPipEnableChanged(boolean enable) {}
}
