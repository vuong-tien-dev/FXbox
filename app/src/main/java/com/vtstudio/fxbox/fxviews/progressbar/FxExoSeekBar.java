package com.vtstudio.fxbox.fxviews.progressbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.player.FxExoPlayer;

public class FxExoSeekBar extends androidx.appcompat.widget.AppCompatSeekBar implements Player.Listener {
    private SimpleExoPlayer player;
    private Handler progressHandler;
    private Runnable progressRunner;
    private int t;

    private boolean trackingEffect;

    private boolean started;
    private boolean enabled;
    private Drawable activeDrawable;
    private Drawable inactiveDrawable;
    private Drawable activeThumb;
    private Drawable inactiveThumb;

    private OnSeekBarChangeListener onSeekBarChangeListener;
    private OnSeekBarChangeListener resizeWhenTouchListener;
    private boolean isTrackingTouch;
    private boolean releaseOnDetached;

    // Hàm tạo
    public FxExoSeekBar(Context context) {
        super(context);
    }

    public FxExoSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExoSeekBar);
        initAttrs(a);
        a.recycle();

    }

    public FxExoSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExoSeekBar);
        initAttrs(a);
        a.recycle();
    }

    /**
     * Khởi tạo các thuộc tính từ AttributeSet.
     * Initialize the attributes from the given AttributeSet.
     *
     * @param a the attribute set used to initialize the view.
     */
    private void initAttrs(TypedArray a) {

        trackingEffect = a.getBoolean(R.styleable.ExoSeekBar_trackingEffect, false);

        activeDrawable = a.getDrawable(R.styleable.ExoSeekBar_activeDrawable);
        inactiveDrawable = a.getDrawable(R.styleable.ExoSeekBar_inactiveDrawable);

        activeThumb = a.getDrawable(R.styleable.ExoSeekBar_activeThumb);
        inactiveThumb = a.getDrawable(R.styleable.ExoSeekBar_inactiveThumb);

        setBackground(null);

        setProgress(0);
        setThumb(inactiveThumb);

        if (inactiveDrawable != null) {
            setProgressDrawable(inactiveDrawable);
        }

        if (trackingEffect) {
            setTrackingEffect(true);
        }

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
                    progressHandler.postDelayed(this, FxExoSeekBar.this.t);
                }
            };
        }

        // Bắt đầu cập nhật trạng thái nếu ExoPlayer đang phát
        // Start updating the status if the ExoPlayer is currently playing
        if (player.isPlaying()) {
            enabled = true;
            start();
        }
    }

    /**
     * Phương thức được gọi khi trạng thái phát thay đổi.
     * Called when the playback state changes.
     *
     * @param state the new playback state.
     */

    @Override
    public void onPlaybackStateChanged(int state) {
        if (isTrackingTouch) return;
        Log.d("FxExoSeekBar", "onPlaybackStateChanged");
        if (state == Player.STATE_READY && player != null) {
            setMax((int) player.getDuration());
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
        Log.d("FxExoSeekBar", "onIsPlayingChanged");
        if (isTrackingTouch) return;
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
            setProgress((int) player.getCurrentPosition());
        }

        setThumb(inactiveThumb);
        resume();
        enabled = true;
    }

    public void unEnable() {
        setScaleY(0.3f);
        setThumb(null);
        setProgress(0);
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
            // kiểm tra trường hợp chưa setMax do create view chậm đoạn mã này sẽ khắc phục vấn đề
            if (player.getDuration() > getMax()) setMax((int) player.getDuration());

            if (inactiveThumb != null && enabled) {
                setThumb(inactiveThumb);
                setScaleY(1f);
            }

            progressHandler.postDelayed(progressRunner, t);
            started = true;
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
            setThumb(null);

            started = false;

        }
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

        setProgress(Math.toIntExact(player.getCurrentPosition()));
    }


    /**
     * Thiết lập hiệu ứng theo dõi khi kéo SeekBar.
     * Set up the tracking effect when dragging the SeekBar.
     *
     * @param b true to enable the tracking effect, false to disable it.
     */
    public void setTrackingEffect(boolean b) {

        trackingEffect = b;

        if (b) {

            if (resizeWhenTouchListener != null) return;

            resizeWhenTouchListener = new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    if (onSeekBarChangeListener != null) {
                        onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                    }

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                    isTrackingTouch = true;
                    pause();
                    if (activeDrawable != null) setProgressDrawable(activeDrawable);
                    setThumb(activeThumb);

                    if (onSeekBarChangeListener != null) {
                        onSeekBarChangeListener.onStartTrackingTouch(seekBar);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    isTrackingTouch = false;
                    if (inactiveDrawable != null) setProgressDrawable(inactiveDrawable);
                    setThumb(inactiveThumb);

                    if (player != null) player.seekTo(getProgress());

                    if (onSeekBarChangeListener != null) {
                        onSeekBarChangeListener.onStopTrackingTouch(seekBar);
                    }
                }
            };
            super.setOnSeekBarChangeListener(resizeWhenTouchListener);
        } else {
            resizeWhenTouchListener = null;
            super.setOnSeekBarChangeListener(onSeekBarChangeListener);
        }
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
        if (!trackingEffect) super.setOnSeekBarChangeListener(onSeekBarChangeListener);
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
            onSeekBarChangeListener = null;
            resizeWhenTouchListener = null;
            t = 0;
            setProgress(0);
        }

        if (player != null) {
            player.removeListener(this);
            player = null;
        }
    }

    public void releaseOnDetach(boolean releaseOnDetached) {
        this.releaseOnDetached = releaseOnDetached;
    }
}