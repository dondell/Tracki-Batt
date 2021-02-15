package com.ami.batterwatcher.data.usage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ami.batterwatcher.viewmodels.ChargingSampleModel;
import com.ami.batterwatcher.viewmodels.DischargingSampleModel;

import java.util.List;

@Dao
public interface DischargingSampleDao {
    @Query("SELECT * FROM dischargingsamplemodel")
    LiveData<List<DischargingSampleModel>> getAll();

    @Query("SELECT * FROM dischargingsamplemodel WHERE dischargingSampleId IN (:ids)")
    List<DischargingSampleModel> loadAllByIds(int[] ids);

    @Query("SELECT * FROM dischargingsamplemodel WHERE dischargingSampleId LIKE :id LIMIT 1")
    LiveData<DischargingSampleModel> findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DischargingSampleModel... models);

    @Update
    void update(DischargingSampleModel... models);

    @Delete
    void delete(DischargingSampleModel model);

    @Query("DELETE FROM dischargingsamplemodel")
    void deleteAll();

}
