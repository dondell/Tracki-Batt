package com.ami.batterwatcher.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ami.batterwatcher.viewmodels.PercentageModel;

import java.util.List;

@Dao
public interface PercentageDao {
    @Query("SELECT * FROM percentagemodel")
    LiveData<List<PercentageModel>> getAll();

    @Query("SELECT * FROM percentagemodel WHERE chargeModelId = 1")
    LiveData<List<PercentageModel>> getAllChargingItems();

    @Query("SELECT * FROM percentagemodel WHERE chargeModelId = 2")
    LiveData<List<PercentageModel>> getAllDischargingItems();

    @Query("SELECT * FROM percentagemodel WHERE percentageId IN (:userIds)")
    List<PercentageModel> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM percentagemodel WHERE percentage LIKE :first LIKE :last LIMIT 1")
    PercentageModel findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PercentageModel... models);

    @Update
    void update(PercentageModel... models);

    @Delete
    void delete(PercentageModel model);

    @Query("DELETE FROM chargemodel")
    void deleteAll();


}
