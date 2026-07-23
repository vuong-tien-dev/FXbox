package com.vtstudio.fxbox.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.TooltipCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

object ViewsUtils {
    fun disableNavigationTooltips(bottomNavigationView: BottomNavigationView) {
        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val view =
                bottomNavigationView.findViewById<View>(bottomNavigationView.menu.getItem(i).itemId)
            TooltipCompat.setTooltipText(view, null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @JvmStatic
    fun setTouchScaleEffect(view: View, scale: Float) {
        if (!view.isClickable) {
            view.isClickable = true
        }
        view.setOnTouchListener { v, event ->
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                v.scaleX = scale
                v.scaleY = scale
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.scaleX = 1f
                v.scaleY = 1f
            }
            false
        }
    }

    @JvmStatic
    fun getItemIndexFromId(bottomNavigationView: BottomNavigationView, desiredItemId: Int): Int {
        val menu = bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            if (menuItem.itemId == desiredItemId) {
                return i
            }
        }
        // Trả về -1 nếu không tìm thấy
        return -1
    }

    @JvmStatic
    fun startFadeAnimWithFillAfter(view: View?, isFadeIn: Boolean) {
        if (view != null) {
            if (isFadeIn) {
                val animator = ValueAnimator.ofFloat(0.3f, 1f)
                animator.duration = 200
                animator.addUpdateListener { animation ->
                    view.alpha = animation.animatedValue as Float
                }
                animator.start()
            } else {
                val animator = ValueAnimator.ofFloat(1f, 0.3f)
                animator.duration = 200
                animator.addUpdateListener { animation ->
                    view.alpha = animation.animatedValue as Float
                }
                animator.start()
            }
        }
    }

    @JvmStatic
    fun makeVisibilityWithAnim(view: View?, isFadeIn: Boolean) {
        if (view != null) {
            if (isFadeIn) {
                val animator = ValueAnimator.ofFloat(0f, 1f)
                animator.duration = 200
                animator.addUpdateListener { animation ->
                    view.alpha = animation.animatedValue as Float
                }
                animator.start()
            } else {
                val animator = ValueAnimator.ofFloat(1f, 0f)
                animator.duration = 200
                animator.addUpdateListener { animation ->
                    view.alpha = animation.animatedValue as Float
                }
                animator.start()
            }
        }
    }

    @JvmStatic
    fun addRipple (view: View) = with (TypedValue()) {
        view.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
        view.setBackgroundResource(resourceId)
    }
}