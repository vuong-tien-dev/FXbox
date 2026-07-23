package com.vtstudio.fxbox.fxviews.textview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.vtstudio.fxbox.R

class VerifiableTextView @JvmOverloads constructor
    (context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0 ) : AppCompatTextView(context, attrs, defStyleAttr) {

    var isVerified = false
        @SuppressLint("UseCompatLoadingForDrawables")
        set(value) {
            if(value && !field) {
                val verifyDrw: Drawable? = context.getDrawable(R.drawable.verify);
//                val mPaint = paint;
//                val top: Int = mPaint.fontMetrics.top.toInt()
//                val side: Int = (mPaint.fontMetrics.bottom - top).toInt()
                verifyDrw?.setBounds(0, 6, 36, 42)
                compoundDrawablePadding = 10
                setCompoundDrawables(null, null, verifyDrw, null)
            } else if(!value && field) {
                compoundDrawablePadding = 0
                setCompoundDrawables(null, null, null, null)
            }
            field = value
        }

}