package com.vtstudio.fxbox.fxviews.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;

import com.vtstudio.fxbox.R;

public class TextExtraView extends AppCompatTextView {

    public static final int FLAG_GRAVITY_TOP = 1;
    public static final int FLAG_GRAVITY_BOTTOM = 2;
    public static final int FLAG_GRAVITY_LEFT = 4;
    public static final int FLAG_GRAVITY_RIGHT = 8;
    private String textExtra;
    private float textExtraWidth;

    public TextExtraView(@NonNull Context context) {
        super(context);
    }

    public TextExtraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(attrs == null) return;

        init(attrs, 0);
    }

    public TextExtraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs == null) return;

        init(attrs, defStyleAttr);
    }

    private void init(@NonNull AttributeSet attrs, int defStyleAttr){
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TextExtraView, defStyleAttr, 0);
        textExtra = array.getString(R.styleable.TextExtraView_textExtra);
        array.recycle();

        if(textExtra == null) return;

        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textExtraWidth = getPaint().measureText(textExtra);

        Drawable shape = AppCompatResources.getDrawable(getContext(), R.drawable.search_bar_shape);
        setBackground(shape);

        setPadding(getPaddingLeft(), getPaddingBottom(), (int) (textExtraWidth*1.4f), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(textExtra == null) return;

        canvas.drawText(textExtra, 0, textExtra.length(), getWidth() - getPaddingRight()*0.9f, getBaseline() , getPaint());
        canvas.drawLine(getWidth() - getPaddingRight(), getPaddingTop(),getWidth() - getPaddingRight(), getHeight() - getPaddingBottom(), getPaint() );
    }
}
