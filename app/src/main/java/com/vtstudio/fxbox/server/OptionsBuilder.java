package com.vtstudio.fxbox.server;

import android.content.Context;

import com.vtstudio.fxbox.server.npl.NPLApi;
import com.vtstudio.fxbox.server.tiktok.TikTokApi;
import com.vtstudio.fxbox.server.youtube.YoutubeApi;

import java.util.List;

public class OptionsBuilder {
    private Context context;
    private ApiCaller caller;

    protected OptionsBuilder (Context context, ApiCaller caller){
        this.context = context;
        this.caller = caller;
    }
    public TikTokApi asTikTokApi () {
        List<Api> apiList = caller.getApiList();

        for(Api api : apiList){
            if(api.context == context && api instanceof TikTokApi) return (TikTokApi) api;
        }

        TikTokApi api = new TikTokApi(context, caller);
        apiList.add(api);
        release();
        return api;
    }

    public NPLApi asNPLApi () {
        List<Api> apiList = caller.getApiList();

        for(Api api : apiList){
            if(api.context == context && api instanceof NPLApi) return (NPLApi) api;
        }

        NPLApi api = new NPLApi(context, caller);
        apiList.add(api);
        release();
        return api;
    }

    public YoutubeApi asYTApi () {
        List<Api> apiList = caller.getApiList();

        for(Api api : apiList){
            if(api.context == context && api instanceof YoutubeApi) return (YoutubeApi) api;
        }

        YoutubeApi api = new YoutubeApi(context, caller);
        apiList.add(api);
        release();
        return api;
    }

    private void release (){
        this.context = null;
        this.caller = null;
    }
}
