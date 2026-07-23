package com.vtstudio.fxbox.ui;

import android.animation.ObjectAnimator;
import android.view.View;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class DownloadItemAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        // Áp dụng hiệu ứng mờ dần khi xóa item
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.start();

        return super.animateRemove(holder);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        // Áp dụng hiệu ứng trượt từ trái sang phải khi chèn item
        holder.itemView.setTranslationX(-holder.itemView.getWidth());
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_X, 0f);
        slideIn.setDuration(500);
        slideIn.start();

        return super.animateAdd(holder);
    }
}