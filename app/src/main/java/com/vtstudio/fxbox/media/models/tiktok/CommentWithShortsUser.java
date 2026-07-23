package com.vtstudio.fxbox.media.models.tiktok;

import androidx.room.Embedded;
import androidx.room.Relation;

public class CommentWithShortsUser {
    @Embedded
    private Comment comment;

    @Relation(parentColumn = "uid", entityColumn = "uid")
    private ShortsUser user;

    public Comment getComment() {
        return comment;
    }

    public ShortsUser getUser() {
        return user;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public void setUser(ShortsUser user) {
        this.user = user;
    }
}
