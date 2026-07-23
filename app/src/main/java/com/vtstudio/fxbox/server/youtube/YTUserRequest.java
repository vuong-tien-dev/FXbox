package com.vtstudio.fxbox.server.youtube;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.api.TiktokResponseModelsParser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;
import com.vtstudio.fxbox.server.tiktok.TikTokApi;
import com.vtstudio.fxbox.server.tiktok.TiktokApiPreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YTUserRequest extends YTBaseRequest<YTUserRequest, YTUser> {

    protected YTUserRequest(@NonNull YoutubeApi api) {
        super(api);
    }

    @Override
    protected void executeRequest(@NonNull YoutubeApi api) {
        List<String> supportedSevers = Arrays.asList(YoutubeApi.getUserSupportedSevers());
        List<String> apiKeys = Arrays.asList(YoutubeApi.getApiKeys());

        String executedServer = "";

        for (String server : supportedSevers) {
            executedServer = server;
            for (String key : apiKeys) {

                if (isCanceled()) return;

                String baseUrl = YoutubeApi.getUserBaseUrl(server, url[0]);
                String body = api.callApi(YoutubeApi.RAPID_API_NAME_KEY,
                        YoutubeApi.RAPID_API_NAME_HOST,
                        baseUrl,
                        key, YoutubeApi.getApiHost(server));

                if (body != null) {
                    YTUser user = YoutubeResponseModelsParser.parseUser(server, body);
                    if (user != null) {
                        api.postEvent(() -> notifyOnResponse(new Response<>(user, true, server, null)));
                        release();
                        return;
                    }
                }
            }
        }

        String finalExecutedServer = executedServer;
        api.postEvent(() -> notifyOnResponse(new Response<>(null, false, finalExecutedServer, new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL))));

    }
}
