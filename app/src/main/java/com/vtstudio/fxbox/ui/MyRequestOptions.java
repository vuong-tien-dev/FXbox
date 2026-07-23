package com.vtstudio.fxbox.ui;

import android.annotation.SuppressLint;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class MyRequestOptions {
    private static final RequestOptions options = buildRequestOptions();
    @SuppressLint("CheckResult")
    private static RequestOptions buildRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.skipMemoryCache(true);
        options.diskCacheStrategy(DiskCacheStrategy.NONE);
        options.format(DecodeFormat.PREFER_RGB_565);
        return options;
    }

    public static RequestOptions getOptions() {
        return options;
    }
}
