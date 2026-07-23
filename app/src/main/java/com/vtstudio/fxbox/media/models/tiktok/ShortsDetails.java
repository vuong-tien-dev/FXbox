package com.vtstudio.fxbox.media.models.tiktok;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ShortsDetails {
    @Embedded private ShortsVideo shortsVideo;

    @Relation(parentColumn = "authorId", entityColumn = "uid")
    private ShortsUser shortsUser;

    @Relation(parentColumn = "musicId", entityColumn = "id")
    private ShortsMusic shortsMusic;

    public ShortsVideo getShortsVideo() {
        return shortsVideo;
    }

    public void setShortsVideo(ShortsVideo shortsVideo) {
        this.shortsVideo = shortsVideo;
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
}
