package com.vtstudio.fxbox.server.tiktok;

import android.content.Context;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.server.ApiCaller;
import com.vtstudio.fxbox.server.RapidApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TikTokApi extends RapidApi {
    // api key
    private static final String RAPID_API_HOST_TIKTOK_ALL_IN_ONE = "tiktok-all-in-one.p.rapidapi.com";
    private static final String RAPID_API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK = "tiktok-full-video-info-without-watermark.p.rapidapi.com";
    private static final String RAPID_API_HOST_TIKTOK_FULL_INFO = "tiktok-full-info-without-watermark.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_TIKTOK_VIDEO_FEATURE_SUMMARY = "tiktok-video-feature-summary.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_VIDEO_NO_WATERMARK = "tiktok-video-no-watermark2.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_TIKTOK_DOWNLOAD_VIDEO = "tiktok-download-video1.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_TIKTOK_MOBILE_VERSION = "tokapi-mobile-version.p.rapidapi.com";
    public static final String RAPID_API_API_HOST_TIKTOK_SOLUTIONS = "tiktok_solutions.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_VIDEO_NO_WATERMARK_10 = "tiktok-video-no-watermark10.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_TIKTOK_SCRAPPER = "tiktok-scraper7.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_TIKTOK_PRIVATE = "tiktok-private1.p.rapidapi.com";
    private static final String RAPID_API_API_HOST_TIKTOK_FACEOK = "tiktok89.p.rapidapi.com";

    // api constant
    public static final String API_TIKTOK_ALL_IN_ONE = "tiktok_all_in_one"; // 500/day // use for get user id and music id by awme_id
    public static final String API_VIDEO_NO_WATERMARK = "video_no_watermark"; // 300/month
    public static final String API_VIDEO_NO_WATERMARK_10 = "video_no_watermark_10";
    // 100/day
    public static final String API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK = "tiktok_downloader_full_info_no_watermark"; // 100/day use for get video details
    // 2000/month
    public static final String API_TIKTOK_VIDEO_FULL_INFO = "tiktok_video_full_info"; // 2000/moth // use for only video and aweme_id
    public static final String API_TIKTOK_VIDEO_FEATURE_SUMMARY = "tiktok_video_feature_summary"; // 1000/month // use for comment
    public static final String API_TIKTOK_DOWNLOAD_VIDEO_1 = "tiktok_download_video"; // 100/month use for reply comment
    public static final String API_TIKTOK_MOBILE_VERSION = "tiktok_mobile_version"; // about 100 - 200 / month multiple purposes
    public static final String API_TIKTOK_SOLUTIONS = "tiktok_solutions";
    public static final String API_TIKTOK_SCRAPPER = "tiktok_scrapper";
    public static final String API_TIKTOK_PRIVATE = "tiktok_private";
    public static final String API_TIKTOK_FACEOK = "tiktok_faceok";

    /// các phương thức này sẽ cho biết chức năng chuyên duụng của từng api trên web rapidapi
    private static final String[] commentSupportedServers = {
            API_TIKTOK_MOBILE_VERSION,
            API_TIKTOK_VIDEO_FEATURE_SUMMARY,
            API_VIDEO_NO_WATERMARK,
            API_TIKTOK_DOWNLOAD_VIDEO_1
    };

    private static final String[] videoSupportedServers = {
            API_TIKTOK_MOBILE_VERSION,
            API_TIKTOK_PRIVATE,
            API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK,
            API_TIKTOK_FACEOK,
    };

    private static final String[] replySupportedServers = {
            API_TIKTOK_MOBILE_VERSION,
    };

    private static final String[] userSupportedSevers = {
            API_TIKTOK_VIDEO_FEATURE_SUMMARY,
            API_TIKTOK_MOBILE_VERSION
    };

    protected static String getShortsVideoBaseUrl(String server, String url) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
                return "https://tiktok-all-in-one.p.rapidapi.com/video?id=" + url;
            case API_TIKTOK_VIDEO_FULL_INFO:
                return "https://tiktok-full-info-without-watermark.p.rapidapi.com/vid/index?url=" + url;
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
                return "https://tiktok-full-video-info-without-watermark.p.rapidapi.com/?url=" + url;
            case API_VIDEO_NO_WATERMARK:
                return "https://tiktok-video-no-watermark2.p.rapidapi.com/?url=" + url;
            case API_TIKTOK_MOBILE_VERSION:
                if(url.contains("http"))  return "https://tokapi-mobile-version.p.rapidapi.com/v1/post?video_url=" + url;
                else return "https://tokapi-mobile-version.p.rapidapi.com/v1/post/" + url;
            case API_TIKTOK_SOLUTIONS:
                return "https://tiktok_solutions.p.rapidapi.com/video/" + url;
            case API_VIDEO_NO_WATERMARK_10:
                return "https://tiktok-video-no-watermark10.p.rapidapi.com/index/Tiktok/getVideoInfo?url=" + url;
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
                return "https://tiktok-video-feature-summary.p.rapidapi.com/?url=" + url;
            case API_TIKTOK_SCRAPPER:
                return "https://tiktok-scraper7.p.rapidapi.com/?url=" +url;
            case API_TIKTOK_PRIVATE:
                return "https://tiktok-private1.p.rapidapi.com/post/" + url;
            case API_TIKTOK_FACEOK:
                return "https://tiktok89.p.rapidapi.com/tiktok?link=" + url;
        }
        return null;
    }

    protected static String getCommentBaseUrl(String server, String[] url, int offset, int cursor) {
        switch (server) {
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
                return "https://tiktok-video-feature-summary.p.rapidapi.com/comment/list?url=" + url[0] + "&count=" + offset + "&cursor=" + cursor;
            case API_VIDEO_NO_WATERMARK:
                return "https://tiktok-video-no-watermark2.p.rapidapi.com/comment/list?url=" + url[0] + "&count=" + offset + "&cursor=" + cursor;
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
                return "https://tiktok-download-video1.p.rapidapi.com/commentList?url=" + url[0] + "&count=" + offset + "&cursor=" + cursor;
        }
        return null;
    }

    protected static String getReplyBaseUrl(String server, String[] url, int offset, int cursor) {
        switch (server) {
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
                return "https://tiktok-download-video1.p.rapidapi.com/commentReply?comment_id=" + url[0] + "&count=" + offset + "&cursor=" + cursor;
            case  API_VIDEO_NO_WATERMARK:
                return "https://tiktok-video-no-watermark2.p.rapidapi.com/comment/reply?comment_id=" + url[0] + "&count="  + offset + "&cursor=" + cursor;
            case API_TIKTOK_MOBILE_VERSION:
                return "https://tokapi-mobile-version.p.rapidapi.com/v1/post/+" + url[0] + "/comment/" + url[1] + "/replies?count=" + offset + "&offset=" + cursor;
        }
        return null;
    }

    protected static String getUserBaseUrl (String server, String id){
        switch (server)
        {
            case API_TIKTOK_ALL_IN_ONE:
                return "https://tiktok-all-in-one.p.rapidapi.com/user?id=" + id;
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
                return "https://tiktok-video-feature-summary.p.rapidapi.com/user/info?user_id=" + id;
            case API_TIKTOK_MOBILE_VERSION:
                return "https://tokapi-mobile-version.p.rapidapi.com/v1/user/" + id;
        }
        return null;
    }

    public static String[] getSupportedVideoServers() {
        return videoSupportedServers;
    }
    public static String[] getSupportedCommentServers() {
        return commentSupportedServers;
    }
    public static String[] getReplySupportedServers() {
        return replySupportedServers;
    }
    public static String[] getUserSupportedSevers() { return userSupportedSevers; }
    @NonNull
    public static List<String> getAllServers() {
        return new ArrayList<>(Arrays.asList(
                API_TIKTOK_ALL_IN_ONE,
                API_VIDEO_NO_WATERMARK,
                API_VIDEO_NO_WATERMARK_10,
                API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK,
                API_TIKTOK_VIDEO_FULL_INFO,
                API_TIKTOK_VIDEO_FEATURE_SUMMARY,
                API_TIKTOK_DOWNLOAD_VIDEO_1,
                API_TIKTOK_MOBILE_VERSION,
                API_TIKTOK_SOLUTIONS,
                API_TIKTOK_SCRAPPER,
                API_TIKTOK_PRIVATE,
                API_TIKTOK_FACEOK
        ));
    }

    protected static String getApiHost(String server) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
                return RAPID_API_HOST_TIKTOK_ALL_IN_ONE;
            case API_VIDEO_NO_WATERMARK:
                return RAPID_API_API_HOST_VIDEO_NO_WATERMARK;
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
                return RAPID_API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK;
            case API_TIKTOK_VIDEO_FULL_INFO:
                return RAPID_API_HOST_TIKTOK_FULL_INFO;
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
                return RAPID_API_API_HOST_TIKTOK_VIDEO_FEATURE_SUMMARY;
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
                return RAPID_API_API_HOST_TIKTOK_DOWNLOAD_VIDEO;
            case API_TIKTOK_MOBILE_VERSION:
                return RAPID_API_API_HOST_TIKTOK_MOBILE_VERSION;
            case API_TIKTOK_SOLUTIONS:
                return RAPID_API_API_HOST_TIKTOK_SOLUTIONS;
            case API_VIDEO_NO_WATERMARK_10:
                return RAPID_API_API_HOST_VIDEO_NO_WATERMARK_10;
            case API_TIKTOK_SCRAPPER:
                return RAPID_API_API_HOST_TIKTOK_SCRAPPER;
            case API_TIKTOK_PRIVATE:
                return RAPID_API_API_HOST_TIKTOK_PRIVATE;
            case API_TIKTOK_FACEOK:
                return RAPID_API_API_HOST_TIKTOK_FACEOK;
            default:
                return null;
        }
    }
    protected static String[] getApiKeys() {
        return RAPID_API_KEYS;
    }

    public TikTokApi(Context context, ApiCaller caller) {
        super(context, caller);
        TiktokApiPreferenceManager.checkIntervalMonthAndClear(context);
    }

    public ShortsRequest asShorts() {
        return new ShortsRequest(this);
    }

    public CommentRequest asComment() {
        return new CommentRequest(this);
    }

    public ReplyRequest asReply() {
        return new ReplyRequest(this);
    }
    public UserRequest asUser() {return new UserRequest(this); }

    @Override
    protected void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    protected void postEvent(Runnable event) {
        super.postEvent(event);
    }
}
