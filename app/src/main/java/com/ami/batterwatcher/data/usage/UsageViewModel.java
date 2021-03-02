package com.ami.batterwatcher.data.usage;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.List;

public class UsageViewModel extends AndroidViewModel {

    private UsageRepository mRepository;

    private final LiveData<List<UsageModel>> mAllUsages;

    public UsageViewModel(Application application) {
        super(application);
        mRepository = new UsageRepository(application);
        mAllUsages = mRepository.getAllBaseDischargingSession();
    }

    public LiveData<List<UsageModel>> getAll() {
        return mAllUsages;
    }

    public LiveData<UsageModel> findUsage(String packageName) {
        return mRepository.findUsage(packageName);
    }

    public void insert(UsageModel model) {
        mRepository.insert(model);
    }

    public void update(UsageModel model) {
        mRepository.update(model);
    }

    public void delete(UsageModel model) {
        mRepository.delete(model);
    }
}
