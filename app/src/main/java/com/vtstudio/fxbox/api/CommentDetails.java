package com.vtstudio.fxbox.api;

import com.vtstudio.fxbox.media.models.ApiModel;
import com.vtstudio.fxbox.media.models.tiktok.Comment;

import java.util.List;

public class CommentDetails extends ApiModel {
    private List<Comment> comments;
    private int total;
    private int cursor;
    private boolean hasMore;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
