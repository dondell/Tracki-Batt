package com.ami.batterwatcher.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.AlertModel;

import java.util.List;

public class AlertViewModel extends AndroidViewModel {

    private AlertRepository mRepository;

    private final LiveData<List<AlertModel>> mAllAlerts;

    public AlertViewModel(Application application) {
        super(application);
        mRepository = new AlertRepository(application);
        mAllAlerts = mRepository.getAll();
    }

    public LiveData<List<AlertModel>> getAll() {
        return mAllAlerts;
    }

    public void insert(AlertModel model) {
        mRepository.insert(model);
    }

    public void update(AlertModel model) {
        mRepository.update(model);
    }

    public void delete(AlertModel model) {
        mRepository.delete(model);
    }
}
