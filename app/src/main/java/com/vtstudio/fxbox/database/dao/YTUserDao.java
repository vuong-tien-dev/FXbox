package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.youtube.YTUser;

import java.util.List;

@Dao
public interface YTUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(YTUser user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<YTUser> users);

    @Update
    void update(YTUser user);

    @Delete
    void delete(YTUser user);

    @Query("DELETE FROM yt_user WHERE channelId = :channelId")
    void deleteByChannelId(String channelId);
    @Query("DELETE FROM yt_user")
    void deleteAll();
    @Query("SELECT * FROM yt_user")
    List<YTUser> getAllUsers();

    @Query("SELECT * FROM yt_user WHERE channelId = :channelId")
    YTUser getUserByChannelId(String channelId);

    @Query("SELECT * FROM yt_user WHERE fxId = :fxId")
    YTUser getUserByFxId(long fxId);

    @Query("SELECT EXISTS(SELECT 1 FROM yt_user WHERE channelId = :channelId)")
    boolean isExistsByChannelId(String channelId);

    @Query("SELECT EXISTS(SELECT 1 FROM yt_user WHERE uniqueId = :uniqueId)")
    boolean isExistsByUniqueId(String uniqueId);
}
