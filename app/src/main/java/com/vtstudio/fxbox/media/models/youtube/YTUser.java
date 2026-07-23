package com.vtstudio.fxbox.media.models.youtube;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "yt_user")
public class YTUser {

    @Ignore
    private static String USER_AVATAR_DIRECTORY_PATH;
    private long fxId;
    @PrimaryKey
    @NonNull
    private String channelId;
    private String uniqueId;
    private String channelName;
    private String description;
    private String avatarUrl;
    private String socialUserType;
    private String country;
    private long subscriberCount;
    private long totalViewCount;
    private long videoCount;
    private boolean verified;

    @Ignore
    private String rawResponse;

    public YTUser(long fxId, @NonNull String channelId, String uniqueId, String channelName,
                  String description, String avatarUrl, String socialUserType, String country,
                  long subscriberCount, long totalViewCount, long videoCount, boolean verified) {
        this.fxId = fxId;
        this.channelId = channelId;
        this.uniqueId = uniqueId;
        this.channelName = channelName;
        this.description = description;
        this.avatarUrl = avatarUrl;
        this.socialUserType = socialUserType;
        this.country = country;
        this.subscriberCount = subscriberCount;
        this.totalViewCount = totalViewCount;
        this.videoCount = videoCount;
        this.verified = verified;
    }

    public long getFxId() {
        return fxId;
    }

    public void setFxId(long fxId) {
        this.fxId = fxId;
    }

    @NonNull
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(@NonNull String channelId) {
        this.channelId = channelId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getSocialUserType() {
        return socialUserType;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public long getTotalViewCount() {
        return totalViewCount;
    }

    public void setTotalViewCount(long totalViewCount) {
        this.totalViewCount = totalViewCount;
    }

    public long getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(long videoCount) {
        this.videoCount = videoCount;
    }

    public void setSocialUserType(String socialUserType) {
        this.socialUserType = socialUserType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public String getAvatarPath() {
        return USER_AVATAR_DIRECTORY_PATH + "/" + channelId + ".png";
    }
    public static void setUserAvatarDirectoryPath(String userAvatarDirectoryPath) {
        USER_AVATAR_DIRECTORY_PATH = userAvatarDirectoryPath;
    }

    public static String getUserAvatarDirectoryPath() {
        return USER_AVATAR_DIRECTORY_PATH;
    }
}
