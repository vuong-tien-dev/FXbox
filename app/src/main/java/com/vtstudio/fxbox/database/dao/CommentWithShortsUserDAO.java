package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vtstudio.fxbox.media.models.tiktok.CommentWithShortsUser;

import java.util.List;

@Dao
public interface CommentWithShortsUserDAO {
    @Transaction
    @Query("SELECT * FROM comment")
    List<CommentWithShortsUser> getAll();
}
