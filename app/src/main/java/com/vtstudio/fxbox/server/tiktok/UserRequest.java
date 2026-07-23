package com.vtstudio.fxbox.server.tiktok;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.api.TiktokResponseModelsParser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserRequest extends TiktokBaseRequest<UserRequest, ShortsUser> {
    protected UserRequest(TikTokApi api) {
        super(api);
    }

    @Override
    public void executeRequest(@NonNull TikTokApi api) {
        List<String> supportedSevers;
        List<String> apiKeys;
        Context context = api.getContext();
        if(context != null) {
            supportedSevers = TiktokApiPreferenceManager.sortServerByFailedCount(context, new ArrayList<>(Arrays.asList(TikTokApi.getUserSupportedSevers())));
            apiKeys = TiktokApiPreferenceManager.getSortedApiKeysByFailedCount(context);
        } else {
            supportedSevers = new ArrayList<>(Arrays.asList(TikTokApi.getUserSupportedSevers()));
            apiKeys = new ArrayList<>(Arrays.asList(TikTokApi.getApiKeys()));
        }
        String executedServer = "";

        for (String server : supportedSevers) {
            executedServer = server;
            for (String key : apiKeys) {

                if (isCanceled()) return;

                String baseUrl = TikTokApi.getUserBaseUrl(server, url[0]);
                        String body = api.callApi(TikTokApi.RAPID_API_NAME_KEY,
                        TikTokApi.RAPID_API_NAME_HOST,
                        baseUrl,
                        key, TikTokApi.getApiHost(server));
                if (body != null) {
                    ShortsUser user = TiktokResponseModelsParser.parseUserTiktokApi(server, body);
                    if (user != null) {
                        Log.d("User","Ok " + user.getUid());
                        api.postEvent(() -> notifyOnResponse(new Response<>(user, true, server, null)));
                        release();
                        return;
                    } else {
                        if(context != null) {
                            TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                            TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                        }
                        break;
                    }
                } else if(context != null) {
                    TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                    TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                }
            }
        }

        String finalExecutedServer = executedServer;
        api.postEvent(() -> notifyOnResponse(new Response<>(null, false, finalExecutedServer, new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL))));
    }
}
