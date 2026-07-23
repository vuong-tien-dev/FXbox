package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.youtube.YTVideo;

import java.util.List;

@Dao
public interface YTVideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(YTVideo video);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<YTVideo> videos);

    @Update
    void update(YTVideo video);

    @Delete
    void delete(YTVideo video);

    @Query("DELETE FROM yt_video")
    void deleteAll();

    @Query("SELECT * FROM yt_video")
    List<YTVideo> getAllVideos();
    @Query("SELECT * FROM yt_video WHERE id = :id")
    YTVideo getVideoById(String id);

    @Query("SELECT * FROM yt_video WHERE fxId = :fxId")
    YTVideo getVideoByFxId(long fxId);

    @Query("SELECT * FROM yt_video WHERE channelId = :channelId")
    List<YTVideo> getVideosByChannelId(String channelId);

    @Query("SELECT * FROM yt_video ORDER BY createTime ASC")
    List<YTVideo> getAllVideosByCreateTimeASC();

    @Query("SELECT * FROM yt_video ORDER BY createTime DESC")
    List<YTVideo> getAllVideosByCreateTimeDESC();

    @Query("SELECT EXISTS(SELECT 1 FROM yt_video WHERE id = :id)")
    boolean isExistsById(String id);

    @Query("SELECT COUNT(*) FROM yt_video WHERE channelId = :channelId")
    int countVideosByChannelId(String channelId);
}
