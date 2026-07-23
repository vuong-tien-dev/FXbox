package com.vtstudio.fxbox.fxviews.textview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RightAlignedSpan extends ReplacementSpan {
    private final String text;
    private final Paint paint;
    private final float textWidth;
    
    public RightAlignedSpan(String text, Paint paint) {
        this.text = text;
        this.paint = new Paint(paint);
        this.textWidth = paint.measureText(text);
    }
    
    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        return 0;
    }
    
    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        // Vẽ button ở góc phải
        canvas.drawText(this.text, canvas.getWidth() - textWidth - 16, y, this.paint);
    }
}
