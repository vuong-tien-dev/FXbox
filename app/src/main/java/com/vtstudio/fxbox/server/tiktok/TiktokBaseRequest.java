package com.vtstudio.fxbox.server.tiktok;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.ApiModel;
import com.vtstudio.fxbox.server.BaseRequest;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;

public abstract class TiktokBaseRequest<T, ModelType> extends BaseRequest<T, ModelType> {
    private TikTokApi api;

    protected TiktokBaseRequest(@NonNull TikTokApi api) {
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

    protected abstract void executeRequest (@NonNull TikTokApi api);
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
