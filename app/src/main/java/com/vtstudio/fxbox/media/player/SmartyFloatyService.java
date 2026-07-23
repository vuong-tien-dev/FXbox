package com.vtstudio.fxbox.media.player;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.activity.FxBaseActivity;
import com.vtstudio.fxbox.databinding.VideoFloatingLayoutBinding;
import com.vtstudio.fxbox.fxviews.FlipImageView;

public class SmartyFloatyService extends Service implements
        View.OnTouchListener, View.OnClickListener {
    public static final int WINDOW_MOVE_THRESHOLD = 5;
    private static final String LOG_TAG = SmartyFloatyService.class.getSimpleName();
    private static final int CONTROLLER_SHOW_MILLIS = 4000;
    private boolean isDisabled = false;

    private WindowManager windowManager;
    private VideoFloatingLayoutBinding mBinding;
    private FrameLayout overlay;
    private WindowManager.LayoutParams params;
    private BroadcastReceiver broadcastReceiver;
    private boolean shouldIgnoreSuperOnStartCommand;
    private Point screenSize = new Point();

    /* onTouch vars */
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private Class<? extends FxBaseActivity> cls;
    private boolean isMovingWindow;
    private PlayerView playerView;
    private int controllerTimeKeep = 0;
    private boolean isOutsideXBounds;
    private boolean isOutsideYBounds;
    private int realWindowWidth;
    private int realWindowHeight;
    /* end */

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (!canInitializeFloatingWindow() || shouldIgnoreSuperOnStartCommand())
            return START_NOT_STICKY;

        Log.d("FxPlayer", "SmartyFloatyService onStartCommand ok");

        shouldIgnoreSuperOnStartCommand = true;

        this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        final LayoutInflater inflater = LayoutInflater.from(this);
        this.mBinding = VideoFloatingLayoutBinding.inflate(inflater);
        this.overlay = mBinding.getRoot();
        playerView = mBinding.floatingPlayerView;
        playerView.setControllerShowTimeoutMs(0);
        playerView.setOnTouchListener(this);
        onViewOverlayCreated(overlay);

        this.getScreenSize();
        setupWindowParams();
        showOverlay();
        setupBroadcastReceiver();

        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("FxPlayer", "onConfigurationChanged");
        getScreenSize();
        updateWindowSize();
    }

    void getScreenSize() {
        if (windowManager == null || screenSize == null) return;

        Display display = windowManager.getDefaultDisplay();
        display.getSize(screenSize);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        realWindowWidth = metrics.widthPixels;
        realWindowHeight = metrics.heightPixels;
        Log.d("FxPlayer", "getScreenSize: w: " + realWindowWidth + ", h: " + realWindowHeight);
        screenSize.x *= 0.85f;
        screenSize.y *= 0.85f;
    }

    protected float getPipWindowWidth() {
        return params == null ? 0 : params.width;
    }

    protected float getPipWindowHeight() {
        return params == null ? 0 : params.height;
    }

    protected boolean canInitializeFloatingWindow() {
        return false;
    }

    protected void waitController(int millis) {
        this.controllerTimeKeep = Math.min(millis + this.controllerTimeKeep, CONTROLLER_SHOW_MILLIS);
    }

    protected boolean shouldIgnoreSuperOnStartCommand() {
        return shouldIgnoreSuperOnStartCommand;
    }

    public static int getShowControllerMillis() {
        return CONTROLLER_SHOW_MILLIS;
    }

    protected void onViewOverlayCreated(@NonNull View view) {
    }

    @SuppressLint("RtlHardcoded")
    private void setupWindowParams() {
        final int desireWidth = getWindowWidth();
        final int desireHeight = getWindowHeight();
        params = new WindowManager.LayoutParams(
                computeDecideWidth(desireWidth, desireHeight),
                computeDecideHeight(desireWidth, desireHeight),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 50;
    }

    private int computeDecideWidth(int width, int height) {
        if (screenSize == null || screenSize.x < 50) return width;

        final float rational = width / ((float) height);
        final int mTarget = Math.min(screenSize.x, screenSize.y);
        int decidedWidth = width;
        if (rational > 1f) {
            decidedWidth = mTarget;
        } else if (rational < 1f) {
            decidedWidth = (int) (mTarget * rational);
        } else {
            decidedWidth = mTarget;
        }
        return decidedWidth;
    }

    private int computeDecideWidth(float rational) {
        if (screenSize == null || screenSize.x < 50) return params.width;

        final int mTarget = Math.min(screenSize.x, screenSize.y);
        int decidedWidth = params.width;
        if (rational > 1f) {
            decidedWidth = mTarget;
        } else if (rational < 1f) {
            decidedWidth = (int) (mTarget * rational);
        } else {
            decidedWidth = mTarget;
        }

        return decidedWidth;
    }

    private int computeDecideHeight(int width, int height) {
        if (screenSize == null || screenSize.x < 50) return height;

        final float rational = width / ((float) height);
        final int mTarget = Math.min(screenSize.x, screenSize.y);
        int decidedHeight = height;
        if (rational < 1f) {
            decidedHeight = mTarget;
        } else if (rational > 1f) {
            decidedHeight = (int) (mTarget * (1 / rational));
        } else {
            decidedHeight = mTarget;
        }
        return decidedHeight;
    }

    private int computeDecideHeight(float rational) {
        if (screenSize == null || screenSize.x < 50) return params.height;

        final int mTarget = Math.min(screenSize.x, screenSize.y);
        int decidedHeight = params.height;
        if (rational < 1f) {
            decidedHeight = mTarget;
        } else if (rational > 1f) {
            decidedHeight = (int) (mTarget * (1 / rational));
        } else {
            decidedHeight = mTarget;
        }

        return decidedHeight;
    }

    protected int getWindowWidth() {
        return 270;
    }

    protected int getWindowHeight() {
        return 480;
    }

    protected void computeWindowSize() {
        final int desireWidth = getWindowWidth();
        final int desireHeight = getWindowHeight();

        params.width = computeDecideWidth(desireWidth, desireHeight);
        params.height = computeDecideHeight(desireWidth, desireHeight);
    }

    protected void updateWindowSize() {
        if (windowManager == null || params == null || this.overlay == null) return;
        computeWindowSize();
        windowManager.updateViewLayout(this.overlay, params);
    }

    protected void updateWindowSizeWithAnim(int duration) {

        if (windowManager == null || params == null || this.overlay == null) return;

        final float oldWidth = params.width;
        final float oldHeight = params.height;
        final float oldScale = oldWidth / oldHeight;

        computeWindowSize();

        final float newScale = params.width / ((float) params.height);

        if(oldScale == newScale) return;

        PlayerView playerView = overlay.findViewById(R.id.floating_player_view);
        if (newScale >= 1) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        }

        Log.d("FxPlayer", "we have old width=" + oldWidth + ", old height=" + oldHeight);
        Log.d("FxPlayer", "we have new width=" + params.width + ", new height=" + params.height);

        @SuppressLint("Recycle") ValueAnimator valueAnimator = ValueAnimator.ofFloat(oldScale, newScale);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();

                if(overlay == null || params == null || windowManager == null) animation.cancel();

                params.width = computeDecideWidth(value);
                params.height = computeDecideHeight(value);
                windowManager.updateViewLayout(overlay, params);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                checkAndMoveIfOutside();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                checkAndMoveIfOutside();
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });

        valueAnimator.setDuration(Math.max(duration, 0));
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.start();
    }

//    protected void updateWindowSizeWithAnim(int duration) {
//
//        if (windowManager == null || params == null || this.overlay == null) return;
//
//        final int oldWidth = params.width;
//        final int oldHeight = params.height;
//
//        computeWindowSize();
//
//        final float newScale = params.width / ((float) params.height);
//
//        if(oldWidth == params.width && oldHeight == params.height) return;
//
//        PlayerView playerView = overlay.findViewById(R.id.floating_player_view);
//        if (newScale >= 1) {
//            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
//        } else {
//            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
//        }
//        Log.d("FxPlayer", "we have old width=" + oldWidth + ", old height=" + oldHeight);
//        Log.d("FxPlayer", "we have new width=" + params.width + ", new height=" + params.height);
//
//        @SuppressLint("Recycle") final ValueAnimator widthAnim =  ValueAnimator.ofInt(oldWidth, params.width);
//        widthAnim.addUpdateListener(animation -> {
//            if(params != null && windowManager != null && overlay != null) {
//                params.width = (int) animation.getAnimatedValue();
//                windowManager.updateViewLayout(overlay, params);
//            }
//        });
//
//        @SuppressLint("Recycle") final ValueAnimator heightAnim =  ValueAnimator.ofInt(oldHeight, params.height);
//        heightAnim.addUpdateListener(animation -> {
//            if(params != null && windowManager != null && overlay != null) {
//                params.height = (int) animation.getAnimatedValue();
//                windowManager.updateViewLayout(overlay, params);
//            }
//        });
//
//        final AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(widthAnim, heightAnim);
//        animatorSet.setInterpolator(new DecelerateInterpolator());
//        animatorSet.setDuration(duration);
//        animatorSet.start();
//    }

    private void moveWindow(int fromX, int fromY, int toX, int toY) {

        // Khởi tạo ValueAnimator cho tọa độ X
        ValueAnimator xAnimator = ValueAnimator.ofInt(fromX, toX);
        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.x = (int) animation.getAnimatedValue();

                if(overlay == null || params == null || windowManager == null) animation.cancel();

                windowManager.updateViewLayout(overlay, params);
            }
        });

        // Khởi tạo ValueAnimator cho tọa độ Y
        ValueAnimator yAnimator = ValueAnimator.ofInt(fromY, toY);
        yAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.y = (int) animation.getAnimatedValue();

                if(overlay == null || params == null || windowManager == null) animation.cancel();

                windowManager.updateViewLayout(overlay, params);
            }
        });

        // Kết hợp cả hai animator trong một AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(xAnimator, yAnimator);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    private void moveWindowX(int fromX, int toX) {
        // Khởi tạo ValueAnimator cho tọa độ X
        ValueAnimator xAnimator = ValueAnimator.ofInt(fromX, toX);
        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.x = (int) animation.getAnimatedValue();

                if(overlay == null || params == null || windowManager == null) animation.cancel();

                windowManager.updateViewLayout(overlay, params);
            }
        });
        xAnimator.setDuration(200);
        xAnimator.setInterpolator(new DecelerateInterpolator());
        xAnimator.start();
    }

    private void moveWindowY(int fromY, int toY) {
        // Khởi tạo ValueAnimator cho tọa độ X
        ValueAnimator xAnimator = ValueAnimator.ofInt(fromY, toY);
        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.y = (int) animation.getAnimatedValue();

                if(overlay == null || params == null || windowManager == null) animation.cancel();

                windowManager.updateViewLayout(overlay, params);
            }
        });
        xAnimator.setDuration(200);
        xAnimator.setInterpolator(new DecelerateInterpolator());
        xAnimator.start();
    }

    private void checkAndMoveIfOutside() {
        isOutsideXBounds = params.x < 0 || params.x + params.width > realWindowWidth;
        isOutsideYBounds = params.y < 0 || params.y + params.height > realWindowHeight;

        if (isOutsideXBounds && isOutsideYBounds) {
            moveWindow(params.x, params.y,
                    params.x <= 0 ? 0 : realWindowWidth - params.width,
                    params.y <= 0 ? 0 : realWindowHeight - params.height);
        } else if (isOutsideXBounds) {
            moveWindowX(params.x,
                    params.x <= 0 ? 0 : realWindowWidth - params.width);
        } else if (isOutsideYBounds) {
            moveWindowY(params.y,
                    params.y <= 0 ? 0 : realWindowHeight - params.height);
        }
    }

    protected void hideOverlay() {
        if (this.overlay.getWindowToken() != null) {
            windowManager.removeViewImmediate(this.overlay);
        }
    }

    protected void showOverlay() {
        if (this.overlay.getWindowToken() == null) {
            this.windowManager.addView(this.overlay, this.params);
        }
    }

    private void setupBroadcastReceiver() {
        if (Utility.isAccessibilityEnabled(this, SmartyFloatyAccessibilityService.ACCESSIBILITY_ID)) {
            broadcastReceiver = new SmartyFloatyBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(SmartyFloatyAccessibilityService.ACTION_DISABLE_FLOATING_VIDEO);
            intentFilter.addAction(SmartyFloatyAccessibilityService.ACTION_ENABLE_FLOATING_VIDEO);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(broadcastReceiver, intentFilter);
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlay != null) {
            if (overlay.getWindowToken() != null) {
                windowManager.removeViewImmediate(overlay);
            }
        }

        if(playerView != null) {
            playerView.setPlayer(null);
        }
        windowManager = null;
        mBinding = null;
        playerView = null;
        overlay = null;
        shouldIgnoreSuperOnStartCommand = false;
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    protected void setClass(Class<? extends FxBaseActivity> cls) {
        this.cls = cls;
    }

    protected Class<? extends FxBaseActivity> getRequestClass() {
        return this.cls;
    }

    protected void startTargetActivity() {
        if (cls != null) {
            Intent intent = new Intent(this, cls);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("FxPlayer", "onSingleTapConfirmed");
        View view = this.overlay.findViewById(R.id.video_floating_controller);
        if (view != null) {
            Log.d("FxPlayer", "onSingleTapConfirmed view is already");
            if (view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.GONE);
               if (playerView != null) playerView.hideController();
            } else {
                if (playerView != null) playerView.showController();
                view.setVisibility(View.VISIBLE);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (controllerTimeKeep == 0) {
                            view.setVisibility(View.GONE);
                            if (playerView != null) playerView.hideController();
                        } else {
                            final int time = controllerTimeKeep;
                            controllerTimeKeep = 0;
                            view.postDelayed(this, time);
                        }
                    }
                }, CONTROLLER_SHOW_MILLIS);
            }
        }
    }

    private class SmartyFloatyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SmartyFloatyAccessibilityService.ACTION_DISABLE_FLOATING_VIDEO)) {
                Log.d("FxPlayer", "ACTION_DISABLE_FLOATING_VIDEO");
                hideOverlay();
            } else if (intent.getAction().equals(SmartyFloatyAccessibilityService.ACTION_ENABLE_FLOATING_VIDEO)
                    && !isDisabled) {
                showOverlay();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Cập nhật vị trí của floating window
                final int distanceX = (int) (event.getRawX() - initialTouchX);
                final int distanceY = (int) (event.getRawY() - initialTouchY);
                final int newX = initialX + distanceX;
                final int newY = initialY + distanceY;

                if(Math.abs(newX - params.x) > WINDOW_MOVE_THRESHOLD || Math.abs(newY - params.y) > WINDOW_MOVE_THRESHOLD) {
                    params.x = newX;
                    params.y = newY;
                    windowManager.updateViewLayout(this.overlay, params);
                    isMovingWindow = true;
                    onWindowMoveStateChange(true);
                    Log.d("Floating", "on moving");
                    return true;
                }
              break;
            case MotionEvent.ACTION_UP:
                checkAndMoveIfOutside();
                Log.d("Floating", "on up");

                if (isMovingWindow) {
                    isMovingWindow = false;
                    onWindowMoveStateChange(false);
                    return true;
                } else {
                    if(playerView != null) {
                        playerView.performClick();
                    }
                    onClick(null);
                    return true;
                }
        }

//        if (detector != null) {
//            //boolean detectorResult = detector.onTouchEvent(event);
//            //Log.d("Floating", "onTouchEvent detector: " + detectorResult);
//        }

        return false;
    }


    protected void onWindowMoveStateChange(boolean isMoving) {

        if(overlay == null) return;
        FlipImageView view =  overlay.findViewById(R.id.video_floating_thumbnail);
        if (view == null) return;

        view.setFlip(!isMoving);
    }

}