package com.vtstudio.fxbox.fxviews;

import android.animation.Animator;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.vtstudio.fxbox.R;

public class FxLottieView extends LottieAnimationView {

    private boolean animationRunning = false;
    private OnClickListener onClickListener;

    public FxLottieView(Context context) {
        super(context);
        disableClickWhenAnimating();
    }

    public FxLottieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        disableClickWhenAnimating();
    }

    public FxLottieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        disableClickWhenAnimating();
    }

    public void setLikeState(boolean like) {
        if (like) {
            setAnimation(R.raw.like_blue);
            LottieValueCallback<ColorFilter> colorFilterCallback = new LottieValueCallback<>();
            colorFilterCallback.setValue(new PorterDuffColorFilter(getContext().getColor(R.color.like_button_true), PorterDuff.Mode.SRC_ATOP));
            addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER, colorFilterCallback);
        } else {
            LottieValueCallback<ColorFilter> colorFilterCallback = new LottieValueCallback<>();
            colorFilterCallback.setValue(new PorterDuffColorFilter(getContext().getColor(R.color.rgb_240), PorterDuff.Mode.SRC_ATOP));
            addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER, colorFilterCallback);
        }
    }

    private void disableClickWhenAnimating(){
        addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                animationRunning = true;
                setOnClickListener(null);
                Log.d("FxLottie", "onAnimationStart");
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                animationRunning = false;
                setOnClickListener(onClickListener);
                Log.d("FxLottie", "onAnimationEnd");
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                animationRunning = false;
                setOnClickListener(onClickListener);
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
    }

    @Override
    public void playAnimation() {
        super.playAnimation();
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {

        if(animationRunning){
            super.setOnClickListener(null);
            return;
        }

        this.onClickListener = onClickListener;
        super.setOnClickListener(this.onClickListener);
    }
}
