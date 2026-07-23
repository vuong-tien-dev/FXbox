package com.vtstudio.fxbox.downloader;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;

public interface OnSaveModelCallback {
    boolean onSaveModel(@NonNull FXDownloader downloader, @NonNull FXDownloader.Request request);
}
