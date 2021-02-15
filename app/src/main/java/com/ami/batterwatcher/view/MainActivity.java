package com.ami.batterwatcher.view;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.adapters.AlertListAdapter;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.AlertViewModel;
import com.ami.batterwatcher.data.ChargeViewModel;
import com.ami.batterwatcher.databinding.ActivityMainBinding;
import com.ami.batterwatcher.service.BatteryService;
import com.ami.batterwatcher.util.BatteryUtil;
import com.ami.batterwatcher.viewmodels.AlertModel;
import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding viewDataBinding;
    private AlertViewModel viewModel;
    private ChargeViewModel chargeViewModel;
    private RecyclerView recyclerView_list;
    private AlertListAdapter listAdapter;
    private List<AlertModel> models;
    private List<ChargeModel> chargeModels;
    private List<ChargeWithPercentageModel> chargeWithPercentageModels;
    private BroadcastReceiver mBatInfoReceiver;
    private boolean initTTSSuccessfull = false;
    private TextToSpeech tts;
    private Voice defaultTTSVoice;
    private int currentBattLevel;
    private BatteryUtil batteryUtil;
    private AudioManager audio;
    int currentMusicVolume;
    int currentRingtoneVolume;
    FirebaseCrashlytics mCrashlytics;
    int t = 0;
    Handler handler = new Handler();
    //private WaveHelper mWaveHelper;
    private BatteryService batteryService;
    private boolean mBounded;

    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected View setLayoutBinding() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewDataBinding = DataBindingUtil.inflate(inflater, setLayout(), null, false);
        viewDataBinding.executePendingBindings();
        return viewDataBinding.getRoot();
    }

    @Override
    protected void setData() {
        // Get a new or existing ViewModel from the ViewModelProvider.
        store.setInt(bat_capacity, (int) BaseActivity.getBatteryCapacity(getApplicationContext()));
        viewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        chargeViewModel = new ViewModelProvider(this).get(ChargeViewModel.class);
        models = new ArrayList<>();
        chargeModels = new ArrayList<>();
        chargeWithPercentageModels = new ArrayList<>();
        listAdapter = new AlertListAdapter(this, models, new AlertListAdapter.ClickListener() {
            @Override
            public void onClick(int position) {
                Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
                addIntent.putExtra("screen_type", 2);
                addIntent.putExtra("data", models.get(position));
                startActivity(addIntent);
            }

            @Override
            public void onLongPress(int position) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Alert")
                        .setMessage("Do you really want to Delete?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                viewModel.delete(models.get(position));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }

    @Override
    protected void setViews() {
        recyclerView_list = findViewById(R.id.recyclerView_list);
        recyclerView_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView_list.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_list.setItemAnimator(new DefaultItemAnimator());
        showBackButton(false);

        batteryUtil = new BatteryUtil();

        recyclerView_list.setAdapter(listAdapter);
        audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);

        viewDataBinding.switchService.setChecked(!store.getBoolean(isSwitchOff, true));

        //mWaveHelper = new WaveHelper(mContext, viewDataBinding.wave, (float) 0.0f);
        /*mCrashlytics = FirebaseCrashlytics.getInstance();

        // Log the onCreate event, this will also be printed in logcat
        mCrashlytics.log("onCreate");

        // Add some custom values and identifiers to be included in crash reports
        mCrashlytics.setCustomKey("MeaningOfLife", 42);
        mCrashlytics.setCustomKey("LastUIAction", "Test value");
        mCrashlytics.setUserId("123456789");

        // Report a non-fatal exception, for demonstration purposes
        mCrashlytics.recordException(new Exception("Non-fatal exception: something went wrong!"));

        try {
            throw new NullPointerException();
        } catch (NullPointerException ex) {
            // [START crashlytics_log_and_report]
            mCrashlytics.log("NPE caught!");
            mCrashlytics.recordException(ex);
            // [END crashlytics_log_and_report]
        }*/

    }

    @Override
    protected void setListeners() {
        viewDataBinding.imageViewBatteryUsage.setOnClickListener(view -> {
            Intent batteryUsageIntent = new Intent(MainActivity.this, UsageActivity.class);
            startActivity(batteryUsageIntent);
        });

        viewDataBinding.imageViewBatteryInfo.setOnClickListener(view -> {
            Intent batteryInfoIntent = new Intent(MainActivity.this, BatteryInfoActivity.class);
            startActivity(batteryInfoIntent);
        });

        viewDataBinding.includeSetting.getRoot().setOnClickListener(view -> {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        });

        viewDataBinding.includeCharging.getRoot().setOnClickListener(view -> {
            Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
            addIntent.putExtra("screen_type", 3);
            if (chargeModels.size() > 0)
                addIntent.putExtra("data", chargeModels.get(0));
            startActivity(addIntent);
        });

        viewDataBinding.includeDischarging.getRoot().setOnClickListener(view -> {
            Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
            addIntent.putExtra("screen_type", 4);
            if (chargeModels.size() > 0)
                addIntent.putExtra("data", chargeModels.get(1));
            startActivity(addIntent);
        });

        viewDataBinding.switchService.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                store.setBoolean(isSwitchOff, false);
            } else {
                store.setBoolean(isSwitchOff, true);
            }
        });

        viewDataBinding.includePromoteTrackiApp.getRoot().setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "https://play.google.com/store/apps/details?id=com.trackimo.android.tracki"));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        });

        viewDataBinding.includeBuyOnAmazon.getRoot().setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://amzn.to/2Mijobo"));
            startActivity(browserIntent);
        });

        viewDataBinding.includeBuyOnAmazon.textViewText1.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://amzn.to/2Mijobo"));
            startActivity(browserIntent);
        });

        viewDataBinding.includeAudiosetting.getRoot().setOnClickListener(view -> {
            //Text-to-speech output
            startActivity(new Intent().setAction("com.android.settings.TTS_SETTINGS")
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });

        viewDataBinding.includeAudiosetting.imageViewRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != batteryService) {
                    batteryService.initializeTTS();
                    showDialogOkButton("Reinitialized TTS successfully!");
                }
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BaseActivity.isCharging(mContext)) {
                    viewDataBinding.textViewChargingStatus.setText("isCharging = true");
                    store.setBoolean(isCharging, true);
                } else {
                    viewDataBinding.textViewChargingStatus.setText("isCharging = false");
                    store.setBoolean(isCharging, false);
                }
                setBatteryInfo();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void startBatteryService() {
        startService(new Intent(this, BatteryService.class));
        mBatInfoReceiver = new BroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter("YourAction");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBatInfoReceiver, intentFilter);
    }

    private void stopBatteryService() {
        if (mBatInfoReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBatInfoReceiver);
        }
        mBatInfoReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_setting) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (itemId == R.id.menu_info) {
            return true;
        } else if (itemId == R.id.menu_more) {
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBatteryService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, BatteryService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
        stopBatteryService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBatteryService();
        //mWaveHelper.start();
        if (batteryService != null) {
            batteryService.initializeTTS();
        }
        viewModel.getAll().observe(this, alertModels -> {
            models.clear();
            models.addAll(alertModels);
            listAdapter.notifyDataSetChanged();
        });
        chargeViewModel.getAll().observe(this, list -> {
            chargeModels.clear();
            chargeModels.addAll(list);
        });
        chargeViewModel.getAllChargeWithPercentageSets().observe(this, list2 -> {
            chargeWithPercentageModels.clear();
            chargeWithPercentageModels.addAll(list2);
            if (list2.size() > 0) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for (PercentageModel p1 : list2.get(0).percentageModels) {
                    if (p1.selected)
                        sb1.append(p1.percentage).append(",");
                }
                for (PercentageModel p2 : list2.get(1).percentageModels) {
                    if (p2.selected)
                        sb2.append(p2.percentage).append(",");
                }
                viewDataBinding.includeCharging.textViewChargingPercentages.setText(sb1.toString());
                viewDataBinding.includeDischarging.textViewDisChargingPercentages.setText(sb2.toString());

                Calendar nowCal = Calendar.getInstance();
                if (!isTimeInBetweenSleepMode(
                        store.getLong(startTimeLong), nowCal.getTimeInMillis(),
                        store.getLong(stopTimeLong))) {
                    viewDataBinding.includeSetting.textViewText1.setText("Sleep mode - Alerts are on now");
                    viewDataBinding.includeSetting.textViewText1.setTextColor(
                            mContext.getResources().getColor(R.color.blue)
                    );
                } else {
                    viewDataBinding.includeSetting.textViewText1.setText("Sleep mode - Alerts are off now");
                    viewDataBinding.includeSetting.textViewText1.setTextColor(
                            mContext.getResources().getColor(R.color.red)
                    );
                }
            }
        });

        viewDataBinding.includeSetting.textViewStartTime.setText(
                String.format(Locale.US, "%s",
                        convertTimePickerTime(
                                store.getInt(startTimeHr), store.getInt(startTimeMn)) + "-"));
        viewDataBinding.includeSetting.textViewStopTime.setText(
                convertTimePickerTime(store.getInt(stopTimeHr), store.getInt(stopTimeMn)));

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses("com.skype.raider");

        Method forceStopPackage = null;
        try {
            forceStopPackage = activityManager.getClass().getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(activityManager, "com.skype.raider");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mWaveHelper.cancel();
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("YourAction")) {
                currentBattLevel = intent.getIntExtra("batteryLevel", -1);
                setBatteryInfo();
            }
        }
    }

    private void setBatteryInfo() {
        viewDataBinding.textViewChargingSpeed.setText(String.format(Locale.US,
                "%s speed: %s (%s mA)",
                BaseActivity.isCharging(mContext) ? "Charging" : "Discharging",
                batteryUtil.getChargingAndDischargingSpeed(store.getInt(bat_current)),
                store.getInt(bat_current)));

        //Get stored battery value and compare it on the current value
        int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
        log("rawLevel:" + currentBattLevel +
                " scale:" + store.getInt(bat_scale) + " status:" + store.getInt(bat_status) +
                " storedPreviousBatLevel:" + storedPreviousBatLevel);
        //If no stored battery value, then save the current battery level
        if (storedPreviousBatLevel == -1) {
            store.setInt(previousBatValueKey, currentBattLevel);
        }
        viewDataBinding.textViewChargingTimeRemaining.setText(
                store.getString(remainingTimeForBatteryToDrainOrCharge, "Computing time..."));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TTS_DATA_CHECK_CODE) {
            // Success! File has already been installed
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //mTts = new TextToSpeech(mContext, null);
                showDialogOkButton("Successfully configured TTS");
            } else {
                // fail, attempt to install tts
                Intent installTts = new Intent();
                installTts.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTts);
            }
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //showDialogOkButton("Service is disconnected");
            mBounded = false;
            batteryService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //showDialogOkButton("Service is connected");
            mBounded = true;
            BatteryService.LocalBinder mLocalBinder = (BatteryService.LocalBinder) service;
            batteryService = mLocalBinder.getServerInstance();
        }
    };

}