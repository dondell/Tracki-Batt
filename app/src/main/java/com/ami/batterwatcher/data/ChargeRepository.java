package com.ami.batterwatcher.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;

import java.util.List;

class ChargeRepository {

    private ChargeDao mChargeDao;
    private LiveData<List<ChargeModel>> mAllAlerts;
    private LiveData<List<ChargeWithPercentageModel>> chargeWithPercentageModels;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    ChargeRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mChargeDao = db.chargeDao();
        mAllAlerts = mChargeDao.getAll();
        chargeWithPercentageModels = mChargeDao.getChargeModelWithPercentage();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<ChargeModel>> getAll() {
        return mAllAlerts;
    }
    LiveData<List<ChargeWithPercentageModel>> getAllChargeWithPercentage() {
        return chargeWithPercentageModels;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(ChargeModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mChargeDao.insert(model);
        });
    }

    void update(ChargeModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mChargeDao.update(model);
        });
    }

    void delete(ChargeModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mChargeDao.delete(model);
        });
    }
}
