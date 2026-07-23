package com.vtstudio.fxbox.server;

import android.app.Service;
import android.content.Context;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.vtstudio.fxbox.listeners.FxLifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Api implements LifecycleEventObserver, FxLifecycle {

    // static fields
    private static final int TIMEOUT_SECONDS = 15;

    // instance fields
    protected Context context;
    private ApiCaller caller;
    private List<OnResponseListener.OnApiDestroyListener> onApiDestroyListeners;

    @NonNull
    Context requireContext () {
        if (context == null) {
            throw new NullPointerException("The context is null at " + getClass());
        }
        return context;
    }

    @Nullable
    public Context getContext() {
        return context;
    }

    protected Api (Context context, ApiCaller caller){
        this.context = context;
        this.caller = caller;
        okHttpClient = createOkHttpClient();
        onApiDestroyListeners = new ArrayList<>();

        if (context instanceof FragmentActivity) {
            ((FragmentActivity) context).getLifecycle().addObserver(this);
        } else if (context instanceof Service) {

        }
    }
    private OkHttpClient okHttpClient;

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    // call api feature methods
    protected static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    protected void postEvent (Runnable event){
        if(caller != null) caller.postEvent(event);
    }

    protected void execute (Runnable command){
        if(caller != null) caller.execute(command);
    }

    public Request newRequest(String nameKey, String nameHost, String url, String key, String host) {
        return new Request.Builder().url(url).get()
                .addHeader(nameKey, key)
                .addHeader(nameHost, host)
                .build();
    }

    @Override
    public void fxOnCreate() {
        Log.d("Response", "fxOnCreate of API");
    }

    @CallSuper
    @Override
    public void fxOnDestroy() {
        Log.d("Response", "fxOnDestroy of API");
        notifyOnApiDestroy();
        if(context != null) ((FragmentActivity) context).getLifecycle().removeObserver(this);
        onApiDestroyListeners.clear();
        this.okHttpClient = null;
        this.context = null;
        caller.remove(this);
        caller = null;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                // Xử lý sự kiện khi LifecycleOwner được tạo
                fxOnCreate();
                break;
            case ON_DESTROY:
                // Xử lý sự kiện khi LifecycleOwner bị hủy
                fxOnDestroy();
                break;
        }
    }

    public void addOnApiDestroyListener(@NonNull OnResponseListener.OnApiDestroyListener apiDestroyListener){
        postEvent(() -> onApiDestroyListeners.add(apiDestroyListener)); // run on ui thread
    }

    public void removeOnApiDestroyListener(@NonNull OnResponseListener.OnApiDestroyListener apiDestroyListener){
        postEvent(() -> onApiDestroyListeners.remove(apiDestroyListener)); // run on ui thread
    }

    public void notifyOnApiDestroy(){
        for (OnResponseListener.OnApiDestroyListener listener : onApiDestroyListeners){
            Log.d("Response", "notifyOnDestroy");
            listener.OnDestroy();
        }
    }

}
