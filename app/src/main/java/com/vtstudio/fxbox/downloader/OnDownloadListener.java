package com.vtstudio.fxbox.downloader;

import androidx.annotation.NonNull;

public interface OnDownloadListener {
    default void onPrepare(RequestInfo requestInfo) {
    }

    default void onStart(RequestInfo requestInfo) {
    }

    default void onPause(@NonNull RequestInfo requestInfo) {
    }

    default void onResume(@NonNull RequestInfo requestInfo) {
    }

    default void onProgress(@NonNull RequestInfo requestInfo) {
    }

    default void onFailed(RequestInfo requestInfo) {
    }

    default void onInterruption(@NonNull RequestInfo requestInfo) {
    }

    default void onSuccess(@NonNull RequestInfo requestInfo) {
    }

    default void onNetworkErrol(@NonNull RequestInfo requestInfo) {

    }

    default void onCancel(@NonNull RequestInfo requestInfo) {

    }

   default void onDataChanged() {

   }
}