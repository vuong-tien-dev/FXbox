package com.vtstudio.fxbox.media.models;


import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "fx_media_video")
public class FxMediaVideo extends Media implements MediaTrack {

    // static vars
    @Ignore
    private static String fxThumbnailDirectoryPath;
    @Ignore
    private static String fxNotificationLargeIconDirectoryPath;

    // instance vars
    private String fxThumbnailId;
    private String fxNotificationLargeIconId;
    private String fxDescription;
    private long fxAuthorId;
    private long fxMusicId;
    private long duration;
    private int width;
    private int height;
    private float volume = 1;
    private long mediaSegmentId =  -1;
    @Ignore
    private MediaSegment currentSegment;

    public FxMediaVideo(String mediaStoreId, String mediaStoreName, String mediaStorePath, String mediaStoreParent, long mediaStoreDayAdded, int size, long duration) {
        super(mediaStoreId, mediaStoreName, mediaStorePath, mediaStoreParent, size, mediaStoreDayAdded);
        this.duration = duration;
    }


    // instance method
    public String getFxThumbnailId() {
        return fxThumbnailId;
    }

    public void setFxThumbnailId(String fxThumbnailId) {
        this.fxThumbnailId = fxThumbnailId;
    }

    public String getFxNotificationLargeIconId() {
        return fxNotificationLargeIconId;
    }

    public void setFxNotificationLargeIconId(String fxNotificationLargeIconId) {
        this.fxNotificationLargeIconId = fxNotificationLargeIconId;
    }

    public String getFxDescription() {
        return fxDescription;
    }

    public void setFxDescription(String fxDescription) {
        this.fxDescription = fxDescription;
    }

    // static method

    public static String getFxThumbnailDirectoryPath() {
        return fxThumbnailDirectoryPath;
    }

    public static void setFxThumbnailDirectoryPath(String fxThumbnailDirectoryPath) {
        FxMediaVideo.fxThumbnailDirectoryPath = fxThumbnailDirectoryPath;
    }

    public static String getFxNotificationLargeIconDirectoryPath() {
        return fxNotificationLargeIconDirectoryPath;
    }

    public static void setFxNotificationLargeIconDirectoryPath(String fxNotificationLargeIconDirectoryPath) {
        FxMediaVideo.fxNotificationLargeIconDirectoryPath = fxNotificationLargeIconDirectoryPath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getFxAuthorId() {
        return fxAuthorId;
    }

    public void setFxAuthorId(long fxAuthorId) {
        this.fxAuthorId = fxAuthorId;
    }

    public long getFxMusicId() {
        return fxMusicId;
    }

    public void setFxMusicId(long fxMusicId) {
        this.fxMusicId = fxMusicId;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public String getFxNotificationLargeIconPath() {
        return fxNotificationLargeIconDirectoryPath + "/" + getMediaStoreName() + ".jpg";
    }

    public String getFxThumbnailPath() {
        return fxThumbnailDirectoryPath + "/" + fxThumbnailId + ".jpg";
    }

    public long getMediaSegmentId() {
        return mediaSegmentId;
    }

    public void setMediaSegmentId(long mediaSegmentId) {
        this.mediaSegmentId = mediaSegmentId;
    }

    public MediaSegment getCurrentSegment() {
        return currentSegment;
    }

    public void setCurrentSegment(MediaSegment currentSegment) {
        this.currentSegment = currentSegment;
    }

    public long getSeekTime(long segmentTimeMillis) {
        if(currentSegment != null) {
            return currentSegment.getStartTime() + segmentTimeMillis;
        }

        return segmentTimeMillis;
    }

    public long getDisplayTime(long originalTimeMillis) {
        if(currentSegment != null) {
            return originalTimeMillis - currentSegment.getStartTime();
        }

        return originalTimeMillis;
    }

    public long getSegmentedDuration() {
        if(currentSegment != null) {
            return currentSegment.getEndTime() - currentSegment.getStartTime();
        }
        return duration;
    }

    // MediaTrack implementation

    @Override
    public String getTrackTitle() {
        return getMediaStoreName();
    }

    @Override
    public String getTrackAuthor() {
        return getMediaStoreParent();
    }

    @Override
    public long getTrackDuration() {
        return getSegmentedDuration();
    }

    @Override
    public String getTrackThumbnailPath() {
        return getFxThumbnailPath();
    }
}
