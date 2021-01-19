package com.ami.batterwatcher.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;

import java.util.List;

@Dao
public interface ChargeDao {
    @Query("SELECT * FROM chargemodel")
    LiveData<List<ChargeModel>> getAll();

    @Query("SELECT * FROM chargemodel WHERE chargeId IN (:userIds)")
    List<ChargeModel> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM chargemodel WHERE name LIKE :first LIKE :last LIMIT 1")
    ChargeModel findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChargeModel... models);

    @Update
    void update(ChargeModel... models);

    @Delete
    void delete(ChargeModel model);

    @Query("DELETE FROM chargemodel")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM chargemodel")
    LiveData<List<ChargeWithPercentageModel>> getChargeModelWithPercentage();

}
