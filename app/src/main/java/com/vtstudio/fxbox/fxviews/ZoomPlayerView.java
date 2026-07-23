package com.vtstudio.fxbox.fxviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ui.PlayerView;

public class ZoomPlayerView extends PlayerView {
    public ZoomPlayerView(Context context) {
        super(context);
    }

    public ZoomPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void zoom(float scale) {

    }
}
