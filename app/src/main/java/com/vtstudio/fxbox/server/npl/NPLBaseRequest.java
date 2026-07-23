package com.vtstudio.fxbox.server.npl;

import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;

public abstract class NPLBaseRequest <T, ModelType>{
    private NPLApi api;
    private volatile OnResponseListener.OnApiDestroyListener onApiDestroyListener;
    protected OnResponseListener<ModelType> onResponseListener;
    private volatile boolean isCanceled = false;

    protected NPLBaseRequest(NPLApi api){
        this.api = api;
        this.onApiDestroyListener = this::onCancel;
        api.addOnApiDestroyListener(onApiDestroyListener);
    }

    protected void release() {
        if(this.api != null) {
            api.removeOnApiDestroyListener(onApiDestroyListener);
            this.api = null;
        }
    }

    protected abstract void executeRequest (NPLApi api);
    public void get(){
        api.execute(() -> executeRequest(api));
    }

    protected void notifyOnResponse(Response<ModelType> response) {
        if (this.onResponseListener != null && !isCanceled) this.onResponseListener.onResponse(response);
    }

    public T callback(@NonNull OnResponseListener<ModelType> onResponseListener) {
        this.onResponseListener = onResponseListener;
        return self();
    }

    @CallSuper
    void onCancel (){
        Log.d("Response", "onRequestCancel");
        onResponseListener = null;
        isCanceled = true;
    }

    protected boolean isCanceled () { return  isCanceled; }

    protected T self () { return (T)this; }
}
