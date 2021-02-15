package com.ami.batterwatcher.data.usage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.List;

@Dao
public interface UsageDao {
    @Query("SELECT * FROM usagemodel")
    LiveData<List<UsageModel>> getAll();

    @Query("SELECT * FROM usagemodel WHERE usageId IN (:userIds)")
    List<UsageModel> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM usagemodel WHERE packageName LIKE :first LIMIT 1")
    LiveData<UsageModel> findByName(String first);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UsageModel... models);

    @Update
    void update(UsageModel... models);

    @Delete
    void delete(UsageModel model);

    @Query("DELETE FROM chargemodel")
    void deleteAll();

}
