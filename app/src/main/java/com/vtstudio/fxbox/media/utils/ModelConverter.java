package com.vtstudio.fxbox.media.utils;

import com.vtstudio.fxbox.media.models.tiktok.Comment;
import com.vtstudio.fxbox.media.models.FxMediaVideo;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;

public class ModelConverter {
    public static ShortsVideo convertToShortsVideo3(ShortsVideo shortsVideo) {
        ShortsVideo shortsVideo3 = new ShortsVideo(
                shortsVideo.getMediaStoreId(),
                shortsVideo.getMediaStoreName(),
                shortsVideo.getMediaStorePath(),
                shortsVideo.getMediaStoreParent(),
                shortsVideo.getMediaStoreDayAdded(),
                shortsVideo.getSize(),
                shortsVideo.getDuration()
        );

        shortsVideo3.setAwemeId(shortsVideo.getAwemeId());
        shortsVideo3.setUrl(shortsVideo.getUrl());
        shortsVideo3.setShareUrl(shortsVideo.getShareUrl());
        shortsVideo3.setFxCloudDatabaseUrl(shortsVideo.getFxCloudDatabaseUrl());
        shortsVideo3.setDynamicCover(shortsVideo.getDynamicCover());
        shortsVideo3.setDescription(shortsVideo.getDescription());
        shortsVideo3.setAuthorId(shortsVideo.getAuthorId());
        shortsVideo3.setMusicId(shortsVideo.getMusicId());
        shortsVideo3.setLikeCount(shortsVideo.getLikeCount());
        shortsVideo3.setCommentCount(shortsVideo.getCommentCount());
        shortsVideo3.setShareCount(shortsVideo.getShareCount());
        shortsVideo3.setPlayCount(shortsVideo.getPlayCount());

        // Copy các thuộc tính khác từ FxMediaVideo
        shortsVideo3.setSocialMediaType(shortsVideo.getSocialMediaType());
        shortsVideo3.setFxThumbnailId(shortsVideo.getFxThumbnailId());
        shortsVideo3.setFxNotificationLargeIconId(shortsVideo.getFxNotificationLargeIconId());
        shortsVideo3.setFxDescription(shortsVideo.getFxDescription());
        shortsVideo3.setFxAuthorId(shortsVideo.getFxAuthorId());
        shortsVideo3.setFxMusicId(shortsVideo.getFxMusicId());
        shortsVideo3.setWidth(shortsVideo.getWidth());
        shortsVideo3.setHeight(shortsVideo.getHeight());

        return shortsVideo3;
    }

    public static FxMediaVideo convertFromFxMediaVideo(FxMediaVideo fxMediaVideo) {
        FxMediaVideo fxMediaVideo3 = new FxMediaVideo(
                fxMediaVideo.getMediaStoreId(),
                fxMediaVideo.getMediaStoreName(),
                fxMediaVideo.getMediaStorePath(),
                fxMediaVideo.getMediaStoreParent(),
                fxMediaVideo.getMediaStoreDayAdded(),
                fxMediaVideo.getSize(),
                fxMediaVideo.getDuration()
        );

        // Copy các thuộc tính khác từ FxMediaVideo
        fxMediaVideo3.setFxThumbnailId(fxMediaVideo.getFxThumbnailId());
        fxMediaVideo3.setFxNotificationLargeIconId(fxMediaVideo.getFxNotificationLargeIconId());
        fxMediaVideo3.setFxDescription(fxMediaVideo.getFxDescription());
        fxMediaVideo3.setFxAuthorId(fxMediaVideo.getFxAuthorId());
        fxMediaVideo3.setFxMusicId(fxMediaVideo.getFxMusicId());
        fxMediaVideo3.setWidth(fxMediaVideo.getWidth());
        fxMediaVideo3.setHeight(fxMediaVideo.getHeight());

        return fxMediaVideo3;
    }


    public static Comment convertFromComment(Comment comment) {
        Comment comment3 = new Comment(
                comment.getId(),
                comment.getContent(),
                comment.getUid(),
                comment.getMediaVideoId(),
                comment.getReplyCount(),
                comment.getCreateTime(),
                comment.getDiggCount(),
                comment.isAuthorDigged(),
                comment.isReplyComment(),
                comment.getReplyId(),
                comment.getReplyToReplyId()
        );
        return comment3;
    }

    public static ShortsUser convertFromShortsUser(ShortsUser shortsUser) {
        ShortsUser shortsUser3 = new ShortsUser(
                shortsUser.getUid(),
                shortsUser.getUniqueId(),
                shortsUser.getNickName(),
                shortsUser.getShareUrl(),
                shortsUser.getSignature(),
                shortsUser.getAvatarUrl(),
                shortsUser.getSocialUserType(),
                shortsUser.getFollowerCount(),
                shortsUser.getFollowingCount(),
                shortsUser.getTotalFavorite()
        );

        // Copy các thuộc tính khác từ ShortsUser
        shortsUser3.setFxId(shortsUser.getFxId());

        return shortsUser3;
    }

    public static ShortsMusic convertFromShortsMusic(ShortsMusic shortsMusic) {
        ShortsMusic shortsMusic3 = new ShortsMusic(
                shortsMusic.getId(),
                shortsMusic.getUrl(),
                shortsMusic.getTitle(),
                shortsMusic.getAuthor(),
                shortsMusic.getDuration(),
                shortsMusic.getAuthorId()
        );

        // Copy các thuộc tính khác từ ShortsMusic
        shortsMusic3.setFxId(shortsMusic.getFxId());

        return shortsMusic3;
    }

}
