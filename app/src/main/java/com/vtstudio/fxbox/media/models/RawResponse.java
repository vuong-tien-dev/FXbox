package com.vtstudio.fxbox.media.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;

@Entity
public class RawResponse {

    @PrimaryKey
    @NonNull
    String responseId;
    int mediaType;
    String stringResponse;

    public RawResponse(@NonNull String responseId, int mediaType, String stringResponse) {
        this.responseId = responseId;
        this.mediaType = mediaType;
        this.stringResponse = stringResponse;
    }

    @NonNull
    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(@NonNull String responseId) {
        this.responseId = responseId;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getStringResponse() {
        return stringResponse;
    }

    public void setStringResponse(String stringResponse) {
        this.stringResponse = stringResponse;
    }

    public static RawResponse fromShortsVideo(@NonNull ShortsVideo video) {
        return new RawResponse(video.getAwemeId(), video.getSocialMediaType(), video.getRawResponse());
    }

    public static RawResponse fromShortsMusic (@NonNull ShortsMusic music) {
        return new RawResponse(music.getId(), Media.MediaType.TYPE_SHORTS_TIKTOK, music.getRawResponse());
    }

    public static RawResponse fromShortsComment(@NonNull Comment comment) {
        return new RawResponse(comment.getId(), Media.MediaType.TYPE_SHORTS_TIKTOK, comment.getRawResponse());
    }

    public static RawResponse fromShortsUser(@NonNull ShortsUser user) {
        return new RawResponse(user.getUid(), Media.MediaType.TYPE_SHORTS_TIKTOK, user.getRawResponse());
    }
}
