package com.vtstudio.fxbox.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class UnitUtils {

    public static int dpToPixels(Context context, float dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }

    public static int pixelsToDp(Context context, int pixels) {
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        return (int) (pixels / scale);
    }
}
