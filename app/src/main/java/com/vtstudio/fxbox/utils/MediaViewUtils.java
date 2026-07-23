package com.vtstudio.fxbox.utils;

import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;

public class MediaViewUtils {
    public static void loadImageFromMedia (@NonNull Media media, @NonNull ImageView target, int widthDef, int heightDef){
        int width = target.getWidth();
        int height = target.getHeight();

        width = width > 0 ? width : widthDef;
        height = height > 0 ? height : heightDef;
        boolean isImageLoaded = false;
        if (media instanceof ShortsVideo) {
            ShortsVideo shortsVideo3 = (ShortsVideo) media;
            if (shortsVideo3.isImageList() && shortsVideo3.getImageListPath() != null) {
                Glide.with(target)
                        .load(shortsVideo3.getImageListPath()
                                .get(0))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(width, height)
                        .into(target).clearOnDetach();
                isImageLoaded = true;
            }
        }

        if(!isImageLoaded) {
            Glide.with(target).load(media.getPlayUri())
                    .override(width, height)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontAnimate()
                    .into(target).clearOnDetach();
            Log.d("MediaItemSearch", "is loading in playUri");
        }
    }
    public static void loadImageFromMedia (@NonNull Media media, @NonNull ImageView target){
        loadImageFromMedia(media, target, 100, 150);
    }
}
