package com.ami.batterwatcher.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ami.batterwatcher.viewmodels.AlertModel;

import java.util.List;

@Dao
public interface AlertDao {
    @Query("SELECT * FROM alertmodel")
    LiveData<List<AlertModel>> getAll();

    @Query("SELECT * FROM alertmodel WHERE id IN (:userIds)")
    List<AlertModel> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM alertmodel WHERE name LIKE :first AND " +
            "description LIKE :last LIMIT 1")
    AlertModel findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlertModel... users);

    @Update
    void update(AlertModel... users);

    @Delete
    void delete(AlertModel user);

    @Query("DELETE FROM alertmodel")
    void deleteAll();

}
