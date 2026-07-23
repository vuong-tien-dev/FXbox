package com.vtstudio.fxbox.fxviews.listview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.adapters.ShortsListAdapter;
import com.vtstudio.fxbox.listeners.OnSnapVideoListener;

import java.util.concurrent.ExecutorService;

public class FxRecyclerView extends RecyclerView {

    private static final int POW = 5;
    public static final int SNAP_ON_SCROLLED = 0;
    public static final int SNAP_ON_RELEASE = 1;

    private Interpolator interpolator;
    private OnSnapVideoListener mOnSnapVideoListener;
    private OnScrollListener mOnScrollListener;
    private Runnable mOnSnapRunnable;
    private int currentPlayingIndex = -1;
    private int touchSlop;
    private float prevX;
    private boolean declined;
    private boolean smoothScrollEnabled = true;
    private int mSnapMode = SNAP_ON_SCROLLED;

    public FxRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FxRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FxRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void smoothScrollBy(int dx, int dy, @Nullable Interpolator interpolator) {

        if (!smoothScrollEnabled) {
            smoothScrollEnabled = true;
            return;
        }
        super.smoothScrollBy(dx, dy, interpolator);
    }

    @Override
    public void smoothScrollBy(int dx, int dy) {
        if (!smoothScrollEnabled) {
            smoothScrollEnabled = true;
            return;
        }
        super.smoothScrollBy(dx, dy);
    }

    @Override
    public void smoothScrollBy(int dx, int dy, @Nullable Interpolator interpolator, int duration) {
        if (!smoothScrollEnabled) {
            smoothScrollEnabled = true;
            return;
        }
        super.smoothScrollBy(dx, dy, interpolator, duration);
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @SuppressLint("Recycle")
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = MotionEvent.obtain(event).getX();
                declined = false; // New action
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - prevX);
                if (declined || xDiff > touchSlop) {
                    declined = true; // Memorize
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (declined) return true;
        }
        return super.onInterceptTouchEvent(event);
    }


    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    public void setOnSnapVideoListener(OnSnapVideoListener onSnapVideoListener) {
        this.mOnSnapVideoListener = onSnapVideoListener;
        if (mOnSnapVideoListener != null) {

            mOnScrollListener = buildOnScrollListener(mSnapMode);

            mOnSnapRunnable = buildSnapRunnable();
            addOnScrollListener(mOnScrollListener);
        } else {
            removeOnScrollListener(mOnScrollListener);
            mOnSnapVideoListener = null;
            mOnSnapRunnable = null;
            mOnScrollListener = null;
        }
    }

    private Runnable buildSnapRunnable() {
        return () -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();

            if (layoutManager == null) return;

            // Log.d("FxRecyclerView", "onSnapRunnable " + currentPlayingIndex);

            // Calculate the visible item positions
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

            ShortsListAdapter.ShortsItem firstsItem = ShortsListAdapter.getItemAt(FxRecyclerView.this, firstVisibleItemPosition);
            ShortsListAdapter.ShortsItem lastItem = ShortsListAdapter.getItemAt(FxRecyclerView.this, lastVisibleItemPosition);

            View firstVisibleView = null;

            if (firstsItem != null) {
                firstVisibleView = firstsItem.itemView;
            }

            View lastVisibleView = null;
            if (lastItem != null) {
                lastVisibleView = lastItem.itemView;
            }

            if (firstVisibleView == null || lastVisibleView == null) return;

            int firstVisibleHeight = firstVisibleView.getTop() > 0 ? FxRecyclerView.this.getBottom() - firstVisibleView.getTop() : FxRecyclerView.this.getBottom() + firstVisibleView.getTop();
            int lastVisibleHeight = lastVisibleView.getTop() > 0 ? FxRecyclerView.this.getBottom() - lastVisibleView.getTop() : FxRecyclerView.this.getBottom() + lastVisibleView.getTop();

            // Determine the video to play based on the visible item positions
            int videoToPlayIndex = firstVisibleHeight * 0.8 > lastVisibleHeight ? firstVisibleItemPosition : lastVisibleItemPosition;

            if (videoToPlayIndex == currentPlayingIndex || videoToPlayIndex < 0) return;

            // get item holder
            ShortsListAdapter.ShortsItem shortsItem = ShortsListAdapter.getItemAt(FxRecyclerView.this, videoToPlayIndex);

            if (shortsItem == null) {
                return;
            }

            if (mOnSnapVideoListener != null) {
                currentPlayingIndex = mOnSnapVideoListener.onSnap(shortsItem, videoToPlayIndex) ? videoToPlayIndex : currentPlayingIndex;
            }
        };
    }


    private OnScrollListener buildOnScrollListener(@IntRange(from = 0, to = 1) int snapMode) {
        if (snapMode == SNAP_ON_SCROLLED) {
            return new OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    enqueueOnSnapRunnable();
                }
            };
        }

        if (snapMode == SNAP_ON_RELEASE) {
            return new OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        enqueueOnSnapRunnable();
                    }
                }
            };
        }
        throw new RuntimeException("snap mode not valid");
    }

    public void setSnapMode(@IntRange(from = 0, to = 1) int snapMode) {
        mSnapMode = snapMode;
        if (mOnSnapVideoListener != null && mOnScrollListener != null) {
            mOnScrollListener = buildOnScrollListener(snapMode);
        }
    }

    private void enqueueOnSnapRunnable() {
        if(mOnSnapRunnable != null) mOnSnapRunnable.run();
    }

    public int getCurrentSnapIndex() {
        return currentPlayingIndex;
    }

    public void disableNextScrollBy() {
        smoothScrollEnabled = false;
    }

    public void cancelDisableNextScrollBy() {smoothScrollEnabled = true;}
}
