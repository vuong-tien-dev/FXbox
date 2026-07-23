package com.vtstudio.fxbox.media.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media_segment")
public class MediaSegment {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private long startTime;
    private long endTime;
    private long mediaId;

    public MediaSegment() {

    }

    public MediaSegment(long id, String title, long startTime, long endTime, long mediaId) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mediaId = mediaId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }
}
