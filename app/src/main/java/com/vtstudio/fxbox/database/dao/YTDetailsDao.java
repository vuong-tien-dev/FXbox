package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;
import com.vtstudio.fxbox.media.models.youtube.YTDetails;

import java.util.List;

@Dao
public interface YTDetailsDao {
    @Transaction
    @Query("SELECT * FROM yt_video WHERE id = :videoId")
    YTDetails getDetailsByVideoId(String videoId);

    @Query("SELECT * FROM yt_video WHERE NOT isLimited AND fxId NOT IN (:excludedIds) ORDER BY RANDOM() LIMIT :limit")
    List<YTDetails> getRemainingRecords(List<String> excludedIds, int limit);

    @Transaction
    @Query("SELECT video.* FROM yt_video video " +
            "JOIN yt_user user ON video.channelId = user.channelId " +
            "WHERE video.description LIKE '%' || :pattern || '%' " +
            "OR video.title LIKE '%' || :pattern || '%' " +
            "OR video.mediaStoreParent LIKE '%' || :pattern || '%' " +
            "OR video.mediaStoreName LIKE '%' || :pattern || '%' " +
            "OR user.channelName LIKE '%' || :pattern || '%' " +
            "OR user.uniqueId LIKE '%' || :pattern || '%' ")
    List<YTDetails> searchDetails(String pattern);
    @Transaction
    @Query("SELECT video.* FROM yt_video video " +
            "JOIN yt_user user ON video.channelId = user.channelId " +
            "WHERE (video.description LIKE '%' || :pattern || '%' " +
            "OR video.title LIKE '%' || :pattern || '%' " +
            "OR video.mediaStoreParent LIKE '%' || :pattern || '%' " +
            "OR video.mediaStoreName LIKE '%' || :pattern || '%' " +
            "OR user.channelName LIKE '%' || :pattern || '%' " +
            "OR user.uniqueId LIKE '%' || :pattern || '%' )" +
            "AND video.fxId NOT IN(:excludedIds) LIMIT :limit")
    List<YTDetails> searchDetails(String pattern, int limit, List<String> excludedIds);
    @Transaction
    @Query("SELECT video.* FROM yt_video video " +
            "WHERE (video.description LIKE '%#shorts%' " +
            "OR video.title LIKE '%#shorts%' " +
            "OR video.duration < 60000 ) " +
            "AND video.fxId NOT IN(:excludedIds) ORDER BY RANDOM() LIMIT :limit")
    List<YTDetails> generateShorts(int limit, List<String> excludedIds);
    @Transaction
    @Query("SELECT * FROM yt_video")
    List<YTDetails> getAllDetails();
    @Transaction
    @Query("SELECT * FROM yt_video WHERE channelId = :channelId")
    List<YTDetails> getDetailsByChannelId(String channelId);

    @Transaction
    @Query("SELECT * FROM yt_video WHERE fxId = :fxId")
    YTDetails getDetailsByFxId(long fxId);
}
