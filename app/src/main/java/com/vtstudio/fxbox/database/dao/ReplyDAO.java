package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.tiktok.Reply3;

import java.util.List;

@Dao
public interface ReplyDAO {
    @Insert
    void insert(Reply3 reply);

    @Insert
    void insertAll(List<Reply3> replies);

    @Update
    void update(Reply3 reply);

    @Delete
    void delete(Reply3 reply);

    @Query("SELECT * FROM reply")
    List<Reply3> getAllReplies();

    @Query("SELECT * FROM reply WHERE id = :replyId")
    Reply3 getReplyById(String replyId);

    @Query("SELECT * FROM reply WHERE commentId = :commentId")
    List<Reply3> getRepliesByCommentId(String commentId);
}