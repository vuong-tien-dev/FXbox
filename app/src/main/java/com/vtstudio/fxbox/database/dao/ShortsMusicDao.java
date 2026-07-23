package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;

import java.util.List;

@Dao
public interface ShortsMusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ShortsMusic> musicList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShortsMusic music);

    @Query("SELECT * FROM shorts_music")
    List<ShortsMusic> getAll();

    @Query("SELECT * FROM shorts_music where timeDownloaded > :since order by timeDownloaded asc")
    List<ShortsMusic> getAllSince(long since);

    @Query("SELECT * FROM shorts_music WHERE id = :id")
    ShortsMusic getById(String id);

    @Query("SELECT Exists(SELECT * FROM shorts_music WHERE id = :id)")
    boolean isExistsById(String id);

    @Update
    void update(ShortsMusic music);

    @Delete
    void delete(ShortsMusic music);
}