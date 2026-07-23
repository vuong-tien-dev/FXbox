package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.FxMediaVideo;

import java.util.List;

@Dao
public interface FxMediaVideoDao {
    @Insert
    void insert (FxMediaVideo fxMediaVideo);

    @Insert
    void insert (FxMediaVideo... fxMediaVideos);

    @Update
    void update (FxMediaVideo fxMediaVideo);

    @Insert
    void update (FxMediaVideo... fxMediaVideos);

    @Delete
    void delete (FxMediaVideo fxMediaVideo);

    @Delete
    void delete (FxMediaVideo... fxMediaVideos);

    @Query("DELETE FROM fx_media_video")
    void deleteAll();

    @Query("SELECT * FROM fx_media_video")
    List<FxMediaVideo> getAll ();

    @Query("SELECT EXISTS(SELECT 1 FROM fx_media_video WHERE fxId = :fxId LIMIT 1)")
    boolean isExistsByFxId(long fxId);

    @Query("SELECT * FROM fx_media_video WHERE mediaStoreId = :mediaStoreId")
    FxMediaVideo getVideoByMediaStoreId(String mediaStoreId);

    @Query("SELECT EXISTS(SELECT 1 FROM fx_media_video WHERE mediaStoreId = :mediaStoreId LIMIT 1)")
    boolean isExistsByMediaStoreId(String mediaStoreId);

    @Query("SELECT * FROM fx_media_video WHERE fxId = :fxId")
    FxMediaVideo getVideoByFxId(long fxId);

    @Query("SELECT * FROM fx_media_video WHERE fxId IN (:fxIdList)")
    List<FxMediaVideo> getAllByFxIdList(List<Long> fxIdList);

    @Query("SELECT * FROM fx_media_video WHERE mediaStoreName LIKE '%' || :key || '%' OR mediaStorePath LIKE '%' || :key || '%' ")
    List<FxMediaVideo> searchVideosByCriteria  (String key);
}
