package com.vtstudio.fxbox.server.tiktok;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.api.CommentDetails;
import com.vtstudio.fxbox.api.TiktokResponseModelsParser;
import com.vtstudio.fxbox.server.OnResponseListener;
import com.vtstudio.fxbox.server.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplyRequest extends TiktokBaseRequest<ReplyRequest, CommentDetails> {

    protected ReplyRequest(TikTokApi api) {
        super(api);
    }

    private static final int DEFAULT_OFFSET = 20;
    private int offset = DEFAULT_OFFSET;
    private int cursor = 0;

    public ReplyRequest offset(int offset) {
        this.offset = offset;
        return this;
    }

    public ReplyRequest cursor(int cursor) {
        this.cursor = cursor;
        return this;
    }

    @Override
    public void executeRequest(@NonNull TikTokApi api) {
        List<String> supportedSevers;
        List<String> apiKeys;
        Context context = api.getContext();
        if(context != null) {
            supportedSevers = TiktokApiPreferenceManager.sortServerByFailedCount(context, new ArrayList<>(Arrays.asList(TikTokApi.getReplySupportedServers())));
            apiKeys = TiktokApiPreferenceManager.getSortedApiKeysByFailedCount(context);
        } else {
            supportedSevers = new ArrayList<>(Arrays.asList(TikTokApi.getReplySupportedServers()));
            apiKeys = new ArrayList<>(Arrays.asList(TikTokApi.getApiKeys()));
        }

        if(url == null) throw new NullPointerException("url is null");

        Log.d("ReplyRequest", "url: " + url);
        for (String server : supportedSevers) {
            Log.d("ReplyRequest", "server: " + server);
        }

        for (String apiKey : apiKeys) {
            Log.d("ReplyRequest", "api key: " + apiKey);
        }
        String executedServer = "";

        for (String key : apiKeys) {
            for (String server : supportedSevers) {
                executedServer = server;
                if (isCanceled()) return;

                String body = api.callApi(TikTokApi.RAPID_API_NAME_KEY,
                        TikTokApi.RAPID_API_NAME_HOST,
                        TikTokApi.getReplyBaseUrl(server, url, offset, cursor),
                        key, TikTokApi.getApiHost(server));
                if (body != null) {
                    CommentDetails commentDetails = TiktokResponseModelsParser.parseCommentListTiktokApi(server, body);
                    if (commentDetails != null) {
                        release();
                        api.postEvent(() -> notifyOnResponse(new Response<>(commentDetails, true, server, null)));
                        return;
                    }
                }
            }
        }
        release();
        String finalExecutedServer = executedServer;
        api.postEvent(() -> notifyOnResponse(new Response<>(null, false, finalExecutedServer, new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL))));
    }
}
