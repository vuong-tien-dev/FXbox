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

public class CommentRequest extends TiktokBaseRequest<CommentRequest, CommentDetails> {
    private static final int DEFAULT_OFFSET = 20;
    private int offset = DEFAULT_OFFSET;
    private int cursor = 0;
    private boolean isReply = false;

    protected CommentRequest(TikTokApi api) {
        super(api);
    }

    public CommentRequest offset(int offset) {
        this.offset = offset;
        return this;
    }

    public CommentRequest cursor(int cursor) {
        this.cursor = cursor;
        return this;
    }

    public CommentRequest asReply() {
        this.isReply = true;
        return this;
    }

    public CommentRequest isReply(boolean isReply) {
        this.isReply = isReply;
        return this;
    }

    @Override
    public void executeRequest(@NonNull TikTokApi api) {

        List<String> supportedSevers = isReply ? new ArrayList<>(Arrays.asList(TikTokApi.getReplySupportedServers())) : new ArrayList<>(Arrays.asList(TikTokApi.getSupportedCommentServers()));
        List<String> apiKeys = new ArrayList<>(Arrays.asList(TikTokApi.getApiKeys()));
        Context context = api.getContext();
        if(context != null) {
            supportedSevers = TiktokApiPreferenceManager.sortServerByFailedCount(context, supportedSevers);
            apiKeys = TiktokApiPreferenceManager.getSortedApiKeysByFailedCount(context);
        }

        String executedServer = "";
        for (String key : apiKeys) {

            if (isCanceled()) return;

            for (String server : supportedSevers) {
                executedServer = server;

                if (isCanceled()) return;

                String baseUrl = isReply ? TikTokApi.getReplyBaseUrl(server, url, offset, cursor) : TikTokApi.getCommentBaseUrl(server, url, offset, cursor);
                String body = api.callApi(TikTokApi.RAPID_API_NAME_KEY,
                        TikTokApi.RAPID_API_NAME_HOST,
                        baseUrl,
                        key, TikTokApi.getApiHost(server));

                if (body != null) {
                    Log.d("CommentRequest", baseUrl + ", " + server + ",Response body is not null, server is " + body);
                    CommentDetails commentDetails = TiktokResponseModelsParser.parseCommentListTiktokApi(server, body);
                    if (commentDetails != null) {
                        api.postEvent(() -> notifyOnResponse(new Response<>(commentDetails, true, server, null)));
                        release();
                        return;
                    } else if(context != null) {
                        TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                        TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                    }
                } else if(context != null) {
                    TiktokApiPreferenceManager.increaseFailedApiKeyCount(key, context);
                    TiktokApiPreferenceManager.increaseFailedServerCount(server, context);
                }
            }
        }
        String finalExecutedServer = executedServer;
        api.postEvent(() -> notifyOnResponse(new Response<>(null, false, finalExecutedServer, new OnResponseListener.Errol(OnResponseListener.Errol.DATA_ERROL))));
        release();
    }

}
