package com.vtstudio.fxbox.models;

import com.vtstudio.fxbox.downloader.ModelDownload;
import com.vtstudio.fxbox.media.models.ApiModel;

import java.util.List;

public class TiktokPackage extends ApiModel implements ModelDownload {
    private int requestId;
    private String description;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    private String url;
    private String shareUrl;

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    private String authorNickname;
    private String musicInformation;
    private String thumbnail;
    private String musicUrl;
    private String awemeId;
    private long duration;
    private String authorAvatar;
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private long playCount;
    private String fxAuthorAvatarId;
    private String fxMusicAvatarId;
    private String authorUniqueId;
    private String authorSignature;
    private String musicAuthor;
    private String authorShareUrl;
    private boolean verified;
    private long authorFollower;
    private long totalFavorited;
    private long authorFollowingCount;
    private List<String> imageIdList;
    private String descriptionTitle;
    private boolean isWarnAttempt;
    private String capcutTemplateId;
    private String capcutSchema;
    private int warningType;
    private String anchorPosId;
    private String anchorPosKeyword;

    public long getAuthorFollower() {
        return authorFollower;
    }

    public void setAuthorFollower(long authorFollower) {
        this.authorFollower = authorFollower;
    }

    public long getTotalFavorited() {
        return totalFavorited;
    }

    public void setTotalFavorited(long totalFavorited) {
        this.totalFavorited = totalFavorited;
    }

    public String getAuthorUniqueId() {
        return authorUniqueId;
    }

    public void setAuthorUniqueId(String authorUniqueId) {
        this.authorUniqueId = authorUniqueId;
    }

    public String getAuthorSignature() {
        return authorSignature;
    }

    public void setAuthorSignature(String authorSignature) {
        this.authorSignature = authorSignature;
    }

    public String getMusicAuthor() {
        return musicAuthor;
    }

    public void setMusicAuthor(String musicAuthor) {
        this.musicAuthor = musicAuthor;
    }

    public String getFxAuthorAvatarId() {
        return fxAuthorAvatarId;
    }

    public void setFxAuthorAvatarId(String fxAuthorAvatarId) {
        this.fxAuthorAvatarId = fxAuthorAvatarId;
    }

    public String getFxMusicAvatarId() {
        return fxMusicAvatarId;
    }

    public void setFxMusicAvatarId(String fxMusicAvatarId) {
        this.fxMusicAvatarId = fxMusicAvatarId;
    }

    public String getMusicAvatarUrl() {
        return musicAvatarUrl;
    }

    public void setMusicAvatarUrl(String musicAvatarUrl) {
        this.musicAvatarUrl = musicAvatarUrl;
    }

    private String musicAvatarUrl;

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }
    private long shortsCreateTime;
    private boolean isImageListUrl;
    private List<String> imageListPath;
    private String region;


    public TiktokPackage() {
    };

    public TiktokPackage(String description, String url, String authorNickname, String musicInformation, String thumbnail, String musicUrl, String awemeId, long duration, String authorAvatar) {
        this.description = description;
        this.url = url;
        this.authorNickname = authorNickname;
        this.musicInformation = musicInformation;
        this.thumbnail = thumbnail;
        this.musicUrl = musicUrl;
        this.awemeId = awemeId;
        this.duration = duration;
        this.authorAvatar = authorAvatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getMusicInformation() {
        return musicInformation;
    }

    public void setMusicInformation(String musicInformation) {
        this.musicInformation = musicInformation;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getAwemeId() {
        return awemeId;
    }

    public void setAwemeId(String awemeId) {
        this.awemeId = awemeId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getShareCount() {
        return shareCount;
    }

    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }

    @Override
    public String toString() {
        return "TiktokPackage{" +
                "description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", authorNickname='" + authorNickname + '\'' +
                ", musicInformation='" + musicInformation + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", musicUrl='" + musicUrl + '\'' +
                ", awemeId='" + awemeId + '\'' +
                ", duration=" + duration +
                ", authorAvatar='" + authorAvatar + '\'' +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", shareCount=" + shareCount +
                ", playCount=" + playCount +
                '}';
    }

    public String getAuthorShareUrl() {
        return authorShareUrl;
    }

    public void setAuthorShareUrl(String authorShareUrl) {
        this.authorShareUrl = authorShareUrl;
    }

    public long getAuthorFollowingCount() {
        return authorFollowingCount;
    }

    public void setAuthorFollowingCount(long authorFollowingCount) {
        this.authorFollowingCount = authorFollowingCount;
    }

    public long getShortsCreateTime() {
        return shortsCreateTime;
    }

    public void setShortsCreateTime(long shortsCreateTime) {
        this.shortsCreateTime = shortsCreateTime;
    }

    public boolean isImageList() {
        return isImageListUrl;
    }

    public void setIsImageList(boolean imageList) {
        isImageListUrl = imageList;
    }

    public List<String> getImageListUrl() {
        return imageListPath;
    }

    public void setImageListUrl(List<String> imageListPath) {
        this.imageListPath = imageListPath;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getImageListPath() {
        return imageListPath;
    }

    public void setImageListPath(List<String> imageListPath) {
        this.imageListPath = imageListPath;
    }

    public List<String> getImageIdList() {
        return imageIdList;
    }

    public void setImageIdList(List<String> imageIdList) {
        this.imageIdList = imageIdList;
    }

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getDescriptionTitle() {
        return descriptionTitle;
    }

    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public boolean isWarnAttempt() {
        return isWarnAttempt;
    }

    public void setWarnAttempt(boolean warnAttempt) {
        isWarnAttempt = warnAttempt;
    }

    public String getCapcutTemplateId() {
        return capcutTemplateId;
    }

    public void setCapcutTemplateId(String capcutTemplateId) {
        this.capcutTemplateId = capcutTemplateId;
    }

    public String getCapcutSchema() {
        return capcutSchema;
    }

    public void setCapcutSchema(String capcutSchema) {
        this.capcutSchema = capcutSchema;
    }

    public int getWarningType() {
        return warningType;
    }

    public void setWarningType(int warningType) {
        this.warningType = warningType;
    }

    public String getAnchorPosId() {
        return anchorPosId;
    }

    public void setAnchorPosId(String anchorPosId) {
        this.anchorPosId = anchorPosId;
    }

    public String getAnchorPosKeyword() {
        return anchorPosKeyword;
    }

    public void setAnchorPosKeyword(String anchorPosKeyword) {
        this.anchorPosKeyword = anchorPosKeyword;
    }

    @Override
    public String onGetTitle() {
        return description;
    }

    @Override
    public String onGetAuthor() {
        return authorNickname;
    }
}
