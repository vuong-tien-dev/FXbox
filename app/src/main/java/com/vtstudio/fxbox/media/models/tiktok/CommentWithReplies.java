package com.vtstudio.fxbox.media.models.tiktok;


import androidx.room.Relation;

import java.util.List;

public class CommentWithReplies {
    @Relation(parentColumn = "id", entityColumn = "commentId")
    public List<Reply3> replies;
}
