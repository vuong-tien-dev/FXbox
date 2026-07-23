package com.vtstudio.fxbox.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vtstudio.fxbox.models.TiktokPackage;

import java.util.List;

@Dao
public interface TiktokDAO {

    @Insert
     void insert (TiktokPackage shorts);

    @Insert
    void insertAll (List<TiktokPackage> shortsList);

    @Update
    void update (TiktokPackage shorts);

    @Delete
    void delete (TiktokPackage shorts);

    @Query("SELECT * FROM Shorts ")
    List<TiktokPackage> getAll ();

}
