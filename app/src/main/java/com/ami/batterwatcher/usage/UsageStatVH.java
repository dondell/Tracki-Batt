package com.ami.batterwatcher.usage;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;

import java.util.Locale;

import static com.ami.batterwatcher.base.BaseActivity.remainingTimeForBatteryToDrainOrCharge;

public class UsageStatVH extends RecyclerView.ViewHolder {

    private BaseActivity baseActivity;
    private ImageView appIcon;
    private TextView appName;
    private TextView lastTimeUsed;
    private TextView total_timeForeground;
    private TextView total_firstTimeStamp;
    private TextView total_lastTimeStamp;
    private TextView total_usageDetails;
    private ProgressBar progressBar;

    public UsageStatVH(View itemView, BaseActivity baseActivity) {
        super(itemView);
        this.baseActivity = baseActivity;
        appIcon = itemView.findViewById(R.id.icon);
        appName = itemView.findViewById(R.id.title);
        progressBar = itemView.findViewById(R.id.progressBar);
       /* lastTimeUsed = itemView.findViewById(R.id.last_used);
        total_timeForeground = itemView.findViewById(R.id.total_timeForeground);
        total_firstTimeStamp = itemView.findViewById(R.id.total_firstTimeStamp);
        total_usageDetails = itemView.findViewById(R.id.total_usageDetails);*/
        total_lastTimeStamp = itemView.findViewById(R.id.total_lastTimeStamp);
    }

    public void bindTo(UsageStatsWrapper usageStatsWrapper) {
        if (null != usageStatsWrapper) {
            if (null != usageStatsWrapper.getAppIcon())
                appIcon.setImageDrawable(usageStatsWrapper.getAppIcon());
            appName.setText(usageStatsWrapper.getAppName());
            /*if (usageStatsWrapper.getUsageStats() == null) {
                lastTimeUsed.setText(R.string.last_time_used_never);
            } else if (usageStatsWrapper.getUsageStats().getLastTimeUsed() == 0L) {
                lastTimeUsed.setText(R.string.last_time_used_never);
            } else {
                lastTimeUsed.setText(App.getApp().getString(R.string.last_time_used,
                        DateUtils.format(usageStatsWrapper)));
            }*/

            /*if (usageStatsWrapper.getUsageStats() != null) {
             *//*total_firstTimeStamp.setText(App.getApp().getString(R.string.total_first_timestamp,
                        DateUtils.formatStandardDateTime(usageStatsWrapper.getUsageStats().getFirstTimeStamp())));*//*

             *//*
                Not accurate
                total_firstTimeStamp.setText(App.getApp().getString(R.string.total_first_timestamp,
                        " " + usageStatsWrapper.getUsageStats().getFirstTimeStamp()));*//*

                total_firstTimeStamp.setVisibility(View.GONE);

                total_timeForeground.setText(App.getApp().getString(R.string.total_time_foreground_used,
                        DateUtils.formatTotTimeInForeground(usageStatsWrapper)));

                total_lastTimeStamp.setText(App.getApp().getString(R.string.total_last_timestamp,
                        DateUtils.formatStandardDateTime(usageStatsWrapper.getUsageStats().getLastTimeStamp())));
            }*/

            if (null != usageStatsWrapper.usageModel) {


                if (baseActivity.store.getString(remainingTimeForBatteryToDrainOrCharge)
                        .contains("Computing")) {
                    total_lastTimeStamp.setText(baseActivity.getString(R.string.computing));
                } else {
                    total_lastTimeStamp.setText(String.format(Locale.US, "%s",
                            usageStatsWrapper.usageComputation));
                    progressBar.setProgress((int) usageStatsWrapper.percentage);

                    /*total_usageDetails.setText(String.format(Locale.US, "" +
                                    "Speed: %.2f%%/h, %s mAh \n" +
                                    "Usage: %.2f%%, %s mAh"
                            , (app_discharging_speed > 100 ? 100 : app_discharging_speed), usageStatsWrapper.usageModel.mAh
                            , app_percentage_used, usageStatsWrapper.usageModel.mAh)
                    );*/

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

        itemView.setOnClickListener(v -> {
            goToAppInfo(usageStatsWrapper.packageName);
        });
    }

    public void goToAppInfo(String packageName) {
        try {
            //Open the specific App Info page:
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            baseActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //e.printStackTrace();
            //Open the generic Apps page:
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            baseActivity.startActivity(intent);

        }
    }
}
