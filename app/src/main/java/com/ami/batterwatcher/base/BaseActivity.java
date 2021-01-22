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
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ami.batterwatcher.BuildConfig;
import com.ami.batterwatcher.R;
import com.ami.batterwatcher.util.PrefStore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    private ActionBar actionBar;
    public LayoutInflater inflater;
    public PrefStore store;
    //SharedPreference keys
    public static final String previousBatValueKey = "previousBatValueKey";//int
    public static final String isSleepModeDisabledKey = "isSleepModeDisabledKey";//boolean
    public static final String isSwitchOff = "isSwitchOff";//boolean
    public static final String startTimeValueKey = "startTimeValueKey";//time
    public static final String endTimeValueKey = "endTimeValueKey";//time
    public static final String enableRepeatedAlertForPercentage = "enableRepeatedAlertForPercentage";//boolean This will repeat TTS
    public static final String checkIntervalOnBatteryServiceLevelChecker = "checkIntervalOnBatteryServiceLevelChecker";//int in minute
    public static final String timeStampAlertLastPlayed = "timeStampAlertLastPlayed";//long Time when the last time alert was sounded
    public static final String ignoreSystemAudioProfile = "ignoreSystemAudioProfile";//boolean
    public static final String playSoundWithMaxVolume = "playSoundWithMaxVolume";//boolean
    public static final String disableAlertDuringCall = "disableAlertDuringCall";//boolean
    public static final String startTimeHr = "startTimeHr";//int
    public static final String startTimeMn = "startTimeMn";//int
    public static final String stopTimeHr = "stopTimeHr";//int
    public static final String stopTimeMn = "stopTimeMn";//int
    public static final String ttsVoiceType = "ttsVoiceType"; //int 1=male, 2=female
    public static final String isCharging = "isCharging";//boolean
    public static final String ttsFemale = "ttsFemale";//String
    public static final String ttsMale = "ttsMale";//String
    private static final int REQUEST_CODE_MAX_VOL = 1001;
    private SimpleDateFormat formatter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the layout binding
        setContentView(setLayoutBinding());

        //or set via layout files directly
        //setContentView(setLayout());

        mContext = BaseActivity.this;
        store = new PrefStore(mContext);
        formatter = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
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

    public static void logStatic(String logMessage) {
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

    public long saveTime() {
        formatter.setLenient(false);
        Date curDate = new Date();
        long curMillis = curDate.getTime();
        String curTime = formatter.format(curDate);
        store.saveLong(timeStampAlertLastPlayed, curMillis);
        return curMillis;
    }

    public boolean isTimeIntervalDone() {
        /*String oldTime = "05.01.2011, 12:45";
        Date oldDate = null;
        try {
            oldDate = formatter.parse(oldTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (oldDate != null) {
            long oldMillis = oldDate.getTime();
        }*/

        Date curDate = new Date();
        int hours;
        int min = 0;
        int days;
        long curMillis = curDate.getTime();
        long prevTime = store.getLong(timeStampAlertLastPlayed, -1);
        if (prevTime != -1) {
            long diff = curMillis - prevTime;
            days = (int) (diff / (1000 * 60 * 60 * 24));
            hours = (int) ((diff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60 * 24));
            min = (int) (diff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
            hours = (hours < 0 ? -hours : hours);
            if (min >= store.getInt(checkIntervalOnBatteryServiceLevelChecker)) {
                log("interval is now pass " + min + " minutes");
                store.saveLong(timeStampAlertLastPlayed, curMillis);
                return true;
            }
        } else {
            log("no timeStampAlertLastPlayed set. Setting now.");
            store.saveLong(timeStampAlertLastPlayed, curMillis);
        }
        return false;
    }

    public void showTimePicker(TimePicker.OnTimeChangedListener timeChangedListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_timepicker, null);
        dialogBuilder.setView(dialogView);

        Button positiveBT = dialogView.findViewById(R.id.button_ok);

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        TimePicker timePicker_time = dialogView.findViewById(R.id.timePicker_time);
        timePicker_time.setIs24HourView(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker_time.setHour(hour);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker_time.setMinute(minute);
        }
        timePicker_time.setOnTimeChangedListener(timeChangedListener);

        AlertDialog alertDialog = dialogBuilder.create();
        if (!isFinishing())
            alertDialog.show();
        positiveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
}
