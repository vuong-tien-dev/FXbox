package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;

import java.util.List;

@Dao
public interface ShortsVideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShortsVideo video);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<ShortsVideo> videos);

    @Update
    void update(ShortsVideo video);

    @Delete
    void delete(ShortsVideo video);

    @Query("SELECT * FROM shorts_video")
    List<ShortsVideo> getAllVideos();

    @Query("SELECT * FROM shorts_video where awemeId in (:awemeIds)")
    List<ShortsVideo> getByAwemeIds(List<String> awemeIds);

    @Query("SELECT * FROM shorts_video where fxId in (:fxIds)")
    List<ShortsVideo> getByFxIds(List<String> fxIds);

    @Query("SELECT * FROM shorts_video ORDER BY mediaStoreDayAdded ASC")
    List<ShortsVideo> getAllVideosByDayAddedASC();

    @Query("SELECT * FROM shorts_video ORDER BY mediaStoreDayAdded DESC")
    List<ShortsVideo> getAllVideosByDayAddedDESC();

    @Query("SELECT * FROM shorts_video WHERE fxId = :fxId")
    ShortsVideo getVideoByFxId(long fxId);

    @Query("SELECT * FROM shorts_video WHERE timeDownloaded > :since ORDER BY timeDownloaded ASC")
    List<ShortsVideo> getAllVideosSince(long since);

    @Query("SELECT * FROM shorts_video WHERE mediaStoreId = :mediaStoreId")
    ShortsVideo getVideoByMediaStoreId(String mediaStoreId);

    @Query("SELECT * FROM shorts_video WHERE authorId = :uid")
    List<ShortsVideo> getVideoByUserId(String uid);

    @Query("SELECT EXISTS(SELECT 1 FROM shorts_video WHERE awemeId = :awemeId)")
    boolean isExistsByAwemeId(String awemeId);

    @Query("SELECT EXISTS(SELECT 1 FROM shorts_video WHERE mediaStoreId = :mediaStoreId)")
    boolean isExistsByMediaStoreId(String mediaStoreId);

    @Query("SELECT EXISTS(SELECT 1 FROM shorts_video WHERE authorId = :uid)")
    boolean hasVideoByUserId(String uid);

    @Query("SELECT EXISTS(SELECT 1 FROM shorts_video WHERE musicId = :musicId)")
    boolean hasVideoByMusicId(String musicId);

    @Query("SELECT COUNT(*) FROM shorts_video WHERE musicId = :musicId")
    int countVideosWithMusicId(String musicId);

    @Query("SELECT EXISTS(SELECT 1 FROM shorts_video WHERE authorId = :uid AND awemeId <> :exceptId)")
    boolean hasVideoByUserId(String uid, String exceptId);

    @Query("SELECT EXISTS(SELECT 1 FROM shorts_video WHERE musicId = :musicId AND awemeId <> :exceptId)")
    boolean hasVideoByMusicId(String musicId, String exceptId);
}