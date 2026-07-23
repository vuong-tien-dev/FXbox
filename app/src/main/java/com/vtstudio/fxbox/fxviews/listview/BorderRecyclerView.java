package com.vtstudio.fxbox.fxviews.listview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vtstudio.fxbox.R;

public class BorderRecyclerView extends RecyclerView {
    public static final int FLAG_BORDER_TOP = 1;
    public static final int FLAG_BORDER_BOTTOM = 2;
    public static final int FLAG_BORDER_LEFT = 4;
    public static final int FLAG_BORDER_RIGHT = 8;
    private float x, y;
    private Paint mPaint;
    private boolean borderTop, borderBottom, borderLeft, borderRight;

    public BorderRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if(attrs == null) return;
        init(attrs, 0);
    }

    public BorderRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(attrs == null) return;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

        @SuppressLint({"Recycle", "CustomViewStyleable"}) final TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.FxAttrs, defStyleAttr, 0);
        final int flags = array.getInt(R.styleable.FxAttrs_border, 0);

        borderTop = (flags & FLAG_BORDER_TOP) == FLAG_BORDER_TOP;
        borderBottom = (flags & FLAG_BORDER_BOTTOM) == FLAG_BORDER_BOTTOM;
        borderLeft = (flags & FLAG_BORDER_LEFT) == FLAG_BORDER_LEFT;
        borderRight = (flags & FLAG_BORDER_RIGHT) == FLAG_BORDER_RIGHT;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getContext().getColor(R.color.rgb_240));

        array.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(borderTop) {
            canvas.drawLine(x, y, x + getWidth(), y, mPaint);
        }
        if(borderBottom) {
            canvas.drawLine(x, y + getHeight(), x + getWidth(), y + getHeight(), mPaint);
        }
        if(borderLeft) {
            canvas.drawLine(x, y, x, y + getHeight(), mPaint);
        }
        if(borderRight) {
            canvas.drawLine(x + getWidth(), y, x + getWidth(), y + getHeight(), mPaint);
        }

    }

    public void setBorder (boolean top, boolean left, boolean bottom, boolean right) {
        this.borderTop = top;
        this.borderLeft = left;
        this.borderRight = right;
        this.borderBottom = bottom;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        x = l;
        y = t;
    }
}
