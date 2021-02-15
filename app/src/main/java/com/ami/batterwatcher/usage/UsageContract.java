package com.ami.batterwatcher.usage;

import android.app.usage.EventStats;

import com.ami.batterwatcher.viewmodels.UsageModel;

import java.util.List;

public interface UsageContract {

    interface View{
        void onUsageStatsRetrieved(List<UsageStatsWrapper> list);
        void onEventStatsRetrieved(List<EventStats> list);
        void onUserHasNoPermission();
    }

    interface Presenter{
        void retrieveUsageStats(List<UsageModel> usageModels);
        void retrieveEventStats();
    }
}
