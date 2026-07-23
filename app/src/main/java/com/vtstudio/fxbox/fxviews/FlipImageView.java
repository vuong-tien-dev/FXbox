package com.vtstudio.fxbox.fxviews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.List;

public class FlipImageView extends androidx.appcompat.widget.AppCompatImageView {

    private List<String> imagePaths;
    private int currentImageIndex = 0;
    private Handler handler;
    private Runnable flipRunnable;
    private int flipDelay = 3000; // Thời gian delay mặc định giữa các ảnh (3 giây)
    private boolean isFlipping;
    private OnFlipImageListener onFlipImageListener;
    private int currentImageWidth = 0;
    private int currentImageHeight = 0;

    public FlipImageView(Context context) {
        super(context);
        init();
    }

    public FlipImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlipImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        handler = new Handler();
        flipRunnable = new Runnable() {
            @Override
            public void run() {
                if(isFlipping) {
                    flipImage();
                    handler.postDelayed(this, flipDelay);
                }
            }
        };
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
        if(imagePaths == null) return;

        currentImageIndex = 0;

        if(imagePaths.size() > 1) {
            final int temp = flipDelay;
            flipDelay = 0;
            startFlipping();
            flipDelay = temp;
        } else {
            stopFlipping();
            flipImage();
        }

    }

    public void setFlipDelay(int delayMillis) {
        this.flipDelay = delayMillis;
        startFlipping();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    private void release() {

        imagePaths = null;

        if(handler != null && flipRunnable != null) {
            stopFlipping();
        }
    }

    private void flipImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            currentImageIndex = currentImageIndex % imagePaths.size();
            final int finalIndex = currentImageIndex;
            String imagePath = imagePaths.get(currentImageIndex);
            Glide.with(getContext()).clear(this);
            Glide.with(this)
                    .load(new File(imagePath))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .encodeQuality(30)
                    .dontTransform()
                    .skipMemoryCache(true)
                    .transition(DrawableTransitionOptions.withCrossFade().crossFade(200))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            currentImageWidth = resource.getIntrinsicWidth();
                            currentImageHeight = resource.getIntrinsicHeight();

                            if(onFlipImageListener != null) {
                                onFlipImageListener.onFlip(FlipImageView.this, finalIndex);
                            }
                            return false;
                        }
                    })
                    .into(this);
            ++currentImageIndex;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility == View.VISIBLE) {
            startFlipping();
        } else {
            stopFlipping();
        }
    }

    public void startFlipping() {
        stopFlipping();
        if (imagePaths != null && imagePaths.size() > 1 && handler != null && flipRunnable != null) {
            isFlipping = true;
            handler.postDelayed(flipRunnable, flipDelay);
        }
    }

    public void stopFlipping() {
        if(handler != null && flipRunnable != null) {
            isFlipping = false;
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void setFlip(boolean isFlipping) {
        if(isFlipping) {
            startFlipping();
        } else {
            stopFlipping();
        }
    }

    public void setOnFlipImageListener(OnFlipImageListener onFlipImageListener) {
        this.onFlipImageListener = onFlipImageListener;
    }

    public int getCurrentImageWidth() {
        return currentImageWidth;
    }

    public int getCurrentImageHeight() {
        return currentImageHeight;
    }

    public interface OnFlipImageListener {
        void onFlip(@NonNull FlipImageView view, int index);
    }
}
