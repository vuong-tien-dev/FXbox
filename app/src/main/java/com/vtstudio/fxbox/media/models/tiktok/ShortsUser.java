package com.vtstudio.fxbox.media.models.tiktok;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.media.models.ApiModel;

@Entity (tableName = "shorts_user")
public class ShortsUser extends ApiModel {
    // static vars
    private static String avatarDirectoryPath;

    // instance vars

    private long fxId;
    @PrimaryKey
    @NonNull
    private String uid;
    private String uniqueId;
    private String nickName;
    private String shareUrl;
    private String signature;
    private String avatarUrl;
    private String socialUserType;
    private String country;
    private long followerCount;
    private long followingCount;
    private long totalFavorite;
    private boolean verified;

    public ShortsUser(){}

    public ShortsUser(String uid, String uniqueId, String nickName, String shareUrl, String signature, String avatarUrl, String socialUserType, long followerCount, long followingCount, long totalFavorite) {
        this.uid = uid;
        this.uniqueId = uniqueId;
        this.nickName = nickName;
        this.shareUrl = shareUrl;
        this.signature = signature;
        this.avatarUrl = avatarUrl;
        this.socialUserType = socialUserType;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.totalFavorite = totalFavorite;
    }

    // instance method
    public static void setAvatarDirectoryPath(String avatarDirectoryPath) {
        ShortsUser.avatarDirectoryPath = avatarDirectoryPath;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarPath) {
        this.avatarUrl = avatarPath;
    }

    public String getSocialUserType() {
        return socialUserType;
    }

    public void setSocialUserType(String socialUserType) {
        this.socialUserType = socialUserType;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public long getTotalFavorite() {
        return totalFavorite;
    }

    public void setTotalFavorite(long totalFavorite) {
        this.totalFavorite = totalFavorite;
    }

    // static method
    public static String getAvatarDirectoryPath() {
        return avatarDirectoryPath;
    }

    public long getFxId() {
        return fxId;
    }

    public void setFxId(long fxId) {
        this.fxId = fxId;
    }
    public String getAvatarPath() {return avatarDirectoryPath + "/" + uid + ".png";}

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
