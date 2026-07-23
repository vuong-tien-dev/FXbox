package com.vtstudio.fxbox.media.player;

import android.content.Context;
import android.os.Looper;

import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.Clock;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.Media;

public class FxExoPlayer extends SimpleExoPlayer {
    private FxMediaVideo currentPlayingMedia;
    protected FxExoPlayer(Builder builder) {
        super(builder);
    }

    protected FxExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, MediaSourceFactory mediaSourceFactory, LoadControl loadControl, BandwidthMeter bandwidthMeter, AnalyticsCollector analyticsCollector, boolean useLazyPreparation, Clock clock, Looper applicationLooper) {
        super(context, renderersFactory, trackSelector, mediaSourceFactory, loadControl, bandwidthMeter, analyticsCollector, useLazyPreparation, clock, applicationLooper);
    }

    protected FxMediaVideo getCurrentPlayingMedia() {
        return currentPlayingMedia;
    }

    protected void setCurrentPlayingMedia(FxMediaVideo currentPlayingMedia) {
        this.currentPlayingMedia = currentPlayingMedia;
    }

    public long getSeekTime(long segmentTimeMillis) {
        return currentPlayingMedia != null ? currentPlayingMedia.getSeekTime(segmentTimeMillis) : getCurrentPosition() + segmentTimeMillis;
    }

    public long getDisplayTime() {
        return currentPlayingMedia != null ? currentPlayingMedia.getDisplayTime(getCurrentPosition()) : getCurrentPosition();
    }

    public long getSegmentedDuration() {
        return currentPlayingMedia != null ? currentPlayingMedia.getSegmentedDuration() : getDuration();

    }
}
