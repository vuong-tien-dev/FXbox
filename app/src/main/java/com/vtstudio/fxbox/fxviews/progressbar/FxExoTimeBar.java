package com.vtstudio.fxbox.fxviews.progressbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.vtstudio.fxbox.R;

public class FxExoTimeBar extends DefaultTimeBar implements Player.Listener {
    private SimpleExoPlayer player;
    private Handler progressHandler;
    private Runnable progressRunner;
    private OnAttachStateChangeListener onAttachStateChangeListener;
    private OnScrubListener onScrubListener;
    private int t;

    private boolean started;
    private boolean enabled;
    private boolean releaseOnDetached;
    private boolean isPausedOnDetached;

    // Hàm tạo
    public FxExoTimeBar(Context context) {
        super(context);
    }

    public FxExoTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExoSeekBar);
        initAttrs(a);
        a.recycle();

    }

    public FxExoTimeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExoSeekBar);
        initAttrs(a);
        a.recycle();

    }

    public OnAttachStateChangeListener buildOnAttachStateChangeListener() {
        return new OnAttachStateChangeListener() {

            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                if(isPausedOnDetached) {
                    resume();
                    isPausedOnDetached = false;
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                if (started) {
                    pause();
                    isPausedOnDetached = true;
                }
            }
        };
    }

    public OnScrubListener buildOnScrubListener() {
        return new OnScrubListener() {

            @Override
            public void onScrubStart(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                if(player != null) {
                    player.seekTo(position);
                }
            }
        };
    }

    /**
     * Khởi tạo các thuộc tính từ AttributeSet.
     * Initialize the attributes from the given AttributeSet.
     *
     * @param a the attribute set used to initialize the view.
     */
    private void initAttrs(TypedArray a) {

        setPosition(0);
        setScrubberColor(getContext().getColor(R.color.colorAccent));

    }

    @Override
    public boolean post(Runnable action) {
        return super.post(action);
    }

    /**
     * Thiết lập ExoPlayer cho SeekBar và bắt đầu cập nhật trạng thái.
     * Set up the ExoPlayer for the SeekBar and start updating the status.
     *
     * @param player the SimpleExoPlayer instance to use.
     * @param t      the time interval for updating the SeekBar status ( t > 100 milis)
     */

    public void setUpWithExoPlayer(@NonNull SimpleExoPlayer player, int t) {

        // Thiết lập ExoPlayer và thời gian cập nhật SeekBar
        // Set up the ExoPlayer and time interval for updating the SeekBar
        this.player = player;
        player.addListener(this);
        this.t = t > 100 ? t : 1000;

        // Tạo và chạy Handler để cập nhật trạng thái SeekBar
        // Create and run a Handler to update the SeekBar status
        if (progressHandler == null) {
            progressHandler = new Handler();
            progressRunner = new Runnable() {
                @Override
                public void run() {
                    update();
                    progressHandler.postDelayed(this, FxExoTimeBar.this.t);
                }
            };
        }

        // Bắt đầu cập nhật trạng thái nếu ExoPlayer đang phát
        // Start updating the status if the ExoPlayer is currently playing
        if (player.isPlaying()) {
            enabled = true;
            start();
        }

        onAttachStateChangeListener = buildOnAttachStateChangeListener();
        addOnAttachStateChangeListener(onAttachStateChangeListener);

        onScrubListener = buildOnScrubListener();
        addListener(onScrubListener);
    }

    /**
     * Phương thức được gọi khi trạng thái phát thay đổi.
     * Called when the playback state changes.
     *
     * @param state the new playback state.
     */

    @Override
    public void onPlaybackStateChanged(int state) {
        Log.d("FxExoSeekBar", "onPlaybackStateChanged");
        if (state == Player.STATE_READY && player != null) {
            setDuration(player.getDuration());
            start();
        } else if (state == Player.STATE_IDLE) {
            pause();
        }
    }


    /**
     * Phương thức được gọi khi trạng thái phát thay đổi giữa play/pause.
     * Called when the play/pause state changes.
     *
     * @param isPlaying true if the player is currently playing.
     */
    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        Player.Listener.super.onIsPlayingChanged(isPlaying);
        if (isPlaying) {
            start();
        } else {
            update();
            pause();
        }

    }

    public void enable() {
        setScaleY(1f);

        if (player != null) {
            setPosition(player.getCurrentPosition());
        }

        resume();
        enabled = true;
    }

    public void unEnable() {
        setScaleY(0.3f);
        setPosition(0);
        pause();
        enabled = false;
    }

    /**
     * Bắt đầu cập nhật trạng thái SeekBar.
     * Start updating the SeekBar status.
     */
    private void start() {
        if (!started && progressRunner != null && progressHandler != null && player != null) {
            Log.d("FxExoSeekBar", "started");

            progressHandler.postDelayed(progressRunner, t);
            started = true;
        }
    }

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
        if(player != null) {
            setDuration(player.getDuration());
        }
    }

    /**
     * Dừng cập nhật trạng thái SeekBar.
     * Stop updating the SeekBar status.
     */
    private void stop() {
        if (started && progressRunner != null && progressHandler != null) {

            progressHandler.removeCallbacksAndMessages(null);

            setScaleY(0.1f);

            started = false;

        }
        if(onAttachStateChangeListener != null)
            removeOnAttachStateChangeListener(onAttachStateChangeListener);
    }

    public void pause() {
        if (started && progressRunner != null && progressHandler != null) {
            Log.d("FxExoSeekBar", "pause");
            progressHandler.removeCallbacksAndMessages(null);

            started = false;
        }
    }

    public void resume() {
        if (!started && progressRunner != null && progressHandler != null && player.isPlaying()) {
            progressHandler.postDelayed(progressRunner, t);
            started = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (releaseOnDetached) release();
    }

    // Cập nhật trạng thái của SeekBar

    /**
     * Cập nhật trạng thái của SeekBar dựa trên trạng thái hiện tại của ExoPlayer.
     * Update the status of the SeekBar based on the current status of the ExoPlayer.
     */
    private void update() {

        if (player == null) {
            return;
        }

        setPosition(player.getCurrentPosition());
        setBufferedPosition(player.getBufferedPosition());
    }


    // Lấy khoảng thời gian cập nhật SeekBar
    public long getSeekBarUpdateInterval() {
        return t;
    }

    // Thiết lập khoảng thời gian cập nhật SeekBar
    public void setSeekBarUpdateInterval(int t) {
        this.t = t;
    }

    // Hủy bỏ cập nhật SeekBar khi không sử dụng
    public void release() {
        if (progressHandler != null && progressRunner != null) {
            stop();
            progressRunner = null;
            progressHandler = null;
            t = 0;
            setPosition(0);
            setBufferedPosition(0);
        }

        if(onAttachStateChangeListener != null) removeOnAttachStateChangeListener(onAttachStateChangeListener);

        if(onScrubListener != null) removeListener(onScrubListener);

        if (player != null) {
            player.removeListener(this);
            player = null;
        }
    }

    public void releaseOnDetach(boolean releaseOnDetached) {
        this.releaseOnDetached = releaseOnDetached;
    }
}