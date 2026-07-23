package com.vtstudio.fxbox.media.models.tiktok;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.media.models.FxMediaVideo;

import java.util.List;

@Entity (tableName = "shorts_video")
public class ShortsVideo extends FxMediaVideo {
    @Ignore private static String dynamicCoverDirectoryPath;
    @Ignore private static String imgListDirectoryPath;
    @Ignore public static int WARNING_TYPE_DO_NOT_ATTEMPT = 1;
    @Ignore public static int WARNING_TYPE_BEFORE_VIEWING = 2;
    @PrimaryKey @NonNull
    private String awemeId;
    private String url;
    private String shareUrl;
    private String fxCloudDatabaseUrl;
    private String dynamicCover;
    private String description;
    private String descriptionTitle;
    private String authorId;
    private String musicId;
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private long playCount;
    private long shortsCreateTime;
    private boolean isImageList;
    private List<String> imageListPath;
    private List<String> imageIdList;
    private String region;
    //private String descriptionLanguage;
    private boolean isWarnAttempt;
    private int warningType;
    private String capcutTemplateId;
    private String capcutSchema;
    private String anchorPosId;
    private String anchorPosKeyword;
    @Ignore private ShortsUser shortsUser;
    @Ignore private ShortsMusic shortsMusic;
    @ColumnInfo(name = "fxId")
    private long fxId;

    public static String getImgListDirectoryPath() {
        return imgListDirectoryPath;
    }

    public static void setImgListDirectoryPath(String imgListDirectoryPath) {
        ShortsVideo.imgListDirectoryPath = imgListDirectoryPath;
    }

    @Override
    public long getFxId() {
        return fxId;
    }

    @Override
    public void setFxId(long fxId) {
        this.fxId = fxId;
    }

    public ShortsVideo(String mediaStoreId, String mediaStoreName, String mediaStorePath, String mediaStoreParent, long mediaStoreDayAdded, int size, long duration) {
        super(mediaStoreId, mediaStoreName, mediaStorePath, mediaStoreParent, mediaStoreDayAdded, size, duration);
    }


    // static methods
    public static String getDynamicCoverDirectoryPath() {
        return dynamicCoverDirectoryPath;
    }

    public static void setDynamicCoverDirectoryPath(String dynamicCoverDirectoryPath) {
        ShortsVideo.dynamicCoverDirectoryPath = dynamicCoverDirectoryPath;
    }

    // instance methods

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

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public String getAwemeId() {
        return awemeId;
    }

    public void setAwemeId(String awemeId) {
        this.awemeId = awemeId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getFxCloudDatabaseUrl() {
        return fxCloudDatabaseUrl;
    }

    public void setFxCloudDatabaseUrl(String fxCloudDatabaseUrl) {
        this.fxCloudDatabaseUrl = fxCloudDatabaseUrl;
    }

    public String getDynamicCover() {
        return dynamicCover;
    }

    public void setDynamicCover(String dynamicCover) {
        this.dynamicCover = dynamicCover;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public ShortsUser getShortsUser() {
        return shortsUser;
    }

    public void setShortsUser(ShortsUser shortsUser) {
        this.shortsUser = shortsUser;
    }

    public ShortsMusic getShortsMusic() {
        return shortsMusic;
    }

    public void setShortsMusic(ShortsMusic shortsMusic) {
        this.shortsMusic = shortsMusic;
    }

    public long getShortsCreateTime() {
        return shortsCreateTime;
    }

    public void setShortsCreateTime(long shortsCreateTime) {
        this.shortsCreateTime = shortsCreateTime;
    }

    public boolean isImageList() {
        return isImageList;
    }

    public void setIsImageList(boolean imageList) {
        isImageList = imageList;
    }

    public List<String> getImageListPath() {
        return imageListPath;
    }

    public void setImageListPath(List<String> imageListPath) {
        this.imageListPath = imageListPath;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getImageIdList() {
        return imageIdList;
    }

    public void setImageIdList(List<String> imageIdList) {
        this.imageIdList = imageIdList;
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

    // util methods
    public boolean hasDescriptionTitle () {
        return descriptionTitle != null && !descriptionTitle.trim().isEmpty();
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

    // MediaTrack implementation

    @Override
    public String getTrackTitle() {
        return getDescription();
    }

    @Override
    public String getTrackAuthor() {
        return shortsUser != null ? shortsUser.getNickName() : super.getTrackAuthor();
    }
}
