package com.vtstudio.fxbox.server.youtube;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.ApiModel;
import com.vtstudio.fxbox.server.BaseRequest;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;
import com.vtstudio.fxbox.server.tiktok.TikTokApi;

public abstract class YTBaseRequest<T, ModelType> extends BaseRequest<T, ModelType> {
    protected YoutubeApi api;

    protected YTBaseRequest(@NonNull YoutubeApi api) {
        this.api = api;
        onApiDestroyListener = this::onCancel;
        api.addOnApiDestroyListener(onApiDestroyListener);
    }

    protected void release() {
        if(this.api != null) {
            api.removeOnApiDestroyListener(onApiDestroyListener);
            this.api = null;
        }
    }

    protected abstract void executeRequest (@NonNull YoutubeApi api);
    public void get(){
        if(api == null) throw new NullPointerException("api is null at " + getClass());
        api.execute(() -> executeRequest(api));
    }

    protected void notifyOnResponse(@NonNull Response<ModelType> response) {
        if (this.onResponseListener != null && !isCanceled) {
            if(response.isSuccessfully()) {
                if(response.getModel() instanceof ApiModel) {
                    ((ApiModel) response.getModel()).setResponseServer(response.getServer());
                }
            }
            this.onResponseListener.onResponse(response);
        }
    }
}
