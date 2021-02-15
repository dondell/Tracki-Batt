package com.ami.batterwatcher.usage;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.usage.UsageViewModel;

import java.util.Locale;

import static com.ami.batterwatcher.base.BaseActivity.remainingTimeForBatteryToDrainOrCharge;

public class UsageStatVH extends RecyclerView.ViewHolder {

    private BaseActivity baseActivity;
    private UsageViewModel usageViewModel;
    private ImageView appIcon;
    private TextView appName;
    private TextView lastTimeUsed;
    private TextView total_timeForeground;
    private TextView total_firstTimeStamp;
    private TextView total_lastTimeStamp;
    private TextView total_usageDetails;

    public UsageStatVH(View itemView, BaseActivity baseActivity) {
        super(itemView);
        this.baseActivity = baseActivity;
        usageViewModel = new ViewModelProvider
                .AndroidViewModelFactory(baseActivity.getApplication())
                .create(UsageViewModel.class);
        appIcon = itemView.findViewById(R.id.icon);
        appName = itemView.findViewById(R.id.title);
        lastTimeUsed = itemView.findViewById(R.id.last_used);
        total_timeForeground = itemView.findViewById(R.id.total_timeForeground);
        total_firstTimeStamp = itemView.findViewById(R.id.total_firstTimeStamp);
        total_lastTimeStamp = itemView.findViewById(R.id.total_lastTimeStamp);
        total_usageDetails = itemView.findViewById(R.id.total_usageDetails);
    }

    public void bindTo(UsageStatsWrapper usageStatsWrapper) {
        if (null != usageStatsWrapper) {
            if (null != usageStatsWrapper.getAppIcon())
                appIcon.setImageDrawable(usageStatsWrapper.getAppIcon());
            appName.setText(usageStatsWrapper.getAppName());
            if (usageStatsWrapper.getUsageStats() == null) {
                lastTimeUsed.setText(R.string.last_time_used_never);
            } else if (usageStatsWrapper.getUsageStats().getLastTimeUsed() == 0L) {
                lastTimeUsed.setText(R.string.last_time_used_never);
            } else {
                lastTimeUsed.setText(App.getApp().getString(R.string.last_time_used,
                        DateUtils.format(usageStatsWrapper)));
            }

            if (usageStatsWrapper.getUsageStats() != null) {
                /*total_firstTimeStamp.setText(App.getApp().getString(R.string.total_first_timestamp,
                        DateUtils.formatStandardDateTime(usageStatsWrapper.getUsageStats().getFirstTimeStamp())));*/

                /*
                Not accurate
                total_firstTimeStamp.setText(App.getApp().getString(R.string.total_first_timestamp,
                        " " + usageStatsWrapper.getUsageStats().getFirstTimeStamp()));*/

                total_firstTimeStamp.setVisibility(View.GONE);

                total_timeForeground.setText(App.getApp().getString(R.string.total_time_foreground_used,
                        DateUtils.formatTotTimeInForeground(usageStatsWrapper)));

                total_lastTimeStamp.setText(App.getApp().getString(R.string.total_last_timestamp,
                        DateUtils.formatStandardDateTime(usageStatsWrapper.getUsageStats().getLastTimeStamp())));
            }

            if (null != usageStatsWrapper.usageModel) {
                // total capacity mAh * battery percent / 100 to get remaining capacity mAh
                float remainingCap = (usageStatsWrapper.usageModel.capacity_mAh * usageStatsWrapper.usageModel.current_battery_percent / 100.0f);
                // remaining capacity mAh(base on battery percentage) e.g 50% /(divide) hour remaining
                int hourRemaining = baseActivity.store.getInt(BaseActivity.remainingTimeForBatteryToDrainOrChargeHr);
                int mnRemaining = baseActivity.store.getInt(BaseActivity.remainingTimeForBatteryToDrainOrChargeMn);
                float perHour_mAhUsage = remainingCap / hourRemaining;
                // get percentage of app usage per hour
                //float app_discharging_speed = (usageModel.mAh / perHour_mAhUsage) * 100;
                float app_discharging_speed = ((usageStatsWrapper.usageModel.capacity_mAh / remainingCap) * 100.0f);

                if (baseActivity.store.getString(remainingTimeForBatteryToDrainOrCharge)
                        .contains("Computing")) {
                    total_usageDetails.setText(baseActivity.getString(R.string.computing));
                } else {
                    total_usageDetails.setText(String.format(Locale.US, "Usage: %.2f%%/h, %s mAh"
                            , (app_discharging_speed > 100 ? 100 : app_discharging_speed),
                            usageStatsWrapper.usageModel.mAh)
                    );

                            /*
                            //This is the details
                            total_usageDetails.setText(String.format(Locale.US,
                                    "Details: %.2f%%/h\n" +
                                            "Capacity=%s mAh\n" +
                                            "Remaining Capacity=%s mAh\n" +
                                            "App Current=%s mAh\n" +
                                            "Time Remaining=%sh %sm\n"
                                    , (app_discharging_speed > 100 ? 100 : app_discharging_speed),
                                    usageModel.capacity_mAh,
                                    remainingCap,
                                    usageModel.mAh,
                                    hourRemaining, mnRemaining)
                            );*/

                    /*total_usageDetails.setText(String.format(Locale.US,
                                "Usage: before Launch=%s mAh, current=%s mAh, avg=%s mAh, " +
                                        "app=%s mAh, bat capacity=%s mAh, " +
                                        "usage= %.2f%%/h",
                                usageModel.current_beforeLaunch, usageModel.current_mAh,
                                usageModel.avg_mAh, usageModel.mAh, usageModel.capacity_mAh,
                                app_discharging_speed)
                        );*/

                }
            }
        }
    }
}
