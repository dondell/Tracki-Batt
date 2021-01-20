package com.ami.batterwatcher.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

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
import com.ami.batterwatcher.viewmodels.AlertModel;
import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.ChargeWithPercentageModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;

import java.util.ArrayList;
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
    private boolean initSuccessfull = false;
    private TextToSpeech tts;
    private int currentBattLevel;
    private BatteryManager myBatteryManager;
    private AudioManager audio;
    int currentMusicVolume;
    int currentRingtoneVolume;

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

        recyclerView_list.setAdapter(listAdapter);
        audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentRingtoneVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
    }

    @Override
    protected void setListeners() {
        startBatteryService();
        myBatteryManager = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);

        initSuccessfull = false;
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    initSuccessfull = true;
                    log("TTS successfully initialized");
                }
            }
        });

        viewDataBinding.includeSetting.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        viewDataBinding.includeCharging.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
                addIntent.putExtra("screen_type", 3);
                if (chargeModels.size() > 0)
                    addIntent.putExtra("data", chargeModels.get(0));
                startActivity(addIntent);
            }
        });

        viewDataBinding.includeDischarging.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
                addIntent.putExtra("screen_type", 4);
                if (chargeModels.size() > 0)
                    addIntent.putExtra("data", chargeModels.get(1));
                startActivity(addIntent);
            }
        });

        viewDataBinding.switchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startBatteryService();
                } else {
                    stopBatteryService();
                }
            }
        });
    }

    private void startBatteryService() {
        store.setBoolean(isSwitchOff, true);
        //registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        startService(new Intent(this, BatteryService.class));
        mBatInfoReceiver = new BroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter("YourAction");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBatInfoReceiver, intentFilter);
    }

    private void stopBatteryService() {
        store.setBoolean(isSwitchOff, false);
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
       /* } else if (itemId == R.id.menu_add) {
            Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
            addIntent.putExtra("screen_type", 1);
            startActivity(addIntent);
            return true;*/
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
    protected void onResume() {
        super.onResume();
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
            }
        });

        viewDataBinding.includeSetting.textViewStartTime.setText(
                String.format(Locale.US, "%s",
                        convertTimePickerTime(
                                store.getInt(startTimeHr), store.getInt(startTimeMn)) + "-"));
        viewDataBinding.includeSetting.textViewStopTime.setText(
                convertTimePickerTime(store.getInt(stopTimeHr), store.getInt(stopTimeMn)));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            currentBattLevel = intent.getIntExtra("batteryLevel", -1);
            int scale = intent.getIntExtra("scale", -1);
            int status = intent.getIntExtra("status", -1);
            //Get stored battery value and compare it on the current value
            int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
            log("rawLevel:" + currentBattLevel + " scale:" + scale + " status:" + status + " storedPreviousBatLevel:" + storedPreviousBatLevel);
            //If no stored battery value, then save the current battery level
            if (storedPreviousBatLevel == -1) {
                store.setInt(previousBatValueKey, currentBattLevel);
            }
            //Only play the TTS if the current battery level is not the same as previous battery level
            if (storedPreviousBatLevel != -1) {
                checkRulesOnTheList();
            } else
                log("Previous battery level " + storedPreviousBatLevel + " is the same as current level " + currentBattLevel);
        }
    }

    private void checkRulesOnTheList() {

        if (!store.getBoolean(isSwitchOff, false))
            return;

        //Check if user check disable during call settings
        if (store.getBoolean(disableAlertDuringCall, true) && isCallActive(mContext))
            return;

        //Check if user enable max volume for Alert
        if (store.getBoolean(ignoreSystemAudioProfile, false)) {
            if (checkModifyMaxVolumePermissionNoPrompt()) {
                if (store.getBoolean(playSoundWithMaxVolume)) {
                    int maxMusicVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int maxRingVolume = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                    audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVolume,
                            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    audio.setStreamVolume(AudioManager.STREAM_RING, maxRingVolume, 0);
                }
            }
        }

        boolean ttsWasPlayed = false;
        boolean isCharging = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (myBatteryManager.isCharging()) {
                isCharging = true;
            }
        } else if (isCharging(mContext)) {
            isCharging = true;
        }

        if (chargeWithPercentageModels.size() == 0)
            return;

        int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
        int arrayIndexToGet = 0;
        if (isCharging) {
            arrayIndexToGet = 0;

        } else {
            arrayIndexToGet = 1;
        }

        ChargeWithPercentageModel cp = chargeWithPercentageModels.get(arrayIndexToGet);
        for (PercentageModel p : cp.percentageModels) {
            if (ttsWasPlayed)
                return;
            // This will check if the "enable repetition" setting is disabled and battery level = percentage
            if (!store.getBoolean(enableRepeatedAlertForPercentage, true)
                    && p.percentage >= currentBattLevel && p.percentage <= currentBattLevel) {
                log("Play tts in with battery level == percentage");
                store.setInt(previousBatValueKey, currentBattLevel);
                ttsWasPlayed = true;
                playTTS(cp.chargeModel.eventString);
            }
            /*
            This will check if previous battery level is not equal to the current level
             */
            else if (store.getBoolean(enableRepeatedAlertForPercentage, true)
                    && storedPreviousBatLevel != currentBattLevel
            ) {
                log("Play tts in new battery level");
                store.setInt(previousBatValueKey, currentBattLevel);
                ttsWasPlayed = true;
                playTTS(cp.chargeModel.eventString);
            }
        }
    }

    private void playTTS(String tell) {
        tts.setLanguage(Locale.US);
        tts.speak(tell + " " + currentBattLevel, TextToSpeech.QUEUE_ADD, null);
        log("playTTS");

        //Set volume previous volume level set by user
        if (checkModifyMaxVolumePermissionNoPrompt() && store.getBoolean(ignoreSystemAudioProfile, false)) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            audio.setStreamVolume(AudioManager.STREAM_RING, currentRingtoneVolume, 0);
        }
    }

}