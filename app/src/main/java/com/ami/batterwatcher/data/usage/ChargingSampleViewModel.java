package com.ami.batterwatcher.data.usage;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.ChargingSampleModel;

import java.util.List;

public class ChargingSampleViewModel extends AndroidViewModel {

    private ChargingSampleRepository mRepository;

    private final LiveData<List<ChargingSampleModel>> mAllUsages;

    public ChargingSampleViewModel(Application application) {
        super(application);
        mRepository = new ChargingSampleRepository(application);
        mAllUsages = mRepository.getAll();
    }

    public LiveData<List<ChargingSampleModel>> getAll() {
        return mRepository.getAll();
    }

    public void insert(ChargingSampleModel model) {
        mRepository.insert(model);
    }

    public void update(ChargingSampleModel model) {
        mRepository.update(model);
    }

    public void delete(ChargingSampleModel model) {
        mRepository.delete(model);
    }
}
