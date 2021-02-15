package com.ami.batterwatcher.data.usage;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.DischargingSampleModel;

import java.util.List;

public class DischargingSampleViewModel extends AndroidViewModel {

    private DischargingSampleRepository mRepository;

    private final LiveData<List<DischargingSampleModel>> mAllUsages;

    public DischargingSampleViewModel(Application application) {
        super(application);
        mRepository = new DischargingSampleRepository(application);
        mAllUsages = mRepository.getAll();
    }

    public LiveData<List<DischargingSampleModel>> getAll() {
        return mRepository.getAll();
    }

    public void insert(DischargingSampleModel model) {
        mRepository.insert(model);
    }

    public void update(DischargingSampleModel model) {
        mRepository.update(model);
    }

    public void delete(DischargingSampleModel model) {
        mRepository.delete(model);
    }
}
