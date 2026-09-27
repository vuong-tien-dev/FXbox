package com.vtstudio.fxbox.api;


import static com.vtstudio.fxbox.server.tiktok.TikTokApi.*;

import android.util.Log;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.SocialUserType;
import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.utils.ModelsParser;
import com.vtstudio.fxbox.models.TiktokPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TiktokResponseModelsParser {
    public static TiktokPackage parseTiktokApi(@NonNull String server, @NonNull String body) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
                return parseServerTiktokAllInOne(body);
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
            case API_TIKTOK_MOBILE_VERSION:
            case API_TIKTOK_PRIVATE:
            case  API_TIKTOK_FACEOK:
                Log.d("TiktokParser", "Start parsing by server: " + server);
                return parseServerTiktokDownloaderFullInfoNoWatermark(body);
            case API_TIKTOK_VIDEO_FULL_INFO:
                return parseServerTiktokFullInfo(body);
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
            case API_VIDEO_NO_WATERMARK:
            case API_TIKTOK_SOLUTIONS:
            case API_VIDEO_NO_WATERMARK_10:
            case API_TIKTOK_SCRAPPER:
                return parseServerTiktokFeatureSummary(body);
        }
        return null;
    }


    public static CommentDetails parseCommentListTiktokApi(@NonNull String server, @NonNull String body) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
                break;
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
            case API_VIDEO_NO_WATERMARK:
                return parseCommentListServerTiktokFeatureSummary(body, server);
            case API_TIKTOK_MOBILE_VERSION:
                return parseCommentListServerTiktokMobileVersion(body, server);
        }
        return null;
    }

    public static Comment parseComment(@NonNull String server, @NonNull String body) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
                break;
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
                break;
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
            case API_VIDEO_NO_WATERMARK:
                return parseCommentServerTiktokFeatureSummary(body);
        }
        return null;
    }

    public static Comment parseComment(@NonNull String server, @NonNull JSONObject body) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
                break;
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
                break;
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
            case API_VIDEO_NO_WATERMARK:
                return parseCommentServerTiktokFeatureSummary(body);
            case API_TIKTOK_MOBILE_VERSION:
                return parseCommentServerTiktokMobileVersion(body);
        }
        return null;
    }

    public static ShortsUser parseUserTiktokApi(@NonNull String server, @NonNull String body) {
        switch (server) {
            case API_TIKTOK_ALL_IN_ONE:
                break;
            case API_TIKTOK_DOWNLOADER_FULL_INFO_NO_WATERMARK:
                break;
            case API_TIKTOK_DOWNLOAD_VIDEO_1:
            case API_TIKTOK_VIDEO_FEATURE_SUMMARY:
                return parseUserServerTiktokFeatureSummary(body);
            case API_VIDEO_NO_WATERMARK:
                break;
            case API_TIKTOK_MOBILE_VERSION:
                return parseUserServerTiktokMobileVersion(body);
        }
        return null;
    }


    private static ShortsUser parseUserServerTiktokFeatureSummary(@NonNull String body) {
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {

            return null;
        }

        String author_avatar, author_nickname, author_uid, author_unique_id, author_signature, author_share_url;
        long total_favorited, following_count, follower_count;
        boolean verified;

        try {
            JSONObject dataObj = result.getJSONObject("data");
            JSONObject statusObj = dataObj.getJSONObject("stats");

            author_avatar = dataObj.getJSONObject("user").getString("avatarMedium");
            author_nickname = dataObj.getJSONObject("user").getString("nickname");
            author_uid = dataObj.getJSONObject("user").getString("id");
            author_unique_id = dataObj.getJSONObject("user").getString("uniqueId");
            author_signature = dataObj.getJSONObject("user").getString("signature");

            follower_count = statusObj.getLong("followerCount");
            following_count = statusObj.getLong("followingCount");
            total_favorited = statusObj.getLong("heartCount");

            verified = dataObj.getJSONObject("user").getBoolean("verified");

            Log.d("User", "NonNull body");
            ShortsUser user = new ShortsUser(author_uid, author_unique_id, author_nickname, null, author_signature, author_avatar, SocialUserType.USER_TIKTOK, follower_count, following_count, total_favorited);
            user.setVerified(verified);
            user.setTimeDownloaded(System.currentTimeMillis());
            user.setRawResponse(body);
            return user;
        } catch (JSONException e) {

            return null;
        }

    }

    private static ShortsUser parseUserServerTiktokMobileVersion(@NonNull String body) {
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {

            return null;
        }

        String author_avatar = null, author_nickname, author_uid, author_unique_id, author_signature, author_share_url;
        long total_favorited, following_count, follower_count;
        boolean verified;

        try {
            Log.d("UserResponse", "parseUserServerTiktokMobileVersion");
            JSONObject userObj = result.getJSONObject("user");
            JSONObject avtObj = userObj.getJSONObject("avatar_medium");
            JSONArray avtArrayObj = avtObj.getJSONArray("url_list");

            if (avtArrayObj.length() > 1) {
                author_avatar = avtArrayObj.getString(1);
            }

            if (author_avatar == null) return null;

            Log.d("UserResponse", "author_avatar not null");
            author_nickname = userObj.getString("nickname");
            author_uid = userObj.getString("uid");
            author_unique_id = userObj.getString("unique_id");
            author_signature = userObj.getString("signature");

            follower_count = userObj.getLong("follower_count");
            following_count = userObj.getLong("following_count");
            total_favorited = userObj.getLong("total_favorited");

            verified = !userObj.getString("custom_verify").isEmpty();

            Log.d("User", "NonNull body");
            ShortsUser user = new ShortsUser(author_uid, author_unique_id, author_nickname, null, author_signature, author_avatar, SocialUserType.USER_TIKTOK, follower_count, following_count, total_favorited);
            user.setVerified(verified);
            user.setTimeDownloaded(System.currentTimeMillis());
            user.setRawResponse(body);
            return user;
        } catch (JSONException e) {
            return null;
        }

    }

    private static TiktokPackage parseServerTiktokFullInfo(@NonNull String body) {
        String author_avatar, author_nickname, desc, selection_url, music_url, aweme_id, music_name, thumbnail;

        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {
            return null;
        }
        JSONArray musicArray = null;
        try {
            musicArray = result.getJSONArray("music");
            JSONArray videoArray = result.getJSONArray("video");
            JSONArray descriptionArray = result.getJSONArray("description");
            JSONArray avatarArray = result.getJSONArray("avatar_thumb");
            JSONArray authorArray = result.getJSONArray("author");
            JSONArray coverArray = result.getJSONArray("cover");
            JSONArray videoIdArray = result.getJSONArray("videoid");

            music_url = musicArray.getString(0);
            selection_url = videoArray.getString(0);
            desc = descriptionArray.getString(0);
            author_avatar = avatarArray.getString(0);
            author_nickname = authorArray.getString(0);
            thumbnail = coverArray.getString(0);
            aweme_id = videoIdArray.getString(0);
            music_name = "original sound - " + author_nickname + " - " + author_nickname;

            TiktokPackage api = new TiktokPackage();
            api.setTimeDownloaded(System.currentTimeMillis());
            api.setRawResponse(body);
            api.setMusicUrl(music_url);
            api.setAuthorAvatar(author_avatar);
            api.setDescription(desc);
            api.setMusicInformation(music_name);
            api.setUrl(selection_url);
            api.setThumbnail(thumbnail);
            api.setAwemeId(aweme_id);
            Log.d("TiktokPackage", "api created ");
            return api;
        } catch (JSONException e) {

        }
        Log.d("TiktokPackage", "api null ");
        return null;
    }

    private static TiktokPackage parseServerTiktokDownloaderFullInfoNoWatermark(@NonNull String body) {

        String author_avatar;
        String region;
        String author_nickname;
        StringBuilder desc;
        String video_url;
        String music_url = "";
        String aweme_id;
        String music_name;
        String thumbnail;
        String share_url;
        String musicAvatarUrl = null;
        String author_uid;
        String music_str_id;
        String author_unique_id;
        String music_author;
        String author_signature;
        String author_share_url;
        String desc_title = null;
        String capcut_template_id = null;
        String capcut_schema = null;
        String anchor_poi_id = null;
        String anchor_poi_key_word = null;
        boolean verified, is_warn = false;
        int warning_type = 0;
        long like, share, commentCount, total_favorited, following_count, follower_count, play_count, shorts_create_time;
        List<String> imageList = null;
        boolean isImageList = false;
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {
            Log.d("Response", "cannot parse json cause " + e.getMessage());
            return null;
        }

        try {
            JSONObject aweme_detail = result.getJSONObject("aweme_detail");
            JSONArray video_url_array = aweme_detail.getJSONObject("video").getJSONObject("play_addr")
                    .getJSONArray("url_list");
            video_url = video_url_array.get(video_url_array.length() > 1 ? 1 : 0).toString();
//            video_url = result.getString("hdplay");
            desc = new StringBuilder(aweme_detail.getString("desc"));
            aweme_id = aweme_detail.getString("aweme_id");
            shorts_create_time = aweme_detail.getLong("create_time");

            Log.d("Response", "Parse 1 ");

            region = aweme_detail.getString("region");
            JSONArray music_url_array = aweme_detail.getJSONObject("music").getJSONObject("play_url").getJSONArray("url_list");
            if(music_url_array.length() > 0) music_url = music_url_array.getString(0);
            thumbnail = aweme_detail.getJSONObject("video").getJSONObject("cover").getJSONArray("url_list").getString(0);
            author_avatar = aweme_detail.getJSONObject("author").getJSONObject("avatar_medium").getJSONArray("url_list").getString(0);
            author_nickname = aweme_detail.getJSONObject("author").getString("nickname");
            author_signature = aweme_detail.getJSONObject("author").getString("signature");
            music_author = aweme_detail.getJSONObject("music").getString("author");
            author_unique_id = aweme_detail.getJSONObject("author").getString("unique_id");
            verified = !aweme_detail.getJSONObject("author").optString("custom_verify", "").isEmpty();
            music_name = aweme_detail.getJSONObject("music").getString("title");
            share_url = aweme_detail.getString("share_url");
            like = aweme_detail.getJSONObject("statistics").getLong("digg_count");
            commentCount = aweme_detail.getJSONObject("statistics").getLong("comment_count");
            share = aweme_detail.getJSONObject("statistics").getLong("share_count");
            JSONObject musicCoverObj = aweme_detail.getJSONObject("music").optJSONObject("cover_medium");
            if(musicCoverObj != null) musicAvatarUrl = musicCoverObj.getJSONArray("url_list").getString(0);
            author_uid = aweme_detail.getJSONObject("author").getString("uid");
            music_str_id = aweme_detail.getJSONObject("music").getString("id_str");
            author_share_url = aweme_detail.getJSONObject("author").getJSONObject("share_info").getString("share_url");
            follower_count = aweme_detail.getJSONObject("author").getLong("follower_count");
            following_count = aweme_detail.getJSONObject("author").getLong("following_count");
            total_favorited = aweme_detail.getJSONObject("author").getLong("total_favorited");
            play_count = aweme_detail.getJSONObject("statistics").getLong("play_count");

            Log.d("Response", "Parse 2 ");

            JSONObject riskObj = aweme_detail.optJSONObject("risk_infos");
            if (riskObj != null) {
                is_warn = riskObj.optBoolean("warn", false);
                final String warning_message = riskObj.optString("content");
                if(warning_message.toLowerCase().contains("viewer discretion is advised")) {
                    warning_type = ShortsVideo.WARNING_TYPE_BEFORE_VIEWING;
                } else if(warning_message.toLowerCase().contains("do not attempt")) {
                    warning_type = ShortsVideo.WARNING_TYPE_DO_NOT_ATTEMPT;
                }
            }

            Log.d("Response", "Parse 3 ");

            // kiểm tra video capcut
            JSONArray anchorsArrObj = aweme_detail.optJSONArray("anchors");
            if (anchorsArrObj != null && anchorsArrObj.length() > 0) {
                for (int i = 0; i < anchorsArrObj.length(); i++) {
                    JSONObject anchorTargetObj = anchorsArrObj.optJSONObject(i);
                    if (anchorTargetObj != null) {
                        String anchorDesc = anchorTargetObj.optString("component_key");
                        if (anchorDesc.toLowerCase().contains("capcut")) ;
                        {
                            String logExtraString = anchorTargetObj.optString("log_extra");

                            try {
                                JSONObject logExtraObj = new JSONObject(logExtraString);
                                capcut_template_id = logExtraObj.optString("template_id");
                            } catch (JSONException e) {

                            }

                            capcut_schema = anchorTargetObj.optString("schema");
                        }

                        if(anchorDesc.contains("anchor_poi")) {
                            String logExtraString = anchorTargetObj.optString("log_extra");

                            try {
                                JSONObject logExtraObj = new JSONObject(logExtraString);
                                anchor_poi_id = logExtraObj.optString("anchor_id");
                                anchor_poi_key_word = logExtraObj.optString("anchor_name");
                            } catch (JSONException e) {

                            }
                        }
                    }
                }
            }

            Log.d("Response", "Parse 4 ");
            // lấy content desc có dấu xuống dòng
            String extraDescription = aweme_detail.optString("content_desc");
            try {
                JSONArray stringJSONArray = new JSONArray(extraDescription);
                if (stringJSONArray.length() > 0) {
                    desc = new StringBuilder();
                    for (int i = 0; i < stringJSONArray.length(); ++i) {
                        desc.append(stringJSONArray.get(i));
                        if (i < stringJSONArray.length() - 1) {
                            desc.append("\n");
                        }
                    }
                }
            } catch (JSONException ignored) {
                Log.d("TiktokParser", "extraDescription exception");
            }

            // lấy danh sách hình ảnh nếu có
            JSONObject image_post_obj = aweme_detail.optJSONObject("image_post_info");
            if (image_post_obj != null) {
                JSONArray image_post_list = image_post_obj.optJSONArray("images");
                Log.d("Response", "exists img");
                if (image_post_list != null && image_post_list.length() > 0) {
                    imageList = new ArrayList<>();
                    for (int i = 0; i < image_post_list.length(); ++i) {
                        JSONObject item = image_post_list.optJSONObject(i);
                        if (item != null) {
                            JSONObject image_list = item.optJSONObject("display_image");
                            if (image_list != null) {
                                JSONArray images_url = image_list.optJSONArray("url_list");
                                if (images_url != null && images_url.length() > 0) {
                                    Log.d("Response", "image_list not null");
                                    String url = images_url.getString(0);
                                    if (url != null) imageList.add(url);
                                    Log.d("Response", url);
                                }
                            }
                        } // end check each item not null
                    } // end of for loop
                    desc_title = image_post_obj.optString("title");
                } // end check null and zero values
            }

            isImageList = imageList != null && imageList.size() > 0;
            //////
            TiktokPackage api = new TiktokPackage();
            api.setTimeDownloaded(System.currentTimeMillis());
            api.setCapcutSchema(capcut_schema);
            api.setAnchorPosId(anchor_poi_id);
            api.setAnchorPosKeyword(anchor_poi_key_word);
            api.setWarningType(warning_type);
            api.setCapcutTemplateId(capcut_template_id);
            api.setWarnAttempt(is_warn);
            api.setDescriptionTitle(desc_title);
            api.setVerified(verified);
            api.setRawResponse(body);
            api.setImageListUrl(imageList);
            api.setIsImageList(isImageList);
            api.setRegion(region);
            api.setShortsCreateTime(shorts_create_time);
            api.setAuthorShareUrl(author_share_url);
            api.setAuthorSignature(author_signature);
            api.setMusicAuthor(music_author);
            api.setAuthorUniqueId(author_unique_id);
            api.setUrl(video_url);
            api.setPlayCount(play_count);
            api.setDescription(desc.toString());
            api.setAwemeId(aweme_id);
            api.setMusicUrl(music_url);
            api.setThumbnail(thumbnail);
            api.setAuthorAvatar(author_avatar);
            api.setAuthorNickname(author_nickname);
            api.setMusicInformation(music_name);
            api.setShareUrl(share_url);
            api.setLikeCount(like);
            api.setCommentCount(commentCount);
            api.setShareCount(share);
            api.setMusicAvatarUrl(musicAvatarUrl);
            api.setFxMusicAvatarId(music_str_id);
            api.setFxAuthorAvatarId(author_uid);
            api.setTotalFavorited(total_favorited);
            api.setAuthorFollower(follower_count);
            api.setAuthorFollowingCount(following_count);
            Log.d("Parser", "id: " + aweme_id);
            return api;

        } catch (JSONException e) {
            Log.e("TiktokParser", "Missing required field in FullInfoNoWatermark: " + e.getMessage());
            return null;
        }
    }

    private static TiktokPackage parseServerTiktokFeatureSummary(@NonNull String body) {
        String author_avatar, author_nickname, region, desc, video_url, music_url, aweme_id, music_name, thumbnail, musicAvatarUrl, author_uid, music_str_id, author_unique_id, music_author;
        List<String> imageList = null;
        long like, share, commentCount, play_count, shorts_create_time;
        boolean isImageList = false;
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {
            return null;
        }
        try {
            JSONObject dataObj = result.getJSONObject("data");

            region = dataObj.getString("region");
            shorts_create_time = dataObj.getLong("create_time");

            author_avatar = dataObj.getJSONObject("author").getString("avatar");
            author_nickname = dataObj.getJSONObject("author").getString("nickname");
            author_uid = dataObj.getJSONObject("author").getString("id");
            author_unique_id = dataObj.getJSONObject("author").getString("unique_id");

            desc = dataObj.getString("title");
            aweme_id = dataObj.getString("id");
            thumbnail = dataObj.getString("cover");
            video_url = dataObj.getString("play");

            play_count = dataObj.getLong("play_count");
            like = dataObj.getLong("digg_count");
            share = dataObj.getLong("share_count");
            commentCount = dataObj.getLong("comment_count");

            music_url = dataObj.getJSONObject("music_info").getString("play");
            music_name = dataObj.getJSONObject("music_info").getString("title");
            musicAvatarUrl = dataObj.getJSONObject("music_info").getString("cover");
            music_str_id = dataObj.getJSONObject("music_info").getString("id");
            music_author = dataObj.getJSONObject("music_info").getString("author");

            JSONArray imgObjArray = dataObj.optJSONArray("images");

            Log.d("Response", "get img");

            if (imgObjArray != null) {
                Log.d("Response", "exists img");
                imageList = new ArrayList<>();
                for (int i = 0; i < imgObjArray.length(); ++i) {
                    imageList.add(imgObjArray.getString(i));
                }
                isImageList = true;
            }

            TiktokPackage api = new TiktokPackage();
            api.setTimeDownloaded(System.currentTimeMillis());
            api.setShortsCreateTime(shorts_create_time);
            api.setRawResponse(body);
            api.setImageListUrl(imageList);
            api.setRegion(region);
            api.setIsImageList(isImageList);
            api.setMusicAuthor(music_author);
            api.setAuthorUniqueId(author_unique_id);
            api.setUrl(video_url);
            api.setPlayCount(play_count);
            api.setDescription(desc);
            api.setAwemeId(aweme_id);
            api.setMusicUrl(music_url);
            api.setThumbnail(thumbnail);
            api.setAuthorAvatar(author_avatar);
            api.setAuthorNickname(author_nickname);
            api.setMusicInformation(music_name);
            api.setLikeCount(like);
            api.setCommentCount(commentCount);
            api.setShareCount(share);
            api.setMusicAvatarUrl(musicAvatarUrl);
            api.setFxMusicAvatarId(music_str_id);
            api.setFxAuthorAvatarId(author_uid);
            return api;
        } catch (JSONException e) {
            return null;
        }
    }

    private static CommentDetails parseCommentListServerTiktokFeatureSummary(@NonNull String body, String server) {
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {
            Log.d("CommentRequest", "Cannot parse json response: " + e.getMessage());
            return null;
        }
        try {
            JSONObject dataObj = result.getJSONObject("data");
            JSONArray commentArray = dataObj.getJSONArray("comments");

            List<Comment> commentList = new ArrayList<>();

            for (int i = 0; i < commentArray.length(); ++i) {
                Comment comment = parseComment(server, commentArray.getJSONObject(i));
                if (comment != null) {
                    ModelsParser.setServerFromResponse(comment, server);
                    commentList.add(comment);
                } else {
                    Log.d("CommentRequest", "Comment is null at index: " + i);
                    return null;
                }
            }

            int total = dataObj.getInt("total");
            int cursor = dataObj.getInt("cursor");
            boolean hasMore = dataObj.getBoolean("hasMore");

            CommentDetails details = new CommentDetails();
            details.setTimeDownloaded(System.currentTimeMillis());
            details.setComments(commentList);
            details.setCursor(cursor);
            details.setTotal(total);
            details.setHasMore(hasMore);

            return details;
        } catch (JSONException e) {
            return null;
        }
    }

    private static CommentDetails parseCommentListServerTiktokMobileVersion(@NonNull String body, @NonNull String server) {
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {
            Log.d("CommentRequest", "Cannot parse json response: " + e.getMessage());
            return null;
        }

        try {
            JSONArray commentArray = result.getJSONArray("comments");

            List<Comment> commentList = new ArrayList<>();

            for (int i = 0; i < commentArray.length(); ++i) {
                Comment comment = parseComment(server, commentArray.getJSONObject(i));
                if (comment != null) {
                    ModelsParser.setServerFromResponse(comment, server);
                    commentList.add(comment);
                } else {
                    Log.d("CommentRequest", "Comment is null at index: " + i);
                    return null;
                }
            }

            int total = result.getInt("total");
            int cursor = result.getInt("cursor");
            boolean hasMore = result.optInt("has_more") != 0;

            CommentDetails details = new CommentDetails();
            details.setTimeDownloaded(System.currentTimeMillis());
            details.setComments(commentList);
            details.setCursor(cursor);
            details.setTotal(total);
            details.setHasMore(hasMore);

            return details;
        } catch (JSONException e) {
            return null;
        }
    }
    private static Comment parseCommentServerTiktokFeatureSummary(@NonNull String body) {
        JSONObject commentObj = null;
        try {
            commentObj = new JSONObject(body);
            String id = commentObj.getString("id");
            String content = commentObj.getString("text");
            long createTime = commentObj.getLong("create_time");
            int diggCount = commentObj.getInt("digg_count");
            int replyCount = commentObj.optInt("reply_total");

            JSONObject userObj = commentObj.getJSONObject("user");

            String author_avatar, author_nickname, author_uid, author_unique_id, author_signature;
            long total_favorited, following_count, follower_count;

            author_uid = userObj.getString("id");
            author_avatar = userObj.getString("avatar");
            author_nickname = userObj.getString("nickname");
            author_signature = userObj.getString("signature");
            author_unique_id = userObj.getString("unique_id");
            follower_count = userObj.getLong("follower_count");
            following_count = userObj.getLong("favoriting_count");
            total_favorited = userObj.getLong("total_favorited");

            ShortsUser user = new ShortsUser(author_uid, author_unique_id, author_nickname, null, author_signature, author_avatar, SocialUserType.USER_TIKTOK, follower_count, following_count, total_favorited);
            user.setRawResponse(userObj.toString());
            Comment comment = new Comment(id, content, author_uid, null, replyCount, createTime, diggCount, false, false, null, null);
            comment.setShortsUser(user);
            comment.setTimeDownloaded(System.currentTimeMillis());
            comment.setRawResponse(body);
            return comment;
        } catch (JSONException e) {
            return null;
        }
    }

    private static Comment parseCommentServerTiktokFeatureSummary(@NonNull JSONObject commentObj) {
        try {
            String id = commentObj.getString("id");
            String content = commentObj.getString("text");
            long createTime = commentObj.getLong("create_time");
            int diggCount = commentObj.getInt("digg_count");
            int replyCount = commentObj.optInt("reply_total");

            JSONObject userObj = commentObj.getJSONObject("user");

            String author_avatar, author_nickname, author_uid, author_unique_id, author_signature;
            long total_favorited, following_count, follower_count;

            author_uid = userObj.getString("id");
            author_avatar = userObj.getString("avatar");
            author_nickname = userObj.getString("nickname");
            author_signature = userObj.getString("signature");
            author_unique_id = userObj.getString("unique_id");
            follower_count = userObj.getLong("follower_count");
            following_count = userObj.getLong("favoriting_count");
            total_favorited = userObj.getLong("total_favorited");

            ShortsUser user = new ShortsUser(author_uid, author_unique_id, author_nickname, null, author_signature, author_avatar, SocialUserType.USER_TIKTOK, follower_count, following_count, total_favorited);
            user.setRawResponse(userObj.toString());
            user.setTimeDownloaded(System.currentTimeMillis());
            Comment comment = new Comment(id, content, author_uid, null, replyCount, createTime, diggCount, false, false, null, null);
            comment.setShortsUser(user);
            comment.setTimeDownloaded(System.currentTimeMillis());
            comment.setRawResponse(commentObj.toString());
            return comment;
        } catch (JSONException e) {
            return null;
        }
    }

    private static Comment parseCommentServerTiktokMobileVersion(@NonNull JSONObject commentObj) {
        try {
            String id = commentObj.getString("cid");
            String content = commentObj.getString("text");
            long createTime = commentObj.getLong("create_time");
            int diggCount = commentObj.getInt("digg_count");
            int replyCount = commentObj.optInt("reply_total");

            JSONObject userObj = commentObj.getJSONObject("user");

            String author_avatar, author_nickname, author_uid, author_unique_id, author_signature;
            long total_favorited, following_count, follower_count;

            author_uid = userObj.getString("uid");
            author_avatar = userObj.getJSONObject("avatar_thumb").getJSONArray("url_list").optString(0);
            author_nickname = userObj.getString("nickname");
            author_signature = userObj.getString("signature");
            author_unique_id = userObj.getString("unique_id");
            follower_count = userObj.getLong("follower_count");
            following_count = userObj.optLong("favoriting_count");
            total_favorited = userObj.optLong("total_favorited");

            ShortsUser user = new ShortsUser(author_uid, author_unique_id, author_nickname, null, author_signature, author_avatar, SocialUserType.USER_TIKTOK, follower_count, following_count, total_favorited);
            user.setRawResponse(userObj.toString());
            user.setTimeDownloaded(System.currentTimeMillis());
            Comment comment = new Comment(id, content, author_uid, null, replyCount, createTime, diggCount, false, false, null, null);
            comment.setShortsUser(user);
            comment.setTimeDownloaded(System.currentTimeMillis());
            comment.setRawResponse(commentObj.toString());
            return comment;
        } catch (JSONException e) {
            return null;
        }
    }

    private static TiktokPackage parseServerTiktokAllInOne(@NonNull String body) {
        String author_avatar, author_nickname, desc, video_url, music_url, aweme_id, music_name, thumbnail, share_url, musicAvatarUrl, author_uid, music_str_id, author_unique_id, music_author, author_signature, author_share_url;
        long like, share, commentCount, total_favorited, following_count, follower_count, play_count;
        JSONObject result = null;
        try {
            result = new JSONObject(body);
        } catch (JSONException e) {
            return null;
        }
        try {
            JSONObject aweme_detail = result.getJSONObject("aweme_detail");

//            video_url = result.getString("hdplay");
            //desc = aweme_detail.getString("desc");
            //aweme_id = aweme_detail.getString("aweme_id");
            //music_url = aweme_detail.getJSONObject("music").getJSONObject("play_url").getJSONArray("url_list").getString(0);
            //thumbnail = aweme_detail.getJSONObject("video").getJSONObject("cover").getJSONArray("url_list").getString(0);
            //author_avatar = aweme_detail.getJSONObject("author").getJSONObject("avatar_medium").getJSONArray("url_list").getString(0);
            author_nickname = aweme_detail.getJSONObject("author").getString("nickname");
            author_signature = aweme_detail.getJSONObject("author").getString("signature");
            music_author = aweme_detail.getJSONObject("music").getString("author");
            author_unique_id = aweme_detail.getJSONObject("author").getString("unique_id");
            music_name = aweme_detail.getJSONObject("music").getString("title");
            //share_url = aweme_detail.getString("share_url");
            like = aweme_detail.getJSONObject("statistics").getLong("digg_count");
            commentCount = aweme_detail.getJSONObject("statistics").getLong("comment_count");
            //share = aweme_detail.getJSONObject("statistics").getLong("share_count");
            //musicAvatarUrl = aweme_detail.getJSONObject("music").getJSONObject("cover_medium").getJSONArray("url_list").getString(0);
            author_uid = aweme_detail.getJSONObject("author").getString("uid");
            music_str_id = aweme_detail.getJSONObject("music").getString("id_str");
            //author_share_url = aweme_detail.getJSONObject("author").getJSONObject("share_info").getString("share_url");
            follower_count = aweme_detail.getJSONObject("author").getLong("follower_count");
            following_count = aweme_detail.getJSONObject("author").getLong("following_count");
            total_favorited = aweme_detail.getJSONObject("author").getLong("total_favorited");
            play_count = aweme_detail.getJSONObject("statistics").getLong("play_count");
            TiktokPackage api = new TiktokPackage();
            api.setTimeDownloaded(System.currentTimeMillis());
            api.setFxMusicAvatarId(music_str_id);
            api.setFxAuthorAvatarId(author_uid);

            return api;
        } catch (JSONException e) {
            return null;
        }
    }
}
