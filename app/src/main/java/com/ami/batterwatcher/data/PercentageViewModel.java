package com.ami.batterwatcher.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.PercentageModel;

import java.util.List;

public class PercentageViewModel extends AndroidViewModel {

    private PercentageRepository mRepository;

    private final LiveData<List<PercentageModel>> mAllChargingItems;
    private final LiveData<List<PercentageModel>> mAllDischargingItems;

    public PercentageViewModel(Application application) {
        super(application);
        mRepository = new PercentageRepository(application);
        mAllChargingItems = mRepository.getAllChargingItems();
        mAllDischargingItems = mRepository.getAllDischargingItems();
    }

    public LiveData<List<PercentageModel>> getAllChargingItems() {
        return mAllChargingItems;
    }

    public LiveData<List<PercentageModel>> getAllDischargingItems() {
        return mAllDischargingItems;
    }

    public void insert(PercentageModel model) {
        mRepository.insert(model);
    }

    public void update(PercentageModel model) {
        mRepository.update(model);
    }

    public void delete(PercentageModel model) {
        mRepository.delete(model);
    }
}
