package com.vtstudio.fxbox.fxviews.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

import com.vtstudio.fxbox.R;


public class GradientTextView extends TextView {

    private int fromGradientColor;
    private int toGradientColor;

    public GradientTextView(Context context) {
        super(context);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView);
        fromGradientColor = a.getColor(R.styleable.GradientTextView_fromGradient, Color.BLACK);
        toGradientColor = a.getColor(R.styleable.GradientTextView_toGradient, Color.WHITE);
        a.recycle();

    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Tạo một đối tượng Paint và tạo gradient với hai hoặc nhiều màu
        Paint paint = getPaint();
        LinearGradient gradient = new LinearGradient(0, 0, getWidth(), getHeight(),
                fromGradientColor, toGradientColor, Shader.TileMode.CLAMP);

        // Thiết lập đối tượng Paint để sử dụng gradient
        paint.setShader(gradient);

        // Vẽ đoạn text với gradient
        super.onDraw(canvas);
    }
}