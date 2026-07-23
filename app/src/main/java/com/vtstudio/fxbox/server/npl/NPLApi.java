package com.vtstudio.fxbox.server.npl;

import android.content.Context;

import com.vtstudio.fxbox.server.Api;
import com.vtstudio.fxbox.server.ApiCaller;

public class NPLApi extends Api {
    public NPLApi(Context context, ApiCaller caller) {
        super(context, caller);
    }

    public TextSegmentationRequest asSegmentationRequest() {
        return new TextSegmentationRequest(this);
    }

    @Override
    protected void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    protected void postEvent(Runnable event) {
        super.postEvent(event);
    }

}
