package com.vtstudio.fxbox.media.models.tiktok;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.vtstudio.fxbox.media.models.ApiModel;

@Entity (tableName = "comment")
public class Comment extends ApiModel {
    @PrimaryKey @NonNull private String id;
    private String content;
    private String uid;
    private String mediaVideoId;
    private int replyCount;
    private long createTime;
    private int diggCount;
    boolean isAuthorDigged;
    boolean isReplyComment;
    private String replyId;
    private String replyToReplyId;
    private String languge;

    @Ignore private ShortsUser shortsUser;

    public Comment(){}

    public Comment(@NonNull String id, String content, String mediaVideoId, String uid, int replyCount) {
        this.uid = uid;
        this.replyCount = replyCount;
        this.id = id;
        this.content = content;
        this.mediaVideoId = mediaVideoId;
    }

    public Comment(@NonNull String id, String content, String uid, String mediaVideoId, int replyCount, long createTime, int diggCount, boolean isAuthorDigged, boolean isReplyComment, String replyId, String replyToReplyId) {
        this.id = id;
        this.content = content;
        this.uid = uid;
        this.mediaVideoId = mediaVideoId;
        this.replyCount = replyCount;
        this.createTime = createTime;
        this.diggCount = diggCount;
        this.isAuthorDigged = isAuthorDigged;
        this.isReplyComment = isReplyComment;
        this.replyId = replyId;
        this.replyToReplyId = replyToReplyId;
    }

    public String getMediaVideoId() {
        return mediaVideoId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }


    public Comment(String id, String text) {
        this.id = id;
        this.content = text;
    }

    public String getContent() {
        return content;
    }

    public Comment(String text) {
        this.content = text;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMediaVideoId(String mediaVideoId) {
        this.mediaVideoId = mediaVideoId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getDiggCount() {
        return diggCount;
    }

    public void setDiggCount(int diggCount) {
        this.diggCount = diggCount;
    }

    public boolean isAuthorDigged() {
        return isAuthorDigged;
    }

    public void setAuthorDigged(boolean authorDigged) {
        isAuthorDigged = authorDigged;
    }

    public boolean isReplyComment() {
        return isReplyComment;
    }

    public void setReplyComment(boolean replyComment) {
        isReplyComment = replyComment;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getReplyToReplyId() {
        return replyToReplyId;
    }

    public void setReplyToReplyId(String replyToReplyId) {
        this.replyToReplyId = replyToReplyId;
    }

    public ShortsUser getShortsUser() {
        return shortsUser;
    }

    public void setShortsUser(ShortsUser shortsUser) {
        this.shortsUser = shortsUser;
    }

    public String getLanguge() {
        return languge;
    }

    public void setLanguge(String languge) {
        this.languge = languge;
    }
}