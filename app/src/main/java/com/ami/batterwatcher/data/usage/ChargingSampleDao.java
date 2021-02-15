package com.ami.batterwatcher.data.usage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ami.batterwatcher.viewmodels.ChargingSampleModel;
import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.List;

@Dao
public interface ChargingSampleDao {
    @Query("SELECT * FROM chargingsamplemodel")
    LiveData<List<ChargingSampleModel>> getAll();

    @Query("SELECT * FROM chargingsamplemodel WHERE chargingSampleId IN (:ids)")
    List<ChargingSampleModel> loadAllByIds(int[] ids);

    @Query("SELECT * FROM chargingsamplemodel WHERE chargingSampleId LIKE :id LIMIT 1")
    LiveData<ChargingSampleModel> findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChargingSampleModel... models);

    @Update
    void update(ChargingSampleModel... models);

    @Delete
    void delete(ChargingSampleModel model);

    @Query("DELETE FROM chargingsamplemodel")
    void deleteAll();

}
