package com.vtstudio.fxbox.media.utils;

import androidx.annotation.NonNull;

import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.vtstudio.fxbox.media.models.ApiModel;
import com.vtstudio.fxbox.media.models.Media;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.SocialUserType;
import com.vtstudio.fxbox.media.models.youtube.YTVideo;
import com.vtstudio.fxbox.models.TiktokPackage;

public class ModelsParser {
    public static void parseTiktokToShortsVideo(@NonNull TiktokPackage shorts, @NonNull ShortsVideo shortsVideo) {

        if (shorts.getAwemeId() != null) {
            shortsVideo.setAwemeId(shorts.getAwemeId());
        }

        if (shorts.getDescription() != null) {
            shortsVideo.setDescription(shorts.getDescription());
        }

        shortsVideo.setSocialMediaType(Media.MediaType.TYPE_SHORTS_VIDEO);
        shortsVideo.setIsImageList(shorts.isImageList());

        if (shorts.getFxAuthorAvatarId() != null) {
            shortsVideo.setAuthorId(shorts.getFxAuthorAvatarId());
        }

        if (shorts.getRegion() != null) {
            shortsVideo.setRegion(shorts.getRegion());
        }

        if(shorts.getShareUrl() != null) {
            shortsVideo.setShareUrl(shorts.getShareUrl());
        }

        if (shorts.getCommentCount() != 0) {
            shortsVideo.setCommentCount(shorts.getCommentCount());
        }

        if (shorts.getLikeCount() != 0) {
            shortsVideo.setLikeCount(shorts.getLikeCount());
        }

        if (shorts.getFxMusicAvatarId() != null) {
            shortsVideo.setMusicId(shorts.getFxMusicAvatarId());
        }

        if (shorts.getPlayCount() != 0) {
            shortsVideo.setPlayCount(shorts.getPlayCount());
        }

        if (shorts.getShareCount() != 0) {
            shortsVideo.setShareCount(shorts.getShareCount());
        }

        if (shorts.getShortsCreateTime() != 0) {
            shortsVideo.setShortsCreateTime(shorts.getShortsCreateTime());
        }

        if (shorts.getUrl() != null) {
            shortsVideo.setUrl(shorts.getUrl());
        }

        if (shorts.getShareUrl() != null) {
            shortsVideo.setShareUrl(shorts.getShareUrl());
        }

        if (shorts.getImageListPath() != null) {
            shortsVideo.setImageListPath(shorts.getImageListPath());
        }

        if (shorts.getImageIdList() != null) {
            shortsVideo.setImageIdList(shorts.getImageIdList());
        }

        if(shorts.getDescriptionTitle() != null && !shorts.getDescriptionTitle().isEmpty()) {
            shortsVideo.setDescriptionTitle(shorts.getDescriptionTitle());
        }

        if (shorts.getRawResponse() != null) {
            shortsVideo.setRawResponse(shorts.getRawResponse());
        }

        if(shorts.getCapcutSchema() != null && !shorts.getCapcutSchema().isEmpty()) {
            shortsVideo.setCapcutSchema(shorts.getCapcutSchema());
        }

        if(shorts.getCapcutTemplateId() != null && !shorts.getCapcutTemplateId().isEmpty()) {
            shortsVideo.setCapcutTemplateId(shorts.getCapcutTemplateId());
        }

        shortsVideo.setWarnAttempt(shorts.isWarnAttempt());
        shortsVideo.setWarningType(shorts.getWarningType());
        shortsVideo.setTimeDownloaded(shorts.getTimeDownloaded());
        setServerFromResponse(shortsVideo, shorts.getResponseServer());
    }

    public static void parseInformation (@NonNull ShortsVideo shortsVideo, @NonNull TiktokPackage shorts) {
        if (shorts.getCommentCount() != 0) {
            shortsVideo.setCommentCount(shorts.getCommentCount());
        }

        if (shorts.getLikeCount() != 0) {
            shortsVideo.setLikeCount(shorts.getLikeCount());
        }

        if (shorts.getPlayCount() != 0) {
            shortsVideo.setPlayCount(shorts.getPlayCount());
        }

        if (shorts.getShareCount() != 0) {
            shortsVideo.setShareCount(shorts.getShareCount());
        }

        if(shorts.getDescriptionTitle() != null && !shorts.getDescriptionTitle().isEmpty()) {
            shortsVideo.setDescriptionTitle(shorts.getDescriptionTitle());
        }

        if(shorts.getDescription() != null && !shorts.getDescription().isEmpty()) {
            shortsVideo.setDescription(shorts.getDescription());
        }

        if(shorts.getCapcutSchema() != null && !shorts.getCapcutSchema().isEmpty()) {
            shortsVideo.setCapcutSchema(shorts.getCapcutSchema());
        }

        if(shorts.getCapcutTemplateId() != null && !shorts.getCapcutTemplateId().isEmpty()) {
            shortsVideo.setCapcutTemplateId(shorts.getCapcutTemplateId());
        }

        if(shorts.getAnchorPosId() != null && !shorts.getAnchorPosId().isEmpty()) {
            shortsVideo.setAnchorPosId(shorts.getAnchorPosId());
        }

        if(shorts.getAnchorPosKeyword() != null && !shorts.getAnchorPosKeyword().isEmpty()) {
            shortsVideo.setAnchorPosKeyword(shorts.getAnchorPosKeyword());
        }

        if(!shortsVideo.isWarnAttempt()) {
            shortsVideo.setWarningType(shorts.getWarningType());
            shortsVideo.setWarnAttempt(shorts.isWarnAttempt());
        }
    }
    public static ShortsUser createShortsUserFromTiktok(@NonNull TiktokPackage shorts) {
        if (shorts.getFxAuthorAvatarId() == null) return null;

        long fxAuthorId = System.currentTimeMillis();
        ShortsUser user = new ShortsUser(shorts.getFxAuthorAvatarId(), shorts.getAuthorUniqueId(), shorts.getAuthorNickname(), shorts.getShareUrl(), shorts.getAuthorSignature(), shorts.getAuthorAvatar(), SocialUserType.USER_TIKTOK, shorts.getAuthorFollower(), shorts.getAuthorFollowingCount(), shorts.getTotalFavorited());
        user.setFxId(fxAuthorId);
        user.setTimeDownloaded(shorts.getTimeDownloaded());
        user.setVerified(shorts.getVerified());
        setServerFromResponse(user, shorts.getResponseServer());
        return user;
    }

    public static ShortsMusic createShortsMusicFromTiktok(@NonNull TiktokPackage shorts) {
        long fxMusicId = System.currentTimeMillis();
        ShortsMusic music = new ShortsMusic(shorts.getFxMusicAvatarId(), shorts.getMusicUrl(), shorts.getMusicInformation(), shorts.getMusicAuthor(), 0, null);
        music.setFxId(fxMusicId);

        music.setTimeDownloaded(shorts.getTimeDownloaded());
        setServerFromResponse(music, shorts.getResponseServer());
        return music;
    }

    public static void setServerFromResponse (@NonNull ApiModel model, String server){
        if(server != null) {
            model.setResponseServer(server);
        }
    }

    public static YTVideo fromVideoInfo (@NonNull VideoInfo info) {
        YTVideo video = new YTVideo("", "", "", "",0, 0, 0);
        video.setTitle(info.details().title());
        video.setDescription(info.details().description());
        video.setId(info.details().videoId());
        video.setPlayCount(info.details().viewCount());
        video.setLikeCount(info.details().averageRating());
        if(!info.details().thumbnails().isEmpty())
            video.setThumbnailUrl(info.details().thumbnails().get(info.details().thumbnails().size() - 1));
        return video;
    }

}
