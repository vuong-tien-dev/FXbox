package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;

import java.util.List;

@Dao
public interface ShortsUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ShortsUser> users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert (ShortsUser shortsUser);

    @Query("SELECT * FROM shorts_user")
    List<ShortsUser> getAll();

    @Query("SELECT * FROM shorts_user where timeDownloaded > :since order by timeDownloaded asc")
    List<ShortsUser> getAllSince(long since);

    @Query("SELECT * FROM shorts_user WHERE uid = :uid")
    ShortsUser getUserById(String uid);

    @Update
    void update(ShortsUser user);

    @Delete
    void delete(ShortsUser user);
    @Query("DELETE FROM shorts_user WHERE uid = :uid")
    void deleteByUid(String uid);

    @Query("SELECT EXISTS (SELECT * FROM shorts_user where uid = :uid)")
    boolean isExistsByUserId (String uid);

}
