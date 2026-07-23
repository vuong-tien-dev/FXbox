package com.vtstudio.fxbox.media.models.tiktok;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.media.models.ApiModel;

@Entity (tableName = "shorts_music")
public class ShortsMusic extends ApiModel {
    // static vars
    private static String avatarDirectoryPath;

    // instance vars

    private long fxId;
    @PrimaryKey
    @NonNull
    private String id;
    private String url;
    private String title;
    private String author;
    private String authorId;
    private long duration;

    public ShortsMusic(){}

    public ShortsMusic(String id, String url, String title, String author, long duration, String authorId) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.authorId = authorId;
    }

    // static method
    public static String getAvatarDirectoryPath() {
        return avatarDirectoryPath;
    }

    public static void setAvatarDirectoryPath(String avatarDirectoryPath) {
        ShortsMusic.avatarDirectoryPath = avatarDirectoryPath;
    }

    // instance method

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getFxId() {
        return fxId;
    }

    public void setFxId(long fxId) {
        this.fxId = fxId;
    }

    public String getId() {
        return id;
    }

    public void setId(String music_id) {
        this.id = music_id;
    }
    public String getFxThumbnailPath (){
        return avatarDirectoryPath + "/" +  id + ".jpg";
    }
}
