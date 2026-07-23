package com.vtstudio.fxbox.server;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class RapidApi extends Api {

    public static final String RAPID_API_NAME_KEY = "X-RapidAPI-Key";
    public static final String RAPID_API_NAME_HOST = "X-RapidAPI-Host";

    protected static final String[] RAPID_API_KEYS = com.vtstudio.fxbox.BuildConfig.RAPID_API_KEYS != null && com.vtstudio.fxbox.BuildConfig.RAPID_API_KEYS.length > 0
            ? com.vtstudio.fxbox.BuildConfig.RAPID_API_KEYS
            : new String[]{};


    protected RapidApi(@NonNull Context context,@NonNull ApiCaller caller) {
        super(context, caller);
    }

    public String callApi(String nameKey, String nameHost, String url, String key, String host) {

        if(url == null) {
            Log.d("Api", "url is null with server: " + host);
            return null;
        }

        Request request = new Request.Builder().get()
                .url(url)
                .addHeader(nameKey, key)
                .addHeader(nameHost, host)
                .build();
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    return response.body().string();
                }
            } else {
                String body = response.body() != null ? response.body().string() : "unknown";
                Log.d("Response", "Failed call rapid api: " + body);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
