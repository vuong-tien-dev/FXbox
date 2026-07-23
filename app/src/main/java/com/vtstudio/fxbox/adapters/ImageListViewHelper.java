package com.vtstudio.fxbox.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.vtstudio.fxbox.databinding.ShortsListImageLayBinding;
import com.vtstudio.fxbox.fxviews.tools.OnZoomChangeListener;

import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class ImageListViewHelper {
    @SuppressLint("SetTextI18n")
    public static void bind(ViewGroup parent, ShortsListAdapter.ShortsItem holder, List<String> imageList, View.OnClickListener listener, OnZoomChangeListener onZoomChangeListener){
        Context context = parent.getContext();
        ShortsListImageLayBinding imgListBinding = ShortsListImageLayBinding.inflate(LayoutInflater.from(context), parent, false);
        ViewPager2 viewPager2 = imgListBinding.shortsImageListView;
        viewPager2.setOffscreenPageLimit(2);
        ShortsImageAdapter adapter = new ShortsImageAdapter(imageList);
        viewPager2.setAdapter(adapter);
        imgListBinding.shortsImageCount.setText(1 + " / " + adapter.getItemCount());

        CircleIndicator3 dotsIndicator = holder.binding.shortsContents.dotsIndicator;
        dotsIndicator.setVisibility(View.VISIBLE);
        //dotsIndicator.attachTo(viewPager2);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                imgListBinding.shortsImageCount.setText(position + 1 + " / " + adapter.getItemCount());
            }
        });

        viewPager2.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {

            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                viewPager2.setCurrentItem(0);
            }
        });

        viewPager2.setAdapter(adapter);
        adapter.setOnClickListener(listener);
        holder.binding.shortsImgListContainer.addView(imgListBinding.getRoot());

    }
}
