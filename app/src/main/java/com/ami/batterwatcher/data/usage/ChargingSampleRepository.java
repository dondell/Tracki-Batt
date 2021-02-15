package com.ami.batterwatcher.data.usage;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.ami.batterwatcher.data.AppDatabase;
import com.ami.batterwatcher.viewmodels.ChargingSampleModel;

import java.util.List;

class ChargingSampleRepository {

    private ChargingSampleDao mChargingSampleDao;
    private LiveData<List<ChargingSampleModel>> mChargingSamples;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    ChargingSampleRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mChargingSampleDao = db.chargingSampleDao();
        mChargingSamples = mChargingSampleDao.getAll();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<ChargingSampleModel>> getAll() {
        return mChargingSamples;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(ChargingSampleModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mChargingSampleDao.insert(model);
        });
    }

    void update(ChargingSampleModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mChargingSampleDao.update(model);
        });
    }

    void delete(ChargingSampleModel model) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mChargingSampleDao.delete(model);
        });
    }
}
