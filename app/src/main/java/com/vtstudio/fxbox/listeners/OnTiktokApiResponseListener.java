package com.vtstudio.fxbox.listeners;

import androidx.annotation.NonNull;

public interface OnTiktokApiResponseListener {
    default boolean onResponse(@NonNull String server , String body, boolean success, int modelsType, int tags) {return true;}
}
