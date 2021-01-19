package com.ami.batterwatcher.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.AlertModel;
import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;

import java.util.List;

public class ChargeViewModel extends AndroidViewModel {

    private ChargeRepository mRepository;

    private final LiveData<List<ChargeModel>> mAllAlerts;
    private final LiveData<List<ChargeWithPercentageModel>> chargeWithPercentage;

    public ChargeViewModel(Application application) {
        super(application);
        mRepository = new ChargeRepository(application);
        mAllAlerts = mRepository.getAll();
        chargeWithPercentage = mRepository.getAllChargeWithPercentage();
    }

    public LiveData<List<ChargeModel>> getAll() {
        return mAllAlerts;
    }

    public LiveData<List<ChargeWithPercentageModel>> getAllChargeWithPercentageSets() {
        return chargeWithPercentage;
    }

    public void insert(ChargeModel model) {
        mRepository.insert(model);
    }

    public void update(ChargeModel model) {
        mRepository.update(model);
    }

    public void delete(ChargeModel model) {
        mRepository.delete(model);
    }
}
