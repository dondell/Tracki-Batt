package com.ami.batterwatcher.base;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ami.batterwatcher.BuildConfig;
import com.ami.batterwatcher.util.PrefStore;

public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    private ActionBar actionBar;
    public LayoutInflater inflater;
    public PrefStore store;
    //SharedPreference keys
    public static String previousBatValueKey = "previousBatValueKey";//int
    public static String isSleepModeDisabledKey = "isSleepModeDisabledKey";//boolean
    public static String isSwitchOff = "isSwitchOff";//boolean
    public static String startTimeValueKey = "startTimeValueKey";//time
    public static String endTimeValueKey = "endTimeValueKey";//time
    public static String enableRepeatedAlertForPercentage = "enableRepeatedAlertForPercentage";//boolean This will repeat TTS
    public static String checkIntervalOnBatteryServiceLevelChecker = "checkIntervalOnBatteryServiceLevelChecker";//int in minute
    public static String ignoreSystemAudioProfile = "ignoreSystemAudioProfile";//boolean
    public static String playSoundWithMaxVolume = "playSoundWithMaxVolume";//boolean
    public static String disableAlertDuringCall = "disableAlertDuringCall";//boolean
    public static String startTimeHr = "startTimeHr";//int
    public static String startTimeMn = "startTimeMn";//int
    public static String stopTimeHr = "stopTimeHr";//int
    public static String stopTimeMn = "stopTimeMn";//int
    private static final int REQUEST_CODE_MAX_VOL = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the layout binding
        setContentView(setLayoutBinding());

        //or set via layout files directly
        //setContentView(setLayout());

        mContext = BaseActivity.this;
        store = new PrefStore(mContext);
        setData();
        setView();
        setViews();
        setListeners();
    }

    protected abstract int setLayout();

    protected abstract View setLayoutBinding();

    protected abstract void setViews();

    protected abstract void setListeners();

    protected abstract void setData();

    private void setView() {
        actionBar = getSupportActionBar();
    }

    public void showBackButton(boolean flag) {
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(flag);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void log(String logMessage) {
        if (BuildConfig.DEBUG) {
            Log.e("xxx", "xxx " + logMessage);
        }
    }

    public void setActivityTitle(String title) {
        setTitle(title);
    }

    public void showDialogOkButton(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isCharging(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    public boolean isCallActive(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getMode() == AudioManager.MODE_IN_CALL;
    }

    public boolean checkModifyMaxVolumePermissionNoPrompt() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        boolean notifPermissionGranted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            notifPermissionGranted = false;
        }

        return notifPermissionGranted;
    }

    public boolean checkModifyMaxVolumePermission() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        boolean notifPermissionGranted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            notifPermissionGranted = false;
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent, REQUEST_CODE_MAX_VOL);
        }

        return notifPermissionGranted;
    }

    public String convertTimePickerTime(int hourOfDay, int selectedMinute) {
        String AM_PM = " AM";
        String mm_precede = "";
        if (hourOfDay >= 12) {
            AM_PM = " PM";
            if (hourOfDay >= 13 && hourOfDay < 24) {
                hourOfDay -= 12;
            } else {
                hourOfDay = 12;
            }
        } else if (hourOfDay == 0) {
            hourOfDay = 12;
        }
        if (selectedMinute < 10) {
            mm_precede = "0";
        }
        return "" + hourOfDay + ":" + mm_precede + selectedMinute + AM_PM;
    }
}
