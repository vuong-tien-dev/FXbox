package com.vtstudio.fxbox.server.tiktok;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.api.TiktokResponseModelsParser;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortsRequest extends TiktokBaseRequest<ShortsRequest, TiktokPackage> {
    protected ShortsRequest(TikTokApi api) {
        super(api);
    }

    @Override
    public void executeRequest(@NonNull TikTokApi api) {

        List<String> supportedSevers = Arrays.asList(TikTokApi.getSupportedVideoServers());
        List<String> apiKeys;
        Context context = api.getContext();
        if(context != null) {
            apiKeys = TiktokApiPreferenceManager.getSortedApiKeysByFailedCount(context);
        } else {
            apiKeys = new ArrayList<>(Arrays.asList(TikTokApi.getApiKeys()));
        }
        String executedServer = "";

        if (this.servers != null && this.servers.length > 0) {
            // lấy danh sách servers yêu cầu từ developer
            for (String server : this.servers) {
                if (server != null && supportedSevers.contains(server)) {
                    executedServer = server;
                    for (String key : apiKeys) {
                        if (isCanceled()) return;
                        String link = TikTokApi.getShortsVideoBaseUrl(server, url[0]);
                        String host = TikTokApi.getApiHost(server);

                        String body = api.callApi(TikTokApi.RAPID_API_NAME_KEY,
                                TikTokApi.RAPID_API_NAME_HOST,
                                link,
                                key, host);

                        Log.d("Response", "Try: " + server);
                        if (body != null) {
                            TiktokPackage shorts = TiktokResponseModelsParser.parseTiktokApi(server, body);
                            if (shorts != null) {
                                Log.d("Response", "Ok");
                                api.postEvent(() -> notifyOnResponse(new Response<>(shorts, true, server, null)));
                                release();
                                return;
                            } else if(context != null) {
                                TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                                TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                            }
                        } else if(context != null) {
                            Log.d("Response", "Failed: " + server);

                            TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                            TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                        }
                    } // duyệt qua apis - end
                }
                // kiểm tra server hợp lệ - end
            }
            // lấy danh sách servers yêu cầu từ developer - end
        } else {

            for (String server : supportedSevers) {
                executedServer = server;
                for (String key : apiKeys) {

                    if (isCanceled()) return;

                    String link = TikTokApi.getShortsVideoBaseUrl(server, url[0]);
                    String host = TikTokApi.getApiHost(server);

                    String body = api.callApi(TikTokApi.RAPID_API_NAME_KEY,
                            TikTokApi.RAPID_API_NAME_HOST,
                            link,
                            key, host);

                    Log.d("Response", server);
                    if (body != null) {
                        TiktokPackage shorts = TiktokResponseModelsParser.parseTiktokApi(server, body);
                        Log.d("Response", body);
                        if (shorts != null) {
                            Log.d("Response", "Ok");
                            api.postEvent(() -> notifyOnResponse(new Response<>(shorts, true, server, null)));
                            release();
                            return;
                        } else if(context != null) {
                            TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                            TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                            break;
                        }
                    } else if(context != null) {
                        TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                        TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                    }
                }
            }
        }

        String finalExecutedServer = executedServer;
        api.postEvent(() -> notifyOnResponse(new Response<>(null, false, finalExecutedServer, new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL))));
        release();
    }
}
