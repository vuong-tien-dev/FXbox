package com.vtstudio.fxbox.media.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.media.models.Media;

import java.util.List;

public class MediaRequest {
    private final int id;
    private FxPlayer fxPlayer;
    private String name;
    private String playlistId;
    private boolean isPlaying;
    private int currentPlayingIndex = -1;
    private long currentPosition;
    protected boolean firstPlayed;
    private boolean canRemove = false;
    private int playBackBehavior = PlaybackBehavior.BEHAVIOR_REPEAT;
    private List<Media> mediaList;
    private OnQueryMediaListener onQueryMediaListener;
    private boolean canQueryMore;
    private boolean hasBeenLoadedMore = false;

    public MediaRequest(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getCurrentPlayingIndex() {
        return currentPlayingIndex;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    void setFxPlayer (FxPlayer fxPlayer) {
        this.fxPlayer = fxPlayer;
    }

    public void setCurrentPlayingIndex(int currentPlayingIndex) {
//        if(currentPlayingIndex < 0 || mediaList == null || currentPlayingIndex >= mediaList.size()){
//            Log.e("FxPlayer", currentPlayingIndex + " is not valid playing index");
//            return;
//        }

        this.currentPlayingIndex = currentPlayingIndex;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
//        FxMediaVideo currentMedia = mediaList != null
//                && currentPlayingIndex >= 0
//                && currentPlayingIndex < mediaList.size()
//                ? (FxMediaVideo) mediaList.get(currentPlayingIndex) : null;
//
//        if(currentMedia == null) {
//            Log.e("FxPlayer", "Cannot set current playing position because no media is null, instance " + this);
//            return;
//        }
//
//        if(currentPosition < 0 || currentPosition > currentMedia.getDuration()) {
//            Log.e("FxPlayer", currentPosition + " is exceeds duration, media: " + currentMedia.getMediaStoreName());
//            return;
//        }

        this.currentPosition = currentPosition;
    }

    public void setOnQueryMediaListener(OnQueryMediaListener onQueryMediaListener) {
        this.onQueryMediaListener = onQueryMediaListener;
        if(mediaList != null && currentPlayingIndex >= mediaList.size() - 2 && fxPlayer != null && fxPlayer.getDatabase() != null) {
            queryMediaIfHasMore(fxPlayer.getDatabase());
        }
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(@Nullable List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    public boolean isFirstPlayed() {
        return firstPlayed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean canRemove() {
        return canRemove;
    }

    protected void setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
    }
    public int getPlayBackBehavior() {
        return playBackBehavior;
    }

    public void setPlayBackBehavior(int playBackBehavior) {
        this.playBackBehavior = playBackBehavior;
    }

    public boolean canQueryMore() {
        return canQueryMore;
    }

    public void setQueryMore(boolean canQueryMore) {
        this.canQueryMore = canQueryMore;
    }

    public void queryMediaIfHasMore (@NonNull FxRoomDB db) {
        if(canQueryMore && onQueryMediaListener != null) {
            List<Media> queriedMedia = onQueryMediaListener.onQueryMedia(db, this, playlistId, mediaList == null ? 0 : mediaList.size());
            canQueryMore = queriedMedia != null && queriedMedia.size() > 0;
            if(canQueryMore) {
                hasBeenLoadedMore = true;
                mediaList.addAll(queriedMedia);
            }
        }
    }
    @NonNull
    @Override
    public String toString() {
        return "MediaRequest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isPlaying=" + isPlaying +
                ", currentPlayingIndex=" + currentPlayingIndex +
                ", currentPosition=" + currentPosition +
                ", firstPlayed=" + firstPlayed +
                ", canRemove=" + canRemove +
                ", playBackBehavior=" + playBackBehavior +
                '}';
    }

    public boolean isHasBeenLoadedMore() {
        return hasBeenLoadedMore;
    }

    public void hasBeenLoadedMoreSolved () {
        if(hasBeenLoadedMore) {
            hasBeenLoadedMore = false;
        }
    }

}
