package com.ami.batterwatcher.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.PercentageModel;

import java.util.List;

class PercentageRepository {

    private PercentageDao mPercentageDao;
    private LiveData<List<PercentageModel>> mAllChargingItems;
    private LiveData<List<PercentageModel>> mAllDischargingItems;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    PercentageRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mPercentageDao = db.percentageDao();
        mAllChargingItems = mPercentageDao.getAllChargingItems();
        mAllDischargingItems = mPercentageDao.getAllDischargingItems();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<PercentageModel>> getAllChargingItems() {
        return mAllChargingItems;
    }

    LiveData<List<PercentageModel>> getAllDischargingItems() {
        return mAllDischargingItems;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(PercentageModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPercentageDao.insert(model);
        });
    }

    void update(PercentageModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPercentageDao.update(model);
        });
    }

    void delete(PercentageModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPercentageDao.delete(model);
        });
    }
}
