package com.vtstudio.fxbox.server.youtube;

import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.media.models.youtube.YTVideoInfo;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;
import com.vtstudio.fxbox.server.tiktok.TikTokApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class YTRawDetailsRequest extends YTBaseRequest<YTRawDetailsRequest, YTVideoInfo>{
    protected YTRawDetailsRequest(@NonNull YoutubeApi api) {
        super(api);
    }

    @Override
    protected void executeRequest(@NonNull YoutubeApi api) {

        String[] supportedSevers = YoutubeApi.getDetailsSupportedSevers();
        String[] apiKeys = YoutubeApi.getApiKeys();

        String executedServer = "";

        for (String server : supportedSevers) {
            executedServer = server;
            for (String key : apiKeys) {

                if (isCanceled()) return;

                String baseUrl = YoutubeApi.getDetailsBaseUrl(server, url[0]);
                String body = api.callApi(YoutubeApi.RAPID_API_NAME_KEY,
                        YoutubeApi.RAPID_API_NAME_HOST,
                        baseUrl,
                        key, YoutubeApi.getApiHost(server));

                if (body != null) {

                    YTVideoInfo info = YoutubeResponseModelsParser.parseInfo(server, body);

                    if (info != null) {
                        api.postEvent(() -> notifyOnResponse(new Response<>(info, true, server, null)));
                        release();
                        return;
                    } else {
                        Log.d("Youtube", "info is null: " + body);
                    }
                } else {
                    Log.d("Youtube", "body is null");
                }
            }
        }

        String finalExecutedServer = executedServer;
        api.postEvent(() -> notifyOnResponse(new Response<>(null, false, finalExecutedServer, new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL))));

    }
}
