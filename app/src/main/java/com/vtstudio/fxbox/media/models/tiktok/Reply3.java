package com.vtstudio.fxbox.media.models.tiktok;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "reply")
public class Reply3 {
    @PrimaryKey @NonNull
    private String id;
    private String commentId;
    private String content;
    private String uid;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public Reply3(String id, String commentId, String content, String uid) {
        this.commentId = commentId;
        this.id = id;
        this.content = content;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
