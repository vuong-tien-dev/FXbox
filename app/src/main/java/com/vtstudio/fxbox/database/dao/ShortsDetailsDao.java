package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vtstudio.fxbox.media.models.tiktok.ShortsDetails;

import java.util.List;

@Dao
public interface ShortsDetailsDao {
    @Transaction
    @Query("SELECT * FROM shorts_video")
    List<ShortsDetails> getAllShortsDetails();

    @Transaction
    @Query("SELECT * FROM shorts_video LIMIT :limit OFFSET :offset")
    List<ShortsDetails> getShortsDetailsWithLimitOffset(int limit, int offset);

    @Query("SELECT COUNT(*) FROM shorts_video")
    int getShortsCount();

    @Transaction
    @Query("SELECT * FROM shorts_video WHERE NOT isLimited ORDER BY RANDOM() LIMIT :limit")
    List<ShortsDetails> getWithRandomLimit(int limit);

    // Thực hiện truy vấn với điều kiện đã tạo
    @Query("SELECT * FROM shorts_video WHERE (NOT isLimited AND fxId NOT IN (:excludedIds)) ORDER BY RANDOM() LIMIT :limit")
    List<ShortsDetails> getRemainingRecords(List<String> excludedIds, int limit);

    @Transaction
    @Query("SELECT * FROM shorts_video ORDER BY mediaStoreDayAdded ASC")
    List<ShortsDetails> getAllSortedByDayAddedAscending();

    @Transaction
    @Query("SELECT * FROM shorts_video ORDER BY mediaStoreDayAdded DESC")
    List<ShortsDetails> getAllSortedByDayAddedDescending();

    @Transaction
    @Query("SELECT * FROM shorts_video WHERE mediaStoreId = :mediaStoreId")
    ShortsDetails getDetailsFromMediaStoreId (String mediaStoreId);

    @Transaction
    @Query("SELECT * FROM shorts_video where fxId IN (:fxIdList)")
    List<ShortsDetails> getDetailsByListFxId(List<Long> fxIdList);

    @Transaction
    @Query("SELECT * FROM shorts_video where fxId = :fxId")
    ShortsDetails getDetailsByFxId(long fxId);
    @Transaction
    @Query("SELECT sv.* FROM shorts_video sv " +
            "JOIN shorts_user su ON sv.authorId = su.uid " +
            "JOIN shorts_music sm ON sv.musicId = sm.id " +
            "WHERE sv.description LIKE '%' || :keyword || '%' " +
            "OR su.nickname LIKE '%' || :keyword || '%' " +
            "OR sm.title LIKE '%' || :keyword || '%' " +
            "OR sv.description LIKE '%' || :keyword || '%' "  +
            "OR sv.mediaStoreParent LIKE '%' || :keyword || '%' " +
            "OR sv.mediaStoreName LIKE '%' || :keyword || '%'")
    List<ShortsDetails> searchVideosByCriteria(String keyword);
}