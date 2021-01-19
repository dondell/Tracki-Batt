package com.ami.batterwatcher.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.viewmodels.AlertModel;

import java.util.List;

class AlertRepository {

    private AlertDao mAlertDao;
    private LiveData<List<AlertModel>> mAllAlerts;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    AlertRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mAlertDao = db.userDao();
        mAllAlerts = mAlertDao.getAll();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<AlertModel>> getAll() {
        return mAllAlerts;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(AlertModel alertModel) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAlertDao.insert(alertModel);
        });
    }

    void update(AlertModel alertModel) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAlertDao.update(alertModel);
        });
    }

    void delete(AlertModel alertModel) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAlertDao.delete(alertModel);
        });
    }
}
