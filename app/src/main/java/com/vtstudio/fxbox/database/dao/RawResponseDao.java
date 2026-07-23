package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.media.models.RawResponse;

import java.util.List;

@Dao
public interface RawResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     void insert(RawResponse rawResponse);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RawResponse> rawResponses);

    @Update
    void update(RawResponse rawResponse);

    @Delete
    void delete(RawResponse rawResponse);

    @Query("select * from RawResponse")
    List<RawResponse> getAll();

    @Query("select * from RawResponse where responseId = :responseId")
    RawResponse getById(String responseId);
}
