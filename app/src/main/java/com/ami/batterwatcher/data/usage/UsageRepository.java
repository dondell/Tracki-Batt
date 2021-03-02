package com.ami.batterwatcher.data.usage;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.data.AppDatabase;
import com.ami.batterwatcher.viewmodels.UsageModel;
import com.google.gson.Gson;

import java.util.List;

import static com.ami.batterwatcher.service.BatteryService.dischargingStartTimeCopy;

class UsageRepository {

    private UsageDao mUsageDao;
    private LiveData<List<UsageModel>> mAllAlerts;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    UsageRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mUsageDao = db.usageDao();
        mAllAlerts = mUsageDao.getAllBaseDischargingSession(dischargingStartTimeCopy);
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<UsageModel>> getAllBaseDischargingSession() {
        return mAllAlerts;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(UsageModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mUsageDao.insert(model);
        });
    }

    void update(UsageModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mUsageDao.update(model);
        });
    }

    void delete(UsageModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mUsageDao.delete(model);
        });
    }

    LiveData<UsageModel> findUsage(String packageName) {
        return mUsageDao.findByName(packageName);
    }
}
