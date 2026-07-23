package com.vtstudio.fxbox.server;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

public class BaseRequest<T, ModelType>{
    protected String[] url;
    protected String[] servers;
    protected volatile OnResponseListener.OnApiDestroyListener onApiDestroyListener;
    protected volatile boolean isCanceled = false;
    protected OnResponseListener<ModelType> onResponseListener;

    public T url(@NonNull String url) {
        this.url = new String [] {url};
        return self();
    }

    public T url(@NonNull String...url) {
        this.url = url;
        return self();
    }

    public T server(@NonNull String... server) {
        this.servers = server;
        return self();
    }

    public T callback(@NonNull OnResponseListener<ModelType> onResponseListener) {
        this.onResponseListener = onResponseListener;
        return self();
    }

    @CallSuper
    protected void onCancel(){
        onResponseListener = null;
        isCanceled = true;
    }

    protected boolean isCanceled () { return  isCanceled; }

    protected T self() {
        return (T) this;
    }
}
