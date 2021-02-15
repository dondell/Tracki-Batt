package com.ami.batterwatcher.data.usage;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.data.AppDatabase;
import com.ami.batterwatcher.viewmodels.DischargingSampleModel;

import java.util.List;

class DischargingSampleRepository {

    private DischargingSampleDao mDischargingSampleDao;
    private LiveData<List<DischargingSampleModel>> mChargingSamples;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    DischargingSampleRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mDischargingSampleDao = db.dischargingSampleDao();
        mChargingSamples = mDischargingSampleDao.getAll();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<DischargingSampleModel>> getAll() {
        return mChargingSamples;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(DischargingSampleModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mDischargingSampleDao.insert(model);
        });
    }

    void update(DischargingSampleModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mDischargingSampleDao.update(model);
        });
    }

    void delete(DischargingSampleModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mDischargingSampleDao.delete(model);
        });
    }
}
