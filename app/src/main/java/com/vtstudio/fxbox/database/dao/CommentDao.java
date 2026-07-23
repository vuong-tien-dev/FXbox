package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.tiktok.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Comment> comments);

    @Update
    void update(Comment comment);

    @Delete
    void delete(Comment comment);
    @Query("DELETE FROM comment WHERE id = :id")
    void deleteByCommentId(String id);

    @Query("SELECT * FROM comment")
    List<Comment> getAllComments();

    @Query("SELECT * FROM comment WHERE  timeDownloaded > :since order by timeDownloaded asc")
    List<Comment> getAllSince(long since);

    @Query("SELECT * FROM comment WHERE id = :commentId")
    Comment getCommentById(String commentId);

    @Query("SELECT EXISTS(SELECT 1 FROM comment WHERE uid = :uid)")
    boolean hasCommentByUserId(String uid);

    @Query("SELECT EXISTS(SELECT 1 FROM comment WHERE uid = :uid AND id <> :exceptId)")
    boolean hasCommentByUserId(String uid, String exceptId);

    @Query("SELECT * FROM comment WHERE mediaVideoId = :videoId AND (replyId IS NULL OR replyId = '') LIMIT :limit OFFSET :offset")
    List<Comment> getCommentsByVideoId(String videoId, int limit, int offset);

    @Query("SELECT * FROM comment WHERE replyId = :commentId LIMIT :limit OFFSET :offset")
    List<Comment> getReplyById(String commentId, int limit, int offset);
}