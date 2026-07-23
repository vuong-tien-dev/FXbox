package com.vtstudio.fxbox.fxviews.listview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.vtstudio.fxbox.R;

public class BorderLinearLayout extends LinearLayout {

    public static final int FLAG_BORDER_TOP = 1;
    public static final int FLAG_BORDER_BOTTOM = 2;
    public static final int FLAG_BORDER_LEFT = 4;
    public static final int FLAG_BORDER_RIGHT = 8;
    private float x, y;
    private Paint mPaint;
    private boolean borderTop, borderBottom, borderLeft, borderRight;

    public BorderLinearLayout(Context context) {
        super(context);
    }

    public BorderLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if(attrs == null) return;
        init(attrs, 0);
    }

    public BorderLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs == null) return;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        Log.d("BorderLinearLayout", "init");
        @SuppressLint({"Recycle", "CustomViewStyleable"}) final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.BorderLinearLayout, defStyleAttr, 0);
        final int flags = array.getInt(R.styleable.BorderLinearLayout_border, 0);

        borderTop = (flags & FLAG_BORDER_TOP) == FLAG_BORDER_TOP;
        borderBottom = (flags & FLAG_BORDER_BOTTOM) == FLAG_BORDER_BOTTOM;
        borderLeft = (flags & FLAG_BORDER_LEFT) == FLAG_BORDER_LEFT;
        borderRight = (flags & FLAG_BORDER_RIGHT) == FLAG_BORDER_RIGHT;

        Log.d("BorderLinearLayout", "top: " + borderTop + " bottom: " + borderBottom + " left: " + borderLeft + " right: " + borderRight);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getContext().getColor(R.color.rgb_240));

        setWillNotDraw(false);
        array.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(borderTop) {
            canvas.drawLine(x, y, x + getWidth(), y , mPaint);
        }
        if(borderBottom) {
            float height = getHeight();
            canvas.drawLine(x, y + height, x + getWidth(), y + height, mPaint);
        }
        if(borderLeft) {
            float height = getHeight();
            canvas.drawLine(x, y + height*0.2f, x, y + getHeight()*0.8f, mPaint);
        }
        if(borderRight) {
            float height = getHeight();
            canvas.drawLine(x + getWidth(), y + height*0.2f, x + getWidth(), y + height*0.8f, mPaint);
        }

    }
}
