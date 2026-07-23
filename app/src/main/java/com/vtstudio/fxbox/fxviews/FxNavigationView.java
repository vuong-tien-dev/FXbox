package com.vtstudio.fxbox.fxviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vtstudio.fxbox.R;

public class FxNavigationView extends com.google.android.material.bottomnavigation.BottomNavigationView {

    private NavigationBarView.OnItemSelectedListener itemTouchEffectsListener;
    private BottomNavigationView.OnItemSelectedListener itemSelectedListener;
    private int currentSelectedItemId;
    private boolean effected;
    private Animation effect;
    private int colorWhenTouch;

    private int currentColor;
    private int selectedColor;
    private int unSelectedColor;

    private ColorStateList touchEffectStateList;

    int[][] states;

    public FxNavigationView(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        selectedColor = R.color.white_240;
        unSelectedColor = R.color.rgb_180;
        states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked},
        };
        setItemSelectedTint(selectedColor);
        setItemUnSelectedTint(unSelectedColor);

    }

    public FxNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FxNavigationView);
        init();
        initAttrs(a);
        a.recycle();
    }

    public FxNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FxNavigationView);
        init();
        initAttrs(a);
        a.recycle();
    }

    public FxNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FxNavigationView);
        init();
        initAttrs(a);
        a.recycle();
    }

    @SuppressLint("ResourceAsColor")
    private void initAttrs(TypedArray a) {
        colorWhenTouch = a.getColor(R.styleable.FxNavigationView_colorWhenTouch, R.color.colorWhenTouch);
        int[] colors = new int[]{
                getContext().getColor(colorWhenTouch),
                getContext().getColor(unSelectedColor)
        };
        touchEffectStateList = new ColorStateList(states, colors);
    }

    public void setItemsTouchEffect(boolean b) {
        effected = b;

        if (b) {
            if (itemTouchEffectsListener != null) {
                return;
            }

            effect = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_item_touch_anim);
            itemTouchEffectsListener = new OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    if (currentSelectedItemId == itemId) {
                        return true;
                    } else {
                        if (currentSelectedItemId != 0) {
                            findViewById(currentSelectedItemId).setOnTouchListener(null);
                        }

                        currentSelectedItemId = itemId;

                        View view = findViewById(itemId);
                        view.startAnimation(effect);
                        view.setOnTouchListener(new View.OnTouchListener() {
                            @SuppressLint("ClickableViewAccessibility")
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                int action = event.getActionMasked();
                                if (action == MotionEvent.ACTION_DOWN) {
                                    //Log.d("main", "onTouch Down");
                                    // Thực hiện animation và thay đổi màu cho MenuItem
                                    view.setScaleX(0.9f);
                                    view.setScaleY(0.9f);
                                    makeItemTintEffect();
                                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                                    // Thực hiện animation và thay đổi màu cho MenuItem
                                    //Log.d("main", "onTouch Up");
                                    view.setScaleX(1.f);
                                    view.setScaleY(1.f);
                                    setItemSelectedTint(selectedColor);
                                }
                                return true;
                            }
                        });
                    }
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onNavigationItemSelected(item);
                    }
                    return true;
                }
            };
            super.setOnItemSelectedListener(itemTouchEffectsListener);
        } else {
            itemTouchEffectsListener = null;
            super.setOnItemSelectedListener(itemSelectedListener);
        }
    }

    public void setItemSelectedTint(int color) {
        int[] colors = new int[]{
                getContext().getColor(color),
                getContext().getColor(unSelectedColor)
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setItemTextColor(colorStateList);
        setItemIconTintList(colorStateList);
        selectedColor = color;
    }

    private void makeItemTintEffect() {
        setItemTextColor(touchEffectStateList);
        setItemIconTintList(touchEffectStateList);
    }

    public void setItemUnSelectedTint(int color) {
        int[] colors = new int[]{
                getContext().getColor(selectedColor),
                getContext().getColor(color)
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        unSelectedColor = color;
    }

    @Override
    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {

        if (effected) {
            itemSelectedListener = listener;
        } else {
            super.setOnItemSelectedListener(listener);
        }
    }

    public void release (){
        effect = null;
        states = null;
    }

}
