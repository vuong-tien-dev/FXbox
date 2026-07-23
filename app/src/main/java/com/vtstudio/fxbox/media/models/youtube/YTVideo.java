package com.vtstudio.fxbox.media.models.youtube;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.media.models.FxMediaVideo;

import java.io.File;

@Entity(tableName = "yt_video")
public class YTVideo extends FxMediaVideo {

    @Ignore
    private static String YT_VIDEO_THUMBNAIL_DIR_PATH;
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String description;
    private String channelId;
    private String shareUrl;
    private String quality;
    private long likeCount;
    private long commentCount;
    private long playCount;
    private long createTime;
    @ColumnInfo(name = "fxId")
    private long fxId;
    @Ignore
    protected String rawResponse;
    @Ignore
    protected String responseServer;
    @Ignore
    protected long timeDownloaded;
    @Ignore
    protected String thumbnailUrl;

    @Ignore
    private YTUser user;

    public YTVideo(String mediaStoreId, String mediaStoreName, String mediaStorePath, String mediaStoreParent,
                   long mediaStoreDayAdded, int size, long duration, @NonNull String id, String title, String description,
                   String channelId, String shareUrl, String quality, long likeCount, long commentCount, long playCount, long createTime, long fxId) {
        super(mediaStoreId, mediaStoreName, mediaStorePath, mediaStoreParent, mediaStoreDayAdded, size, duration);
        this.id = id;
        this.title = title;
        this.description = description;
        this.channelId = channelId;
        this.shareUrl = shareUrl;
        this.quality = quality;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.playCount = playCount;
        this.createTime = createTime;
        this.fxId = fxId;
    }

    @Ignore
    public YTVideo(String mediaStoreId, String mediaStoreName, String mediaStorePath, String mediaStoreParent, long mediaStoreDayAdded, int size, long duration) {
        super(mediaStoreId, mediaStoreName, mediaStorePath, mediaStoreParent, mediaStoreDayAdded, size, duration);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
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

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public long getFxId() {
        return fxId;
    }

    @Override
    public void setFxId(long fxId) {
        this.fxId = fxId;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Nullable
    public YTUser getUser() {
        return user;
    }

    public void setUser(YTUser user) {
        this.user = user;
    }

    // MediaTrack implementation

    @Override
    public String getTrackTitle() {
        return getTitle();
    }

    @Override
    public String getTrackAuthor() {
        return user != null ? user.getChannelName() : super.getTrackAuthor();
    }

    public String getYTVideoThumbnailPath() {
        String path = YT_VIDEO_THUMBNAIL_DIR_PATH + "/" + id + ".jpg";
        if (new File(path).exists()){
            return path;
        }
        return getFxNotificationLargeIconPath();
    }

    public static String getYtVideoThumbnailDirPath() {
        return YT_VIDEO_THUMBNAIL_DIR_PATH;
    }

    public static void setYtVideoThumbnailDirPath(String ytVideoThumbnailDirPath) {
        YTVideo.YT_VIDEO_THUMBNAIL_DIR_PATH = ytVideoThumbnailDirPath;
    }
}
