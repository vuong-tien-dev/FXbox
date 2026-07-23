package com.vtstudio.fxbox.fxviews.progressbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.vtstudio.fxbox.R;

public class FxLoadingView extends View {
    private ValueAnimator mCircleAnimator;
    private Paint mPaintRed = new Paint();
    private Paint mPaintBlue = new Paint();
    private float mAnimatedValue = 0f;
    private float mPerDegreeDistance = MOVE_DISTANCE / MOVE_DISTANCE_DEGREE;
    private float mMovePositionA = 0f;
    private float mMovePositionB = 0f;
    private boolean mIsOddRotation = true;
    private float mMoveDegree = 0f;
    private float mRadiusA = CIRCLE_RADIUS;
    private float mRadiusB = CIRCLE_RADIUS;
    private float mPositionOffSet = MOVE_DISTANCE / 2;
    private float mAdjust = CIRCLE_RADIUS / (CIRCLE_WEIGHT_ADJUST + 1);
    private float mCircleWeightAdjust = CIRCLE_WEIGHT_ADJUST * mAdjust;
    private boolean isAnimating;
    private int mFirstCircleColor = Color.WHITE;
    private int mSecondCircleColor = Color.WHITE;
    public static final float CIRCLE_RADIUS = 22f;
    public static final float CIRCLE_WEIGHT_ADJUST = 5f;
    public static final long ANIMATION_DURATION = 1000L;
    public static final float MOVE_DISTANCE = 60f;
    public static final float MOVE_DISTANCE_DEGREE = 180f;
    public static final double RADIANS_90 = 1.5707963267948966;

    public FxLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFirstCircleColor = context.getColor(R.color.white);
        mSecondCircleColor = context.getColor(R.color.theme_green_teal_alt);
        initPaint();
    }

    public FxLoadingView(Context context) {
        super(context);
        mFirstCircleColor = context.getColor(R.color.white);
        mSecondCircleColor = context.getColor(R.color.theme_green_teal_alt);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (MOVE_DISTANCE * 2), (int) (CIRCLE_RADIUS * 2));
    }

    private void initPaint(){
        mPaintRed.setColor(mFirstCircleColor);
        mPaintBlue.setColor(mSecondCircleColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mMoveDegree = mAnimatedValue < MOVE_DISTANCE_DEGREE ?
                mAnimatedValue % MOVE_DISTANCE_DEGREE :
                MOVE_DISTANCE_DEGREE - mAnimatedValue % MOVE_DISTANCE_DEGREE;

        mMovePositionA = mPositionOffSet + mPerDegreeDistance * mMoveDegree;
        mMovePositionB = mPositionOffSet + MOVE_DISTANCE - mPerDegreeDistance * mMoveDegree;

        if (mIsOddRotation) {
            canvas.drawCircle(mMovePositionA, CIRCLE_RADIUS, mRadiusA, mPaintRed);
            canvas.drawCircle(mMovePositionB, CIRCLE_RADIUS, mRadiusB, mPaintBlue);
        } else {
            canvas.drawCircle(mMovePositionB, CIRCLE_RADIUS, mRadiusB, mPaintBlue);
            canvas.drawCircle(mMovePositionA, CIRCLE_RADIUS, mRadiusA, mPaintRed);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animateArch();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimate();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if(visibility == VISIBLE) {
            animateArch();
        }
        else {
            stopAnimate();
        }
    }

    private void stopAnimate(){
        if(isAnimating) {
            mCircleAnimator.cancel();
            isAnimating = false;
        }
    }

    private void animateArch() {
        if(isAnimating || getVisibility() != VISIBLE) return;

        mCircleAnimator = ValueAnimator.ofFloat(0f, 360f);
        mCircleAnimator.setDuration(ANIMATION_DURATION);
        mCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mCircleAnimator.setInterpolator(new LinearInterpolator());
        mCircleAnimator.addUpdateListener(animation -> {

            Log.d("FxLoadingView", "isAnimating");
            float newValue = (float) animation.getAnimatedValue();
            double radians = Math.toRadians(newValue);

            mRadiusA = (float) (Math.sin(radians) * mAdjust + mCircleWeightAdjust);
            mRadiusB = (float) (Math.cos(radians + RADIANS_90) * mAdjust + mCircleWeightAdjust);
            mAnimatedValue = newValue;

            if (mAnimatedValue == 0f || mAnimatedValue == 180f || mAnimatedValue == 360f) {
                return;
            }

            invalidate();
        });
        mCircleAnimator.start();
        isAnimating = true;
    }

    public void setFirstCircleColor(@ColorInt int color) {
        this.mFirstCircleColor = color;
        initPaint();
    }

    public void setSecondCircleColor(@ColorInt int color) {
        this.mSecondCircleColor = color;
        initPaint();
    }
}
