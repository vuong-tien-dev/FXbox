package com.vtstudio.fxbox.helpers;

import androidx.annotation.NonNull;

import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;
import com.vtstudio.fxbox.media.models.tiktok.ShortsMusic;
import com.vtstudio.fxbox.media.models.tiktok.ShortsUser;
import com.vtstudio.fxbox.media.models.tiktok.ShortsVideo;
import com.vtstudio.fxbox.media.models.SocialUserType;
import com.vtstudio.fxbox.models.TiktokPackage;

public class ModelsParser {

    public static void parseTiktok(@NonNull ShortsDetails details, @NonNull TiktokPackage shorts) {
        ShortsVideo video = details.getShortsVideo();
        if (video == null) return;

        video.setLikeCount(shorts.getLikeCount());
        video.setShareCount(shorts.getShareCount());
        video.setCommentCount(shorts.getCommentCount());
        video.setPlayCount(shorts.getPlayCount());
        video.setUrl(shorts.getUrl());
        video.setShareUrl(shorts.getShareUrl());
        video.setDescription(shorts.getDescription());
        video.setDuration(shorts.getDuration());
        video.setAwemeId(shorts.getAwemeId());

        ShortsUser user = details.getShortsUser();
        if (user == null) user = new ShortsUser();
        long fxUserId = System.currentTimeMillis();
        user.setFxId(fxUserId);
        user.setUid(shorts.getFxAuthorAvatarId());
        user.setShareUrl(shorts.getAuthorShareUrl());
        user.setSignature(shorts.getAuthorSignature());
        user.setFollowerCount(shorts.getAuthorFollower());
        user.setFollowingCount(shorts.getAuthorFollowingCount());
        user.setTotalFavorite(shorts.getTotalFavorited());
        user.setNickName(shorts.getAuthorNickname());
        user.setSocialUserType(SocialUserType.USER_TIKTOK);

        video.setAuthorId(user.getUid());
        video.setFxAuthorId(user.getFxId());
        details.setShortsUser(user);

        ShortsMusic music = details.getShortsMusic();

        if (music == null) music = new ShortsMusic();
        long fxMusicId = System.currentTimeMillis();
        music.setFxId(fxMusicId);
        music.setTitle(shorts.getMusicInformation());
        music.setUrl(shorts.getMusicUrl());
        music.setId(shorts.getFxMusicAvatarId());
        music.setAuthor(shorts.getMusicAuthor());

        video.setMusicId(music.getId());
        video.setFxMusicId(music.getFxId());
        details.setShortsMusic(music);

    }
}
