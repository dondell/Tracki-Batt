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
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.ChargeViewModel;
import com.ami.batterwatcher.util.PrefStore;
import com.ami.batterwatcher.view.MainActivity;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.ami.batterwatcher.base.BaseActivity.checkIntervalOnBatteryServiceLevelCheckerForCharging;
import static com.ami.batterwatcher.base.BaseActivity.checkIntervalOnBatteryServiceLevelCheckerForDisCharging;
import static com.ami.batterwatcher.base.BaseActivity.checkModifyMaxVolumePermissionNoPrompt;
import static com.ami.batterwatcher.base.BaseActivity.disableAlertDuringCall;
import static com.ami.batterwatcher.base.BaseActivity.enableRepeatedAlertForPercentageForCharging;
import static com.ami.batterwatcher.base.BaseActivity.enableRepeatedAlertForPercentageForDisCharging;
import static com.ami.batterwatcher.base.BaseActivity.ignoreSystemAudioProfile;
import static com.ami.batterwatcher.base.BaseActivity.isSwitchOff;
import static com.ami.batterwatcher.base.BaseActivity.isTimeInBetweenSleepMode;
import static com.ami.batterwatcher.base.BaseActivity.isTimeIntervalDone;
import static com.ami.batterwatcher.base.BaseActivity.logStatic;
import static com.ami.batterwatcher.base.BaseActivity.playSoundWithMaxVolume;
import static com.ami.batterwatcher.base.BaseActivity.previousBatValueKey;
import static com.ami.batterwatcher.base.BaseActivity.startTimeLong;
import static com.ami.batterwatcher.base.BaseActivity.stopTimeLong;

public class BatteryService extends Service {

    public static final int DEFAULT_CHECK_BATTERY_INTERVAL = 10000;
    private static final int CHECK_BATTERY_INTERVAL = DEFAULT_CHECK_BATTERY_INTERVAL;

    private double currentBattLevel;
    private Handler handler;
    private PrefStore store;
    private static final int ID_SERVICE = 104;
    private TextToSpeech tts;
    private Voice defaultTTSVoice;
    private boolean initTTSSuccessfull = false;
    private AudioManager audio;
    private int currentMusicVolume;
    private int currentRingtoneVolume;
    private int maxMusicVolume, maxRingVolume;
    private List<ChargeWithPercentageModel> chargeWithPercentageModels;
    private ChargeViewModel chargeViewModel;

    private BroadcastReceiver batInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent batteryIntent) {
            int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (rawlevel >= 0 && scale > 0) {
                currentBattLevel = (rawlevel * 100.0) / scale;
            }
            //Log.e("xxx Battery status is", "xxx " + batteryLevel + "mm");
        }
    };

    private Runnable checkBatteryStatusRunnable = new Runnable() {
        @Override
        public void run() {
            //DO WHATEVER YOU WANT WITH LATEST BATTERY LEVEL STORED IN batteryLevel HERE...
            // schedule next battery check
            handler.postDelayed(checkBatteryStatusRunnable, store != null ? 10000
                    : DEFAULT_CHECK_BATTERY_INTERVAL);
            logStatic("Battery status is " + currentBattLevel + "mm cached. Interval: " + 10000);

            /*
            //This will only work if activity is running. So we fully transfer inside this service.
            Intent intent = new Intent("YourAction");
            Bundle bundle = new Bundle();
            bundle.putInt("batteryLevel", (int) batteryLevel);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);*/

            int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
            //If no stored battery value, then save the current battery level
            if (storedPreviousBatLevel == -1) {
                store.setInt(previousBatValueKey, (int) currentBattLevel);
            }
            //Only play the TTS if the current battery level is not the same as previous battery level
            if (storedPreviousBatLevel != -1) {
                //Only alert if time is not in between sleep mode
                Calendar nowCal = Calendar.getInstance();
                boolean isCharging = false;
                isCharging = BaseActivity.isCharging(getApplicationContext());

                String checkIntervalOnBatteryServiceLevelCheckerKeyStore = isCharging ?
                        checkIntervalOnBatteryServiceLevelCheckerForCharging :
                        checkIntervalOnBatteryServiceLevelCheckerForDisCharging;
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

    @Override
    public void onCreate() {
        logStatic("BatteryService is now created");
        store = new PrefStore(this);
        handler = new Handler();
        handler.postDelayed(checkBatteryStatusRunnable, CHECK_BATTERY_INTERVAL);

        store.setBoolean(isSwitchOff, false);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
        maxMusicVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        maxRingVolume = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

        chargeWithPercentageModels = new ArrayList<>();
        chargeViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ChargeViewModel.class);
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

        registerReceiver(batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        initializeTTS();
    }

    @Override
    public void onDestroy() {
        logStatic("BatteryService is now destroyed");
        unregisterReceiver(batInfoReceiver);
        handler.removeCallbacks(checkBatteryStatusRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    private void initializeTTS() {

        initTTSSuccessfull = false;
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    initTTSSuccessfull = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        defaultTTSVoice = tts.getDefaultVoice();
                    }

                    /*logStatic("TTS successfully initialized");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        for (Voice tmpVoice : tts.getVoices()) {
                            //logStatic("tts voice " + tmpVoice.getName());
                            if (tmpVoice.getName().contains("female")) {
                                //logStatic("found female voice " + tmpVoice.getName());
                                store.saveString(ttsFemale, tmpVoice.getName());
                            }
                            if (tmpVoice.getName().contains("male")) {
                                //logStatic("found male voice " + tmpVoice.getName());
                                store.saveString(ttsMale, tmpVoice.getName());
                            }
                        }

                        Set<String> a = new HashSet<>();
                        a.add("male");//here you can give male if you want to select male voice.
                        //Voice v=new Voice("en-us-x-sfg#female_2-local",new Locale("en","US"),400,200,true,a);
                        Voice v = new Voice(store.getInt(ttsVoiceType, 2) == 1 ?
                                "en-us-x-sfg#male_2-local" : "es-us-x-sfb#female_1-local",
                                new Locale("en", "US"),
                                400, 200, true, a);
                        int result = tts.setVoice(v);
                        tts.setSpeechRate(0.8f);

                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            logStatic("This Language is not supported");
                            if (null != defaultTTSVoice)
                                tts.setVoice(defaultTTSVoice);
                        }

                    }*/

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
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                audio.setStreamVolume(AudioManager.STREAM_RING, currentRingtoneVolume, 0);
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
        String checkIntervalOnBatteryServiceLevelCheckerKeyStore = isCharging ?
                checkIntervalOnBatteryServiceLevelCheckerForCharging :
                checkIntervalOnBatteryServiceLevelCheckerForDisCharging;

        logStatic("isCharging: " + isCharging);

        if (chargeWithPercentageModels.size() == 0)
            return;

        int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
        int arrayIndexToGet = 0;
        if (!isCharging) {
            arrayIndexToGet = 1;
        }

        ChargeWithPercentageModel cp = chargeWithPercentageModels.get(arrayIndexToGet);
        int prevPercent = 0, nextPercent = 0;
        for (int i = 0; i < cp.percentageModels.size(); i++) {
            logStatic(new Gson().toJson(cp.percentageModels.get(i)));
            PercentageModel prevPercentage = null, nextPercentage = null;
            //get previous percentage
            if (i > 0) {
                prevPercentage = cp.percentageModels.get(i - 1);
                prevPercent = prevPercentage.percentage;
            }
            //get next to the current percentage
            if ((i + 1) <= (cp.percentageModels.size() - 1)) {
                nextPercentage = cp.percentageModels.get(i + 1);
                nextPercent = cp.percentageModels.get(i + 1).percentage;
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
        tts.setLanguage(Locale.US);
        if (TextUtils.isEmpty(tell) || tell.equalsIgnoreCase("null"))
            tts.speak("" + percentage, TextToSpeech.QUEUE_ADD, map);
        else {
            tts.speak(tell + " " + percentage, TextToSpeech.QUEUE_ADD, map);
        }
    }

    public boolean isCallActive(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getMode() == AudioManager.MODE_IN_CALL;
    }

}