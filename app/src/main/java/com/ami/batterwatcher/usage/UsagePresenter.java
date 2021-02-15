package com.ami.batterwatcher.usage;

import android.app.AppOpsManager;
import android.app.usage.EventStats;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.usage.UsageViewModel;
import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static android.os.Process.myUid;

public class UsagePresenter implements UsageContract.Presenter {

    private static final int flags = PackageManager.GET_META_DATA |
            PackageManager.GET_SHARED_LIBRARY_FILES |
            PackageManager.GET_UNINSTALLED_PACKAGES;

    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private UsageContract.View view;
    private final BaseActivity context;

    public UsagePresenter(BaseActivity context, UsageContract.View view) {
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = context.getPackageManager();
        this.view = view;
        this.context = context;
    }

    private long getStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //calendar.add(Calendar.YEAR, -1);
        //calendar.add(Calendar.DAY_OF_MONTH, -6);
        calendar.add(Calendar.HOUR_OF_DAY, -8);
        return calendar.getTimeInMillis();
    }

    @Override
    public void retrieveUsageStats(List<UsageModel> usageModels) {
        if (!checkForPermission(context)) {
            view.onUserHasNoPermission();
            return;
        }

        List<String> installedApps = getInstalledAppList();
        Map<String, UsageStats> usageStats = usageStatsManager.queryAndAggregateUsageStats(getStartTime(), System.currentTimeMillis());
        List<UsageStats> stats = new ArrayList<>(usageStats.values());
        //List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, getStartTime(), System.currentTimeMillis());

        //Get Usage data per app
        List<UsageStatsWrapper> finalList = buildUsageStatsWrapper(installedApps, stats, usageModels);
        view.onUsageStatsRetrieved(finalList);
    }

    @Override
    public void retrieveEventStats() {
        List<String> installedApps = getInstalledAppList();
        List<EventStats> eventStats = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            eventStats = usageStatsManager.queryEventStats(UsageStatsManager.INTERVAL_BEST, getStartTime(), System.currentTimeMillis());
        }
    }

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), context.getPackageName());
        return mode == MODE_ALLOWED;
    }

    private List<String> getInstalledAppList() {
        List<ApplicationInfo> infos = packageManager.getInstalledApplications(flags);
        List<String> installedApps = new ArrayList<>();
        for (ApplicationInfo info : infos) {
//            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
//                // System app - do something here
//            } else {
            // User installed app?
            installedApps.add(info.packageName);
//            }
        }
        return installedApps;
    }

    private List<UsageStatsWrapper> buildUsageStatsWrapper(List<String> packageNames, List<UsageStats> usageStatses, List<UsageModel> usageModels) {
        List<UsageStatsWrapper> list = new ArrayList<>();
        for (String name : packageNames) {
            boolean added = false;
            for (UsageStats stat : usageStatses) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (name.equals(stat.getPackageName())) {
                        added = true;
                        //don't include zero timeInForeground
                        if (stat.getTotalTimeInForeground() > 0) {
                            list.add(fromUsageStat(stat, usageModels));
                        }
                    }
                }
            }
            /*if (!added) {
                list.add(fromUsageStat(name));
            }*/
        }

        Collections.sort(list);
        return list;
    }

    private UsageStatsWrapper fromUsageStat(String packageName) throws IllegalArgumentException {
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
            return new UsageStatsWrapper(null, packageManager.getApplicationIcon(ai), packageManager.getApplicationLabel(ai).toString(),
                    packageName);

        } catch (PackageManager.NameNotFoundException e) {
            //throw new IllegalArgumentException(e);
            Log.e("xxx", "xxx " + e.getMessage());
        }
        return null;
    }

    private UsageStatsWrapper fromUsageStat(UsageStats usageStats, List<UsageModel> usageModels) throws IllegalArgumentException {
        try {
            ApplicationInfo ai = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ai = packageManager.getApplicationInfo(usageStats.getPackageName(), 0);
                UsageModel usageModel = null;
                for (UsageModel um : usageModels) {
                    if (um.packageName.equalsIgnoreCase(usageStats.getPackageName())) {
                        usageModel = um;
                    }
                }
                return new UsageStatsWrapper(
                        usageStats,
                        packageManager.getApplicationIcon(ai),
                        packageManager.getApplicationLabel(ai).toString(),
                        usageStats.getPackageName(),
                        usageModel);
            }

        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }
}
