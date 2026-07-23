package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.MediaSegment;

import java.util.List;

@Dao
public interface MediaSegmentDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaSegment segment);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MediaSegment> segments);

    @Update
    void update(MediaSegment segment);

    @Delete
    void delete(MediaSegment segment);

    @Query("SELECT * FROM media_segment WHERE id = :id")
    MediaSegment getById(long id);

    @Query("SELECT * FROM media_segment")
    List<MediaSegment> getAllSegments();

    @Query("SELECT * FROM media_segment WHERE mediaId = :id")
    List<MediaSegment> getAllByMediaId(long id);

    @Query("DELETE FROM media_segment WHERE mediaId = :id")
    void deleteAllByMediaId(long id);
}