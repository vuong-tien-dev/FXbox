package com.vtstudio.fxbox.server;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiCaller {
    private static ApiCaller instance;
    private ExecutorService executor;
    private static Handler eventPoster;
    private final List<Api> apiList;

    private ApiCaller (){
        apiList = Collections.synchronizedList(new ArrayList<>());
        executor = Executors.newSingleThreadExecutor();
        if (eventPoster == null) eventPoster = new Handler(Looper.getMainLooper());
    }

    public static OptionsBuilder with (Context context) {
        if(instance == null) instance = new ApiCaller();
        return new OptionsBuilder(context, instance);
    }

    protected void remove (Api api){
        if(apiList != null && api != null) {
            apiList.remove(api);

            if(apiList.isEmpty()){
                if(executor != null && !executor.isShutdown()) {
                    executor.shutdown();
                    executor = null;
                }

                if(eventPoster != null) {
                    eventPoster.removeCallbacksAndMessages(null);
                    eventPoster = null;
                }

                instance = null;
                Log.d("ApiCaller", "fxOnDestroy instance");
            }
        }
    }

    protected List<Api> getApiList (){ return apiList; }
    protected void execute (Runnable command){
        executor.execute(command);
    }

    protected void postEvent (Runnable runnable) {
        if (eventPoster != null) {
            eventPoster.post(runnable);
        }
    }
}

