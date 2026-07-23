package com.vtstudio.fxbox.media.models.youtube;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.vtstudio.fxbox.downloader.ModelDownload;

public class YTDetails implements ModelDownload {
    @Embedded
    private YTVideo video;

    @Relation(parentColumn = "channelId", entityColumn = "channelId")
    private YTUser user;

    public YTVideo getVideo() {
        return video;
    }

    public void setVideo(YTVideo video) {
        this.video = video;
    }

    public YTUser getUser() {
        return user;
    }

    public void setUser(YTUser user) {
        this.user = user;
        if(user != null) {

        }
    }

    @Override
    public String onGetTitle() {
        return video.getTitle();
    }

    @Override
    public String onGetAuthor() {
        return user.getChannelName();
    }
}
