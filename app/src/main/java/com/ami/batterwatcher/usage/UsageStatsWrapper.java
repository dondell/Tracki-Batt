package com.ami.batterwatcher.usage;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.ami.batterwatcher.viewmodels.UsageModel;

public final class UsageStatsWrapper implements Comparable<UsageStatsWrapper> {

    private final UsageStats usageStats;
    private final Drawable appIcon;
    private final String appName;
    public String packageName;
    public UsageModel usageModel;
    public String speedComputation;
    public String usageComputation;
    public float percentage;

    public UsageStatsWrapper(UsageStats usageStats, Drawable appIcon, String appName) {
        this.usageStats = usageStats;
        this.appIcon = appIcon;
        this.appName = appName;
    }

    public UsageStatsWrapper(UsageStats usageStats, Drawable appIcon, String appName, String packageName) {
        this.usageStats = usageStats;
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
    }

    public UsageStatsWrapper(UsageStats usageStats, Drawable appIcon, String appName, String packageName, UsageModel usageModel) {
        this.usageStats = usageStats;
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
        this.usageModel = usageModel;
    }

    public UsageStats getUsageStats() {
        return usageStats;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getAppName() {
        return appName;
    }

    @Override
    public int compareTo(@NonNull UsageStatsWrapper usageStatsWrapper) {
        if (usageModel == null && usageStatsWrapper.usageModel != null) {
            return 1;
        } else if (usageStatsWrapper.usageModel == null && usageModel != null) {
            return -1;
        } else if (usageStatsWrapper.usageModel == null && usageModel == null) {
            return 0;
        } else {
            return Float.compare(usageStatsWrapper.percentage,
                    usageModel.percentage);
        }
    }
}
