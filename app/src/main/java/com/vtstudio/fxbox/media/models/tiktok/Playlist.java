package com.vtstudio.fxbox.media.models.tiktok;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.helpers.PreferenceHelper;
import com.vtstudio.fxbox.media.models.FxMediaVideo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity (tableName = "playlist")
public class Playlist {

    @Ignore private static String BACKGROUND_DIRECTORY_PATH;
    @Ignore public static final String FAVORITE_PLAYLIST_ID = "03102005";
    @Ignore public static final String RECENTLY_VIEWED_PLAYLIST = "0110031020005";
    @Ignore public static final String LIMITED_PLAYLIST = "0812742381";
    @Ignore private static final int MAX_RECENT = 40;
    @PrimaryKey @NonNull
    private String id;
    private String name;
    private String backGroundId;
    private long createTime;
    private String description;
    private List<String> videoIdList;
    @Ignore private static final List<String> defaultPlaylistIds = Arrays.asList(FAVORITE_PLAYLIST_ID, RECENTLY_VIEWED_PLAYLIST, LIMITED_PLAYLIST);
    @Ignore
    private List<FxMediaVideo> videoList;

    public Playlist(@NonNull String id, String name, @Nullable String backGroundId, long createTime) {
        this.id = id;
        this.name = name;
        this.backGroundId = backGroundId;
        this.createTime = createTime;
    }
    public static List<String> getAppDefaultPlaylistId() {
        return defaultPlaylistIds;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackGroundId() {
        return backGroundId;
    }

    public void setBackGroundId(String backGroundId) {
        this.backGroundId = backGroundId;
    }
    public String getBackgroundPath () { return getBackgroundDirectoryPath() + "/" + backGroundId + ".jpg"; }
    public static String getBackgroundDirectoryPath() {
        return BACKGROUND_DIRECTORY_PATH;
    }

    public static void setBackgroundDirectoryPath(String backgroundDirectoryPath) {
        BACKGROUND_DIRECTORY_PATH = backgroundDirectoryPath;
    }

    public List<FxMediaVideo> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<FxMediaVideo> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    public List<String> getVideoIdList() {
        if(videoIdList == null) {
            videoIdList = new ArrayList<>();
        }
        return id.equals(RECENTLY_VIEWED_PLAYLIST) ? videoIdList.subList(0, Math.min(MAX_RECENT, videoIdList.size())) : videoIdList;
    }

    public void setVideoIdList(List<String> videoIdList) {
        this.videoIdList = videoIdList;
    }

    public static boolean isAppDefaultPlaylist (String id) {
        return Objects.equals(id, Playlist.FAVORITE_PLAYLIST_ID) || Objects.equals(id, Playlist.LIMITED_PLAYLIST) || Objects.equals(id, Playlist.RECENTLY_VIEWED_PLAYLIST);
    }

    public static boolean isShortsList (@NonNull Context context, String id) {
        return id != null
                && (id.contains(PreferenceHelper.getShortsAudioPath(context, PreferenceHelper.APP))
                || id.contains(PreferenceHelper.getShortsVideoPath(context, PreferenceHelper.APP)));
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
