package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.tiktok.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Playlist playlist);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Playlist> playlists);

    @Update
    void update(Playlist playlist);

    @Delete
    void delete(Playlist playlist);

    @Query("DELETE FROM playlist")
    void deleteAll();
    @Query("SELECT * FROM playlist")
    List<Playlist> getAllPlaylists();

    @Query("SELECT * FROM playlist WHERE id NOT IN (:excludeList)")
    List<Playlist> getAllPlaylistsWithExcludes(List<String> excludeList);

    @Query("SELECT * FROM playlist WHERE videoIdList IS NOT NULL AND LENGTH(videoIdList) > 0")
    List<Playlist> getAllPlaylistsNotEmpty();

    @Query("SELECT * FROM playlist WHERE videoIdList IS NOT NULL AND LENGTH(videoIdList) > 0 AND id NOT IN (:excludeList) ORDER BY createTime DESC LIMIT :limit")
    List<Playlist> getAllNotEmptyDescLimit(int limit, List<String> excludeList);

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    Playlist getPlaylistById(String playlistId);

    @Query("SELECT EXISTS(SELECT 1 FROM playlist WHERE id = :playlistId)")
    boolean isExistsById(String playlistId);
}