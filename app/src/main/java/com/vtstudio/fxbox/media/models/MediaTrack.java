package com.vtstudio.fxbox.media.models;

public interface MediaTrack {
    default String getTrackTitle() {
        return "Unknown track";
    }

    default String getTrackAuthor() {
        return "Unknown author";
    }

    default long getTrackDuration() {
        return  0;
    }

    default String getTrackThumbnailPath() {
        return "";
    }
}
