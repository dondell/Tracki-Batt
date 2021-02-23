package com.ami.batterwatcher.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.ChargeViewModel;
import com.ami.batterwatcher.data.usage.ChargingSampleViewModel;
import com.ami.batterwatcher.data.usage.DischargingSampleViewModel;
import com.ami.batterwatcher.data.usage.UsageViewModel;
import com.ami.batterwatcher.util.BatteryUtil;
import com.ami.batterwatcher.util.PrefStore;
import com.ami.batterwatcher.util.fgchecker.AppChecker;
import com.ami.batterwatcher.view.MainActivity;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;
import com.ami.batterwatcher.viewmodels.ChargingSampleModel;
import com.ami.batterwatcher.viewmodels.DischargingSampleModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;
import com.ami.batterwatcher.viewmodels.UsageModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.ami.batterwatcher.base.BaseActivity.announceOnFastCharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnFastDisharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnMediumCharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnMediumDisharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnSlowCharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnSlowDisharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnSuperFastCharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnSuperFastDisharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnVertFastCharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnVertFastDisharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnVerySlowCharging;
import static com.ami.batterwatcher.base.BaseActivity.announceOnVerySlowDisharging;
import static com.ami.batterwatcher.base.BaseActivity.bat_capacity;
import static com.ami.batterwatcher.base.BaseActivity.bat_current;
import static com.ami.batterwatcher.base.BaseActivity.bat_current_avg;
import static com.ami.batterwatcher.base.BaseActivity.bat_health;
import static com.ami.batterwatcher.base.BaseActivity.bat_isPresent;
import static com.ami.batterwatcher.base.BaseActivity.bat_level;
import static com.ami.batterwatcher.base.BaseActivity.bat_plugged;
import static com.ami.batterwatcher.base.BaseActivity.bat_scale;
import static com.ami.batterwatcher.base.BaseActivity.bat_status;
import static com.ami.batterwatcher.base.BaseActivity.bat_technology;
import static com.ami.batterwatcher.base.BaseActivity.bat_temperature;
import static com.ami.batterwatcher.base.BaseActivity.bat_voltage;
import static com.ami.batterwatcher.base.BaseActivity.chargingAnnouncePercentExactValue;
import static com.ami.batterwatcher.base.BaseActivity.chargingSampleId;
import static com.ami.batterwatcher.base.BaseActivity.checkIntervalOnBatteryServiceLevelCheckerForCharging;
import static com.ami.batterwatcher.base.BaseActivity.checkIntervalOnBatteryServiceLevelCheckerForDisCharging;
import static com.ami.batterwatcher.base.BaseActivity.checkModifyMaxVolumePermissionNoPrompt;
import static com.ami.batterwatcher.base.BaseActivity.disChargingSampleId;
import static com.ami.batterwatcher.base.BaseActivity.disableAlertDuringCall;
import static com.ami.batterwatcher.base.BaseActivity.dischargingAnnouncePercentExactValue;
import static com.ami.batterwatcher.base.BaseActivity.enableRepeatedAlertForPercentageForCharging;
import static com.ami.batterwatcher.base.BaseActivity.enableRepeatedAlertForPercentageForDisCharging;
import static com.ami.batterwatcher.base.BaseActivity.ignoreSystemAudioProfile;
import static com.ami.batterwatcher.base.BaseActivity.includePercentAtTheEndOfAnnouncementCharging;
import static com.ami.batterwatcher.base.BaseActivity.includePercentAtTheEndOfAnnouncementDisCharging;
import static com.ami.batterwatcher.base.BaseActivity.isSwitchOff;
import static com.ami.batterwatcher.base.BaseActivity.isTimeInBetweenSleepMode;
import static com.ami.batterwatcher.base.BaseActivity.isTimeIntervalDone;
import static com.ami.batterwatcher.base.BaseActivity.logStatic;
import static com.ami.batterwatcher.base.BaseActivity.playLoudBeepOnBelowTenPercent;
import static com.ami.batterwatcher.base.BaseActivity.playSoundWithMaxVolume;
import static com.ami.batterwatcher.base.BaseActivity.previousBatValueKey;
import static com.ami.batterwatcher.base.BaseActivity.remainingTimeForBatteryToDrainOrCharge;
import static com.ami.batterwatcher.base.BaseActivity.remainingTimeForBatteryToDrainOrChargeDy;
import static com.ami.batterwatcher.base.BaseActivity.remainingTimeForBatteryToDrainOrChargeHr;
import static com.ami.batterwatcher.base.BaseActivity.remainingTimeForBatteryToDrainOrChargeMn;
import static com.ami.batterwatcher.base.BaseActivity.startTimeLong;
import static com.ami.batterwatcher.base.BaseActivity.stopTimeLong;

public class BatteryService extends Service {

    public static final int DEFAULT_CHECK_BATTERY_INTERVAL = 10000;
    private static final int CHECK_BATTERY_INTERVAL = DEFAULT_CHECK_BATTERY_INTERVAL;

    private double currentBattLevel;
    final Handler handler = new Handler(Looper.getMainLooper());
    private PrefStore store;
    private static final int ID_SERVICE = 104;
    private TextToSpeech tts;
    private Voice defaultTTSVoice;
    private boolean initTTSSuccessfull = false;
    private boolean donePlayingLoadBeepBelowTenPercent = false;
    private AudioManager audio;
    private int currentMusicVolume;
    private int currentRingtoneVolume;
    private int maxMusicVolume, maxRingVolume;
    private List<ChargeWithPercentageModel> chargeWithPercentageModels;
    private IBinder mBinder = new LocalBinder();
    private BatteryManager myBatteryManager;
    private BatteryUtil batteryUtil;
    private AppChecker appChecker;
    private String packageDetected = "";
    private String lastPackageDetected = "";
    private LiveData<UsageModel> usageModel;
    private int currentBeforeAppOnForeground;

    //View Models
    private ChargeViewModel chargeViewModel;
    private UsageViewModel usageViewModel;
    private ChargingSampleViewModel chargingSampleViewModel;
    private DischargingSampleViewModel dischargingSampleViewModel;

    private int mTempPreviousBatValueKey;

    /**
     * Lists to store the tracked data for calculating charging and discharging time.
     */
    //private ArrayList<Long> batteryDischargingTimes, batteryChargingTimes;
    private LiveData<List<ChargingSampleModel>> chargeSampleList;
    private LiveData<List<DischargingSampleModel>> dischargeSampleList;
    private List<Integer> appCurrentSampling = new ArrayList<>();
    /**
     * A broadcast receiver for tracking the level changes and the battery usage.
     */
    private BroadcastReceiver batInfoReceiver = new BroadcastReceiver() {
        int oldDischargeLevel = 101, oldChargeLevel = 0;
        long oldDischargeTime = 0, oldChargeTime = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            boolean isPresent = intent.getBooleanExtra("present", false);
            if (store != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
                        && null != myBatteryManager) {
                    int current = 0, averageCurrent = 0;
                    current = myBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                    averageCurrent = myBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
                    store.setInt(bat_current, current);
                    store.setInt(bat_current_avg, averageCurrent);
                    int chargeCounter = myBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                    int capacity = myBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

                    if (chargeCounter == Integer.MIN_VALUE || capacity == Integer.MIN_VALUE) {
                    } else {
                        //store.setInt(bat_capacity, (chargeCounter / capacity) * 100);
                    }

                }
                store.setBoolean(bat_isPresent, isPresent);
                store.setInt(bat_level, level);
                store.setInt(bat_scale, scale);
                store.saveString(bat_technology, technology);
                store.setInt(bat_voltage, voltage);
                store.setInt(bat_temperature, temperature);
                store.setInt(bat_health, health);
                store.setInt(bat_status, status);
                store.setInt(bat_plugged, plugged);
            }

            //This will only work if activity is running. So we fully transfer inside this service.
            Intent intentBroadcast = new Intent("YourAction");
            Bundle bundle = new Bundle();
            bundle.putInt("batteryLevel", level);
            intentBroadcast.putExtras(bundle);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);

            if (level >= 0 && scale > 0) {
                currentBattLevel = (level * 100.0) / scale;
            }

            boolean charging = BaseActivity.isCharging(getApplicationContext());
            if ((!charging) && (level <= 100)) {

                if (level < oldDischargeLevel) {

                    long time = System.currentTimeMillis();
                    if (oldDischargeTime != 0) {
                        long diffTime = time - oldDischargeTime;
                        saveSamples(false, diffTime);
                        //batteryDischargingTimes.add(diffTime);
                        publishDischargingText();
                    } else {
                        onCalculatingDischargingTime();
                    }
                    oldDischargeTime = time;
                    oldDischargeLevel = level;
                } else if (null != dischargeSampleList.getValue() && dischargeSampleList.getValue().size() > 0) {
                    publishDischargingText();
                }

                //batteryChargingTimes.clear();
                oldChargeLevel = 0;
                oldChargeTime = 0;

            }

            if (charging) {

                if (oldChargeLevel < level) {

                    long time = System.currentTimeMillis();
                    if (oldChargeTime != 0) {
                        long diffTime = time - oldChargeTime;
                        saveSamples(true, diffTime);
                        //batteryChargingTimes.add(diffTime);
                        publishChargingText();
                    } else {
                        onCalculatingChargingTime();
                    }
                    oldChargeTime = time;
                    oldChargeLevel = level;

                } else if (null != chargeSampleList.getValue() && chargeSampleList.getValue().size() > 0) {
                    publishChargingText();
                }

                if (level == 100) {
                    onFullBattery();
                }

                //batteryDischargingTimes.clear();
                oldDischargeLevel = 100;
                oldDischargeTime = 0;

            }

            logStatic("xxx " + currentBattLevel);
        }
    };

    private void saveSamples(boolean charging, long diffTime) {
        //get the current id sample of discharging
        int sampleIndex = store.getInt(charging ? chargingSampleId : disChargingSampleId, -1);

        //starts with id 1 in database
        if (sampleIndex == -1)
            sampleIndex = 1;
        //only maximum of 10 samples required.
        if (sampleIndex > 10)
            sampleIndex = 1;

        if (!charging) {
            dischargingSampleViewModel
                    .insert(new DischargingSampleModel(
                            sampleIndex, diffTime
                    ));
        } else {
            chargingSampleViewModel
                    .insert(new ChargingSampleModel(
                            sampleIndex, diffTime
                    ));
        }
        logStatic("New sample found for " +
                (charging ? "charging" : "discharging") + " at level " +
                store.getInt(bat_level) + " id " + sampleIndex);

        //increment the sample id
        store.setInt(charging ? chargingSampleId : disChargingSampleId, 1 + 1);
    }

    private final Runnable checkBatteryStatusRunnable = new Runnable() {
        @Override
        public void run() {
            //DO WHATEVER YOU WANT WITH LATEST BATTERY LEVEL STORED IN batteryLevel HERE...
            // schedule next battery check
            handler.postDelayed(checkBatteryStatusRunnable, store != null ? 10000
                    : DEFAULT_CHECK_BATTERY_INTERVAL);
            logStatic("Battery status is " + currentBattLevel + "mm cached. Interval: " + 10000);

            donePlayingLoadBeepBelowTenPercent = false;
            int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
            //If no stored battery value, then save the current battery level
            if (storedPreviousBatLevel == -1) {
                store.setInt(previousBatValueKey, (int) currentBattLevel);
                storedPreviousBatLevel = (int) currentBattLevel;
            }
            //Only play the TTS if the current battery level is not the same as previous battery level
            if (storedPreviousBatLevel != -1) {
                //Only alert if time is not in between sleep mode
                Calendar nowCal = Calendar.getInstance();
                boolean isCharging;
                isCharging = BaseActivity.isCharging(getApplicationContext());

                String checkIntervalOnBatteryServiceLevelCheckerKeyStore = isCharging ?
                        checkIntervalOnBatteryServiceLevelCheckerForCharging :
                        checkIntervalOnBatteryServiceLevelCheckerForDisCharging;

                //We don't wait for the interval to trigger. So it will check every time for level changes.
                if (!isTimeInBetweenSleepMode(
                        store.getLong(startTimeLong), nowCal.getTimeInMillis(),
                        store.getLong(stopTimeLong))) {
                    checkLoudBeepRules(storedPreviousBatLevel);
                }

                if (!isTimeInBetweenSleepMode(
                        store.getLong(startTimeLong), nowCal.getTimeInMillis(),
                        store.getLong(stopTimeLong))
                        && isTimeIntervalDone(store, checkIntervalOnBatteryServiceLevelCheckerKeyStore)) {
                    checkRulesOnTheList();
                }
            } else
                logStatic("Previous battery level " + storedPreviousBatLevel + " is the same as current level " + currentBattLevel);

        }
    };

    //Check after playing TTS if playing loud beep below 10 percent match criteria
    private void checkLoudBeepRules(int storedPreviousBatLevel) {
        if (!donePlayingLoadBeepBelowTenPercent &&
                !BaseActivity.isCharging(getApplicationContext()) &&
                store.getBoolean(playLoudBeepOnBelowTenPercent) &&
                (int) currentBattLevel < 10
                && (int) currentBattLevel != storedPreviousBatLevel
        ) {
            donePlayingLoadBeepBelowTenPercent = true;
            store.setInt(previousBatValueKey, (int) currentBattLevel);
            playLoadBeep();
        }
    }

    @Override
    public void onCreate() {
        logStatic("BatteryService is now created");
        myBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        batteryUtil = new BatteryUtil();
        store = new PrefStore(this);
        //batteryDischargingTimes = new ArrayList<>();
        //batteryChargingTimes = new ArrayList<>();
        registerReceiver(batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        initializeTTS();

        handler.postDelayed(checkBatteryStatusRunnable, CHECK_BATTERY_INTERVAL);

        store.setBoolean(isSwitchOff, false);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
        maxMusicVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        maxRingVolume = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

        chargeWithPercentageModels = new ArrayList<>();
        chargeViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ChargeViewModel.class);
        usageViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(UsageViewModel.class);
        chargingSampleViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ChargingSampleViewModel.class);
        dischargingSampleViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(DischargingSampleViewModel.class);
        Observer<List<ChargeWithPercentageModel>> obsEntries = new Observer<List<ChargeWithPercentageModel>>() {
            @Override
            public void onChanged(@Nullable List<ChargeWithPercentageModel> entries) {
                chargeWithPercentageModels.clear();
                for (ChargeWithPercentageModel cwpm : entries) {
                    //let's only include percentage that is active in the checking
                    List<PercentageModel> npm = new ArrayList<>();
                    for (PercentageModel pm : cwpm.percentageModels) {
                        if (pm.selected)
                            npm.add(pm);
                    }
                    cwpm.percentageModels.clear();
                    cwpm.percentageModels.addAll(npm);
                    chargeWithPercentageModels.add(cwpm);
                }
                if (entries.size() > 0) {
                    StringBuilder sb1 = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    for (PercentageModel p1 : entries.get(0).percentageModels) {
                        if (p1.selected)
                            sb1.append(p1.percentage).append(",");
                    }
                    for (PercentageModel p2 : entries.get(1).percentageModels) {
                        if (p2.selected)
                            sb2.append(p2.percentage).append(",");
                    }
                }
            }
        };
        chargeViewModel.getAllChargeWithPercentageSets().observeForever(obsEntries);

        //Load the samples from database for charging/discharging
        chargeSampleList = chargingSampleViewModel.getAll();
        chargeSampleList.observeForever(chargeSamplesObserver);
        dischargeSampleList = dischargingSampleViewModel.getAll();
        dischargeSampleList.observeForever(dischargeSampleListObserver);

        startForegroundChecker();
    }

    @Override
    public void onDestroy() {
        logStatic("BatteryService is now destroyed");
        unregisterReceiver(batInfoReceiver);
        handler.removeCallbacks(checkBatteryStatusRunnable);

        stopForegroundChecker();
        if (null != appUsageObserver)
            usageModel.removeObserver(appUsageObserver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BatteryService getServerInstance() {
            return BatteryService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showForegroundNotification();
        return START_STICKY;
    }

    private void showForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mainDashboardIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = createNotificationChannel(notificationManager);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);

            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_tracki_launcher_icon)
                    .setTicker("Hearty365")
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentTitle("Tracki Batt Notification")
                    .setContentText("Tracki Batt is running in background to stay connected.")
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(mainDashboardIntent)
                    .build();
            startForeground(ID_SERVICE, notification);
        } else {
            Notification notification = new NotificationCompat.Builder(this, "Channel01")
                    .setContentTitle("Tracki Batt")
                    .setContentText("Tracki Batt is working in background")
                    .setSmallIcon(R.drawable.ic_tracki_launcher_icon)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(mainDashboardIntent)
                    .build();
            startForeground(ID_SERVICE, notification);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "my_service_channelid";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    public void initializeTTS() {
        logStatic("initializeTTS");

        initTTSSuccessfull = false;
        tts = new TextToSpeech(this, i -> {
            if (i == TextToSpeech.SUCCESS) {
                initTTSSuccessfull = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    defaultTTSVoice = tts.getDefaultVoice();
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
            }

            @Override
            public void onStart(String s) {
                int checkCurrentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                int checkCurrentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
                logStatic("tts while playing currentMusicVolume:" + checkCurrentMusicVolume + " currentRingtoneVolume:" + checkCurrentRingtoneVolume);
            }

            @Override
            public void onDone(String s) {
                //Set volume previous volume level set by user
                logStatic("onDone tts currentMusicVolume:" + currentMusicVolume + " currentRingtoneVolume:" + currentRingtoneVolume);
                if (checkModifyMaxVolumePermissionNoPrompt(getApplicationContext())) {
                    if (store.getBoolean(playSoundWithMaxVolume)) {
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, 0);
                        audio.setStreamVolume(AudioManager.STREAM_RING, currentRingtoneVolume, 0);
                    }
                }

                //checkLoudBeepRules();
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    private void checkRulesOnTheList() {
        BaseActivity.readPowerConsumption(getApplicationContext());

        if (store.getBoolean(isSwitchOff, false)) {
            logStatic("Switch is off");
            return;
        }

        //Check if user enable max volume for Alert
        if (store.getBoolean(ignoreSystemAudioProfile, false)) {
            if (checkModifyMaxVolumePermissionNoPrompt(getApplicationContext())) {
                if (store.getBoolean(playSoundWithMaxVolume)) {
                    currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
                    logStatic("check currentMusicVolume:" + currentMusicVolume + " currentRingtoneVolume:" + currentRingtoneVolume);

                    //set to max volume
                    audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    audio.setStreamVolume(AudioManager.STREAM_RING, maxRingVolume, 0);
                } else {
                    currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
                    audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    audio.setStreamVolume(AudioManager.STREAM_RING, currentRingtoneVolume, 0);
                }
            }
        }

        //Check if user check disable during call settings
        if (store.getBoolean(disableAlertDuringCall, true)
                && isCallActive(getApplicationContext())) {
            logStatic("Alert is off because you are in active call");
            return;
        }

        boolean ttsWasPlayed = false;
        boolean isCharging = false;

        //isCharging = store.getBoolean(BaseActivity.isCharging, false);
        isCharging = BaseActivity.isCharging(getApplicationContext());
        String isEnableRepeatitionKeyStore = isCharging ?
                enableRepeatedAlertForPercentageForCharging :
                enableRepeatedAlertForPercentageForDisCharging;

        logStatic("isCharging: " + isCharging);

        if (chargeWithPercentageModels.size() == 0)
            return;

        int arrayIndexToGet = 0;
        if (!isCharging) {
            arrayIndexToGet = 1;
        }

        ChargeWithPercentageModel cp = chargeWithPercentageModels.get(arrayIndexToGet);
        for (int i = 0; i < cp.percentageModels.size(); i++) {
            logStatic(new Gson().toJson(cp.percentageModels.get(i)));
            PercentageModel prevPercentage = null, nextPercentage = null;
            //get previous percentage
            if (i > 0) {
                prevPercentage = cp.percentageModels.get(i - 1);
            }
            //get next to the current percentage
            if ((i + 1) <= (cp.percentageModels.size() - 1)) {
                nextPercentage = cp.percentageModels.get(i + 1);
            }
            PercentageModel p = cp.percentageModels.get(i);
            if (ttsWasPlayed)
                return;
            if (isCharging
                    && p.selected &&
                    (p.percentage <= currentBattLevel//left filter
                            && ((nextPercentage == null) || nextPercentage.percentage > currentBattLevel))//right filter
                    && store.getBoolean(isEnableRepeatitionKeyStore, true)
            ) {
                logStatic("Play tts in new battery level");
                store.setInt(previousBatValueKey, (int) currentBattLevel);
                ttsWasPlayed = true;
                playTTS(cp.chargeModel.eventString, p.percentage);
            } else if (!isCharging
                    && p.selected &&
                    (p.percentage >= currentBattLevel//left filter
                            && ((prevPercentage == null) || prevPercentage.percentage < currentBattLevel))//right filter
                    && store.getBoolean(isEnableRepeatitionKeyStore, true)
            ) {
                logStatic("Play tts in new battery level");
                store.setInt(previousBatValueKey, (int) currentBattLevel);
                ttsWasPlayed = true;
                playTTS(cp.chargeModel.eventString, p.percentage);
            }
        }
    }

    private void playTTS(String tell, int percentage) {
        logStatic("playTTS: " + tell);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
        String includeSpeedString = "";
        boolean isCharging = BaseActivity.isCharging(getApplicationContext());
        if (null != myBatteryManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int current;
            current = myBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            String speedString = batteryUtil.getChargingAndDischargingSpeed(current);
            if ((isCharging && store.getBoolean(announceOnVerySlowCharging))
                    || (!isCharging && store.getBoolean(announceOnVerySlowDisharging))) {
                if (speedString.equalsIgnoreCase("Very slow")) {
                    includeSpeedString = "Very slow ";
                }
            }
            if ((isCharging && store.getBoolean(announceOnSlowCharging))
                    || (!isCharging && store.getBoolean(announceOnSlowDisharging))) {
                if (speedString.equalsIgnoreCase("Slow")) {
                    includeSpeedString = "Slow ";
                }
            }
            if ((isCharging && store.getBoolean(announceOnMediumCharging))
                    || (!isCharging && store.getBoolean(announceOnMediumDisharging))) {
                if (speedString.equalsIgnoreCase("Medium")) {
                    includeSpeedString = "Medium ";
                }
            }
            if ((isCharging && store.getBoolean(announceOnFastCharging))
                    || (!isCharging && store.getBoolean(announceOnFastDisharging))) {
                if (speedString.equalsIgnoreCase("Fast")) {
                    includeSpeedString = " Fast ";
                }
            }
            if ((isCharging && store.getBoolean(announceOnVertFastCharging))
                    || (!isCharging && store.getBoolean(announceOnVertFastDisharging))) {
                if (speedString.equalsIgnoreCase("Very fast")) {
                    includeSpeedString = "Very fast ";
                }
            }
            if ((isCharging && store.getBoolean(announceOnSuperFastCharging))
                    || (!isCharging && store.getBoolean(announceOnSuperFastDisharging))) {
                if (speedString.equalsIgnoreCase("Super fast")) {
                    includeSpeedString = "Super fast ";
                }
            }
        }

        //Logic to use settings exact percent or percent base on range
        if (isCharging && store.getBoolean(chargingAnnouncePercentExactValue))
            percentage = (int) currentBattLevel;
        if (!isCharging && store.getBoolean(dischargingAnnouncePercentExactValue))
            percentage = (int) currentBattLevel;

        String includePercentageAtTheEndStr = "";
        if (isCharging && store.getBoolean(includePercentAtTheEndOfAnnouncementCharging))
            includePercentageAtTheEndStr = " Percent";
        else if (!isCharging && store.getBoolean(includePercentAtTheEndOfAnnouncementDisCharging))
            includePercentageAtTheEndStr = " Percent";

        if (TextUtils.isEmpty(tell) || tell.equalsIgnoreCase("null"))
            tts.speak("" + includeSpeedString + " " + percentage, TextToSpeech.QUEUE_ADD, map);
        else {
            tts.speak(tell + " " + includeSpeedString + " " +
                    percentage + includePercentageAtTheEndStr, TextToSpeech.QUEUE_ADD, map);
        }
    }

    private void playLoadBeep() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        boolean notifPermissionGranted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            notifPermissionGranted = false;
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (notifPermissionGranted) {
            AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            int currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
            int maxMusicVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int maxRingVolume = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            audio.setStreamVolume(AudioManager.STREAM_RING, maxRingVolume, 0);

            final MediaPlayer mp = MediaPlayer.create(this, R.raw.low_battery_sound);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    audio.setStreamVolume(AudioManager.STREAM_RING, currentRingtoneVolume, 0);
                }
            });
            mp.start();
        }
    }

    public boolean isCallActive(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getMode() == AudioManager.MODE_IN_CALL;
    }

    /**
     * Method called when the charging time is calculated.
     * The {@param hours} and {@param mins} remaining for full charge.
     */
    private void onChargingTimePublish(int hours, int mins) {
        store.saveString(remainingTimeForBatteryToDrainOrCharge,
                String.format(Locale.US, "Charging time: %sh %sm", hours, mins));
        store.setInt(remainingTimeForBatteryToDrainOrChargeHr, hours);
        store.setInt(remainingTimeForBatteryToDrainOrChargeMn, mins);
    }

    /**
     * Method called when the charging time is being calculated.
     * Any status text indicating that "charging time is being calculated" can be used here.
     */
    private void onCalculatingChargingTime() {
        store.saveString(remainingTimeForBatteryToDrainOrCharge, "Computing charging time...");
    }

    /**
     * Method called when the charging time is calculated.
     * The {@param days} , {@param hours} and {@param mins} remaining for the battery to drain.
     */
    private void onDischargeTimePublish(int days, int hours, int mins) {
        if (days == 0) {
            store.saveString(remainingTimeForBatteryToDrainOrCharge,
                    String.format(Locale.US, "Discharging time: %sh %sm", hours, mins));
        } else {
            store.saveString(remainingTimeForBatteryToDrainOrCharge,
                    String.format(Locale.US, "Discharging time: %sd %sh %sm", days, hours, mins));
        }
        store.setInt(remainingTimeForBatteryToDrainOrChargeDy, days);
        store.setInt(remainingTimeForBatteryToDrainOrChargeHr, hours);
        store.setInt(remainingTimeForBatteryToDrainOrChargeMn, mins);
    }

    /**
     * Method called when the discharging time is being calculated.
     * Any status text indicating that "discharging time is being calculated" can be used here.
     */
    private void onCalculatingDischargingTime() {
        store.saveString(remainingTimeForBatteryToDrainOrCharge, "Computing drain time...");
    }

    /**
     * Method called when the battery is fully charged.
     * Called only when the device is connected to charger and battery becomes full.
     */
    private void onFullBattery() {

    }

    /**
     * Start the calculation when the activity is created.
     * Method to calculate the charging time based on the timely usage.
     */
    private void publishChargingText() {
        chargeSampleList = chargingSampleViewModel.getAll();
        chargeSampleList.observeForever(chargeSamplesObserver);
    }

    Observer<List<ChargingSampleModel>> chargeSamplesObserver = new Observer<List<ChargingSampleModel>>() {
        @Override
        public void onChanged(List<ChargingSampleModel> chargingSampleModels) {
            chargeSampleList.removeObserver(chargeSamplesObserver);
            long average, sum = 0;
            for (ChargingSampleModel time : chargingSampleModels) {
                sum += time.diffTime;
            }

            //Avoid error: java.lang.ArithmeticException: divide by zero
            if (chargingSampleModels.size() == 0)
                return;

            average = (sum / (chargingSampleModels.size())) * (100 - store.getInt(bat_level));

            //Since charging cannot take days
            //int days = (int) TimeUnit.MILLISECONDS.toDays(average);
            int hours = (int) (TimeUnit.MILLISECONDS.toHours(average) % TimeUnit.DAYS.toHours(1));
            int mins = (int) (TimeUnit.MILLISECONDS.toMinutes(average) % TimeUnit.HOURS.toMinutes(1));

            if (BaseActivity.isCharging(getApplicationContext()))
                onChargingTimePublish(hours, mins);
        }
    };

    /**
     * Method to calculate the discharging time based on the timely usage.
     */
    private void publishDischargingText() {
        dischargeSampleList = dischargingSampleViewModel.getAll();
        dischargeSampleList.observeForever(dischargeSampleListObserver);
    }

    Observer<List<DischargingSampleModel>> dischargeSampleListObserver = new Observer<List<DischargingSampleModel>>() {
        @Override
        public void onChanged(List<DischargingSampleModel> dischargingSampleModels) {
            dischargeSampleList.removeObserver(dischargeSampleListObserver);
            long average, sum = 0;
            for (DischargingSampleModel time : dischargingSampleModels) {
                sum += time.diffTime;
            }

            //Avoid error: java.lang.ArithmeticException: divide by zero
            if (dischargingSampleModels.size() == 0)
                return;

            average = (sum / (dischargingSampleModels.size())) * store.getInt(bat_level);

            int days = (int) TimeUnit.MILLISECONDS.toDays(average);
            int hours = (int) (TimeUnit.MILLISECONDS.toHours(average) % TimeUnit.DAYS.toHours(1));
            int mins = (int) (TimeUnit.MILLISECONDS.toMinutes(average) % TimeUnit.HOURS.toMinutes(1));

            onDischargeTimePublish(days, hours, mins);
        }
    };

    private void startForegroundChecker() {
        appChecker = new AppChecker();
        appChecker
                .when(getPackageName(), packageName -> {
                    packageDetected = packageName;
                    observer(packageName);

                    //if (!lastPackageDetected.equals(packageName))
                    //Toast.makeText(getBaseContext(), "Our app is in the foreground.", Toast.LENGTH_SHORT).show();
                })
                .whenOther(packageName -> {
                    observer(packageName);

                    //Toast.makeText(getBaseContext(), "Foreground: " + packageName, Toast.LENGTH_SHORT).show();
                    //}

                })
                .timeout(5000)
                .start(this);
    }

    private void stopForegroundChecker() {
        appChecker.stop();
    }

    private void observer(String packageName) {
        packageDetected = packageName;
        //Get only current in mAh only once per app but exception below
        if (!lastPackageDetected.equals(packageName)) {
            currentBeforeAppOnForeground = store.getInt(bat_current);
            appCurrentSampling.clear();
            appCurrentSampling.add(currentBeforeAppOnForeground);
        }

        //if bat_current is less than the currentBeforeAppOnForeground, then add to app current sampling
        if (lastPackageDetected.equalsIgnoreCase(packageName) && currentBeforeAppOnForeground < store.getInt(bat_current)) {
            currentBeforeAppOnForeground = store.getInt(bat_current);
            appCurrentSampling.add(currentBeforeAppOnForeground);
        }

        lastPackageDetected = packageName;

        handler.postDelayed(() -> {
            usageModel = usageViewModel.findUsage(packageName);
            usageModel.observeForever(appUsageObserver);
        }, 4000);

    }

    Observer<UsageModel> appUsageObserver = new Observer<UsageModel>() {
        @Override
        public void onChanged(@Nullable UsageModel um) {
            if (um != null) {
                logStatic(lastPackageDetected + " is now saved");
            } else {
                logStatic(packageDetected + " is not saved yet");
                um = new UsageModel();
            }
            usageModel.removeObserver(appUsageObserver);

            um.packageName = packageDetected;
            //get average app current and make it as final app mAh
            int appAvgCurr = 0;
            for (int curr : appCurrentSampling) {
                appAvgCurr = appAvgCurr + curr;
            }
            if (appAvgCurr > 0) {
                um.current_beforeLaunch = (float) appAvgCurr / appCurrentSampling.size();//currentBeforeAppOnForeground;
            } else {
                um.current_beforeLaunch = currentBeforeAppOnForeground;
            }
            um.current_mAh = store.getInt(bat_current);
            um.avg_mAh = store.getInt(bat_current_avg);
            um.capacity_mAh = store.getInt(bat_capacity);
            um.current_battery_percent = store.getInt(bat_level);
            if (um.current_beforeLaunch > um.current_mAh) {
                /*
                means currentB4 launch is positive and current_mAh is negative
                before launch = 100, after launch  = -200
                 */
                if (um.current_beforeLaunch > 0 && um.current_mAh < 0)
                    um.mAh = Math.abs((int) um.current_beforeLaunch - (int) um.current_mAh);
                else if (um.current_beforeLaunch > 0 && um.current_mAh > 0)
                    um.mAh = (int) Math.abs(um.current_beforeLaunch) - (int) um.current_mAh;
                else if (um.current_beforeLaunch < 0 && um.current_mAh < 0)
                    um.mAh = (int) Math.abs(um.current_mAh) - (int) Math.abs(um.current_beforeLaunch);

                usageViewModel.insert(um);
            }
        }
    };

}