package com.vtstudio.fxbox.media.models;

import android.net.Uri;

import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;

public class Media extends ApiModel {
    @Ignore private final Uri playUri;
    @PrimaryKey(autoGenerate = true)

    private long fxId;
    private String mediaStoreId;
    private String mediaStoreName;
    private String mediaStorePath;
    private String mediaStoreParent;
    private int size;
    private long mediaStoreDayAdded;
    private boolean isFavorite;
    private boolean isPrivate;
    private boolean isLimited;
    private int socialMediaType;
    @Ignore
    private boolean isCanPlayLimited;


    public Media(String mediaStoreId , String mediaStoreName, String mediaStorePath, String mediaStoreParent, int size, long mediaStoreDayAdded)
    {
        this.playUri = Uri.fromFile(new File(mediaStorePath));
        this.mediaStoreId = mediaStoreId;
        this.mediaStoreName = mediaStoreName;
        this.mediaStorePath = mediaStorePath;
        this.mediaStoreParent = mediaStoreParent;
        this.size = size;
        this.mediaStoreDayAdded = mediaStoreDayAdded;
    }

    public long getFxId() {
        return fxId;
    }

    public void setFxId(long fxId) {
        this.fxId = fxId;
    }

    public String getMediaStoreId() {
        return mediaStoreId;
    }

    public void setMediaStoreId(String mediaStoreId) {
        this.mediaStoreId = mediaStoreId;
    }

    public String getMediaStorePath() {
        return mediaStorePath;
    }

    public void setMediaStorePath(String mediaStorePath) {
        this.mediaStorePath = mediaStorePath;
    }

    public String getMediaStoreParent() {
        return mediaStoreParent;
    }

    public void setMediaStoreParent(String mediaStoreParent) {
        this.mediaStoreParent = mediaStoreParent;
    }

    public long getMediaStoreDayAdded() {
        return mediaStoreDayAdded;
    }

    public void setMediaStoreDayAdded(long mediaStoreDayAdded) {
        this.mediaStoreDayAdded = mediaStoreDayAdded;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getMediaStoreName() {
        return mediaStoreName;
    }

    public void setMediaStoreName(String mediaStoreName) {
        this.mediaStoreName = mediaStoreName;
    }

    public int getSocialMediaType() {
        return socialMediaType;
    }

    public void setSocialMediaType(int socialMediaType) {
        this.socialMediaType = socialMediaType;
    }

    public boolean isLimited() {
        return isLimited;
    }

    public void setLimited(boolean limited) {
        isLimited = limited;
    }
    public Uri getPlayUri() {
        return playUri;
    }

    public boolean isCanPlayLimited() {
        return !isLimited || isCanPlayLimited;
    }

    public void setUserIgnoredLimited(boolean canPlayLimited) {
        isCanPlayLimited = canPlayLimited;
    }


    public static class MediaType {
        public static final int TYPE_EXTERNAL_STORAGE = 0;
        public static final int TYPE_SHORTS_VIDEO = 1;
        public static final int TYPE_SHORTS_TIKTOK = 2;
        public static final int TYPE_SHORTS_YOUTUBE = 3;
        public static final int TYPE_REEL_FACEBOOK = 4;
    }
}
