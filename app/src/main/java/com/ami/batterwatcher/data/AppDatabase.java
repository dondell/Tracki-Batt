package com.ami.batterwatcher.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ami.batterwatcher.data.usage.ChargingSampleDao;
import com.ami.batterwatcher.data.usage.DischargingSampleDao;
import com.ami.batterwatcher.data.usage.UsageDao;
import com.ami.batterwatcher.viewmodels.AlertModel;
import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.ChargingSampleModel;
import com.ami.batterwatcher.viewmodels.DischargingSampleModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;
import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        AlertModel.class, ChargeModel.class, PercentageModel.class, UsageModel.class,
        ChargingSampleModel.class, DischargingSampleModel.class
}, version = 8)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlertDao userDao();

    public abstract ChargeDao chargeDao();

    public abstract PercentageDao percentageDao();

    public abstract UsageDao usageDao();

    public abstract ChargingSampleDao chargingSampleDao();

    public abstract DischargingSampleDao dischargingSampleDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "tracki-batt")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
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
                chargeDao.insert(new ChargeModel(2, "Dis-charging", "Draining"));

                PercentageDao percentageDao = INSTANCE.percentageDao();
                if (percentageDao.findByPercent(3, 1) == null)
                    percentageDao.insert(new PercentageModel(1, 3, 1, false));
                if (percentageDao.findByPercent(7, 1) == null)
                    percentageDao.insert(new PercentageModel(2, 7, 1, false));
                if (percentageDao.findByPercent(10, 1) == null)
                    percentageDao.insert(new PercentageModel(3, 10, 1, false));
                if (percentageDao.findByPercent(20, 1) == null)
                    percentageDao.insert(new PercentageModel(4, 20, 1, false));
                if (percentageDao.findByPercent(30, 1) == null)
                    percentageDao.insert(new PercentageModel(5, 30, 1, false));
                if (percentageDao.findByPercent(40, 1) == null)
                    percentageDao.insert(new PercentageModel(6, 40, 1, false));
                if (percentageDao.findByPercent(50, 1) == null)
                    percentageDao.insert(new PercentageModel(7, 50, 1, false));
                if (percentageDao.findByPercent(60, 1) == null)
                    percentageDao.insert(new PercentageModel(8, 60, 1, false));
                if (percentageDao.findByPercent(70, 1) == null)
                    percentageDao.insert(new PercentageModel(9, 70, 1, false));
                if (percentageDao.findByPercent(80, 1) == null)
                    percentageDao.insert(new PercentageModel(10, 80, 1, false));
                if (percentageDao.findByPercent(90, 1) == null)
                    percentageDao.insert(new PercentageModel(11, 90, 1, false));
                if (percentageDao.findByPercent(100, 1) == null)
                    percentageDao.insert(new PercentageModel(12, 100, 1, false));
                if (percentageDao.findByPercent(3, 2) == null)
                    percentageDao.insert(new PercentageModel(13, 3, 2, false));
                if (percentageDao.findByPercent(7, 2) == null)
                    percentageDao.insert(new PercentageModel(14, 7, 2, false));
                if (percentageDao.findByPercent(10, 2) == null)
                    percentageDao.insert(new PercentageModel(15, 10, 2, false));
                if (percentageDao.findByPercent(20, 2) == null)
                    percentageDao.insert(new PercentageModel(16, 20, 2, false));
                if (percentageDao.findByPercent(30, 2) == null)
                    percentageDao.insert(new PercentageModel(17, 30, 2, false));
                if (percentageDao.findByPercent(40, 2) == null)
                    percentageDao.insert(new PercentageModel(18, 40, 2, false));
                if (percentageDao.findByPercent(50, 2) == null)
                    percentageDao.insert(new PercentageModel(19, 50, 2, false));
                if (percentageDao.findByPercent(60, 2) == null)
                    percentageDao.insert(new PercentageModel(20, 60, 2, false));
                if (percentageDao.findByPercent(70, 2) == null)
                    percentageDao.insert(new PercentageModel(21, 70, 2, false));
                if (percentageDao.findByPercent(80, 2) == null)
                    percentageDao.insert(new PercentageModel(22, 80, 2, false));
                if (percentageDao.findByPercent(90, 2) == null)
                    percentageDao.insert(new PercentageModel(23, 90, 2, false));
                if (percentageDao.findByPercent(100, 2) == null)
                    percentageDao.insert(new PercentageModel(24, 100, 2, false));
            });
        }
    };

    /*
    https://stackoverflow.com/a/54049286/3651090
    Room only supports 5 data types which are TEXT, INTEGER, BLOB, REAL and UNDEFINED.
     */

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            /*
            SQLite does not have a boolean data type. Room maps it to an INTEGER column,
            mapping true to 1 and false to 0. I think below code would be work
             */
            database.execSQL("ALTER TABLE percentagemodel ADD COLUMN doneAlerted INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE usagemodel (" +
                    "usageId INTEGER  NOT NULL DEFAULT 1, " +
                    "packageName TEXT, " +
                    "percentage REAL NOT NULL DEFAULT 1, " +
                    "mAh REAL NOT NULL DEFAULT 1, " +
                    "current_mAh REAL NOT NULL DEFAULT 1, " +
                    "avg_mAh REAL NOT NULL DEFAULT 1, " +
                    "capacity_mAh REAL NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY(`usageId`))");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE usagemodel ADD COLUMN current_beforeLaunch REAL NOT NULL DEFAULT 1");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE usagemodel ADD COLUMN current_battery_percent REAL NOT NULL DEFAULT 1");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE ChargingSampleModel (" +
                    "chargingSampleId INTEGER  NOT NULL DEFAULT 1," +
                    "diffTime INTEGER NOT NULL DEFAULT 1," +
                    "PRIMARY KEY(`chargingSampleId`))");
            database.execSQL("CREATE TABLE DischargingSampleModel (" +
                    "dischargingSampleId INTEGER  NOT NULL DEFAULT 1," +
                    "diffTime INTEGER NOT NULL DEFAULT 1," +
                    "PRIMARY KEY(`dischargingSampleId`))");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE usagemodel ADD COLUMN timeDuration INTEGER NOT NULL DEFAULT 1");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE usagemodel ADD COLUMN timeStart INTEGER NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE usagemodel ADD COLUMN timeEnd INTEGER NOT NULL DEFAULT 1");
        }
    };

}
