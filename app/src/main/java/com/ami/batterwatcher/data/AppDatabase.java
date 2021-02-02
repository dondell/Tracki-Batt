package com.ami.batterwatcher.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ami.batterwatcher.viewmodels.AlertModel;
import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {AlertModel.class, ChargeModel.class, PercentageModel.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao userDao();

    public abstract ChargeDao chargeDao();

    public abstract PercentageDao percentageDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "tracki-batt")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                /*AlertDao dao = INSTANCE.userDao();
                dao.deleteAll();

                AlertModel alertModel = new AlertModel(3, 36,
                        3, "Battery level is now", "Test Alert",
                        "This is just a test");
                dao.insert(alertModel);*/

                ChargeDao chargeDao = INSTANCE.chargeDao();
                chargeDao.deleteAll();
                chargeDao.insert(new ChargeModel(1, "Charging", "Charging"));
                chargeDao.insert(new ChargeModel(2, "Dis-charging",  "Low batt"));

                PercentageDao percentageDao = INSTANCE.percentageDao();
                if(percentageDao.findByPercent(3, 1) == null)
                    percentageDao.insert(new PercentageModel(1, 3, 1, false));
                if(percentageDao.findByPercent(7, 1) == null)
                    percentageDao.insert(new PercentageModel(2, 7, 1, false));
                if(percentageDao.findByPercent(10, 1) == null)
                    percentageDao.insert(new PercentageModel(3, 10, 1, false));
                if(percentageDao.findByPercent(20, 1) == null)
                    percentageDao.insert(new PercentageModel(4, 20, 1, false));
                if(percentageDao.findByPercent(30, 1) == null)
                    percentageDao.insert(new PercentageModel(5, 30, 1, false));
                if(percentageDao.findByPercent(40, 1) == null)
                    percentageDao.insert(new PercentageModel(6, 40, 1, false));
                if(percentageDao.findByPercent(50, 1) == null)
                    percentageDao.insert(new PercentageModel(7, 50, 1, false));
                if(percentageDao.findByPercent(60, 1) == null)
                    percentageDao.insert(new PercentageModel(8, 60, 1, false));
                if(percentageDao.findByPercent(70, 1) == null)
                    percentageDao.insert(new PercentageModel(9, 70, 1, false));
                if(percentageDao.findByPercent(80, 1) == null)
                    percentageDao.insert(new PercentageModel(10, 80, 1, false));
                if(percentageDao.findByPercent(90, 1) == null)
                    percentageDao.insert(new PercentageModel(11, 90, 1, false));
                if(percentageDao.findByPercent(100, 1) == null)
                    percentageDao.insert(new PercentageModel(12, 100, 1, false));
                if(percentageDao.findByPercent(3, 2) == null)
                    percentageDao.insert(new PercentageModel(13, 3, 2, false));
                if(percentageDao.findByPercent(7, 2) == null)
                    percentageDao.insert(new PercentageModel(14, 7, 2, false));
                if(percentageDao.findByPercent(10, 2) == null)
                    percentageDao.insert(new PercentageModel(15, 10, 2, false));
                if(percentageDao.findByPercent(20, 2) == null)
                    percentageDao.insert(new PercentageModel(16, 20, 2, false));
                if(percentageDao.findByPercent(30, 2) == null)
                    percentageDao.insert(new PercentageModel(17, 30, 2, false));
                if(percentageDao.findByPercent(40, 2) == null)
                    percentageDao.insert(new PercentageModel(18, 40, 2, false));
                if(percentageDao.findByPercent(50, 2) == null)
                    percentageDao.insert(new PercentageModel(19, 50, 2, false));
                if(percentageDao.findByPercent(60, 2) == null)
                    percentageDao.insert(new PercentageModel(20, 60, 2, false));
                if(percentageDao.findByPercent(70, 2) == null)
                    percentageDao.insert(new PercentageModel(21, 70, 2, false));
                if(percentageDao.findByPercent(80, 2) == null)
                    percentageDao.insert(new PercentageModel(22, 80, 2, false));
                if(percentageDao.findByPercent(90, 2) == null)
                    percentageDao.insert(new PercentageModel(23, 90, 2, false));
                if(percentageDao.findByPercent(100, 2) == null)
                    percentageDao.insert(new PercentageModel(24, 100, 2, false));
            });
        }
    };

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            /*
            SQLite does not have a boolean data type. Room maps it to an INTEGER column,
            mapping true to 1 and false to 0. I think below code would be work
             */
            database.execSQL("ALTER TABLE percentagemodel " + " ADD COLUMN doneAlerted INTEGER NOT NULL DEFAULT 0");
        }
    };


}
