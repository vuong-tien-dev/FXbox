package com.vtstudio.fxbox.server.youtube;

import android.content.Context;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.youtube.YTUser;
import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.server.RapidApi;

public class YoutubeApi extends RapidApi {

    protected static final String RAPID_API_HOST_YOUTUBE_V2_HOST = "youtube-v2.p.rapidapi.com";
    protected static final String RAPID_API_HOST_YOUTUBE_V3_HOST = "youtube-v31.p.rapidapi.com";

    public static final String API_YOUTUBE_V2 = "youtube_v2";
    public static final String API_YOUTUBE_V3 = "youtube_v3";

    private static final String[] userSupportedSevers = {
            API_YOUTUBE_V3,
            API_YOUTUBE_V2
    };

    private static final String[] detailsSupportedSevers = {
            API_YOUTUBE_V3
    };
    protected static String getApiHost(String server) {
        switch (server) {
            case API_YOUTUBE_V2:
                return RAPID_API_HOST_YOUTUBE_V2_HOST;
            case API_YOUTUBE_V3:
                return RAPID_API_HOST_YOUTUBE_V3_HOST;
            default:
                return null;
        }
    }

    protected static String getUserBaseUrl (String server, String id){
        switch (server)
        {
            case API_YOUTUBE_V2:
                return "https://youtube-v2.p.rapidapi.com/channel/details?channel_id=" + id;
            case API_YOUTUBE_V3:
                return "https://youtube-v31.p.rapidapi.com/channels?part=snippet%2Cstatistics&id=" + id;
        }
        return null;
    }

    protected static String getDetailsBaseUrl (String server, String id){
        switch (server)
        {
            case API_YOUTUBE_V3:
                return "https://youtube-v31.p.rapidapi.com/videos?part=contentDetails%2Csnippet%2Cstatistics&id=" + id;
        }
        return null;
    }

    public static String[] getUserSupportedSevers() { return userSupportedSevers; }
    public static String[] getDetailsSupportedSevers() { return detailsSupportedSevers; }
    protected static String[] getApiKeys() {
        return RAPID_API_KEYS;
    }
    public YoutubeApi(@NonNull Context context, @NonNull ApiCaller caller) {
        super(context, caller);
    }

    public YTUserRequest asUser () {
        return new YTUserRequest(this);
    }

    public YTRawDetailsRequest asDetails () {
        return new YTRawDetailsRequest(this);
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
