package com.ami.batterwatcher.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.ami.batterwatcher.databinding.ActivityMainBinding;
import com.ami.batterwatcher.service.BatteryService;
import com.ami.batterwatcher.viewmodels.AlertModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding viewDataBinding;
    private AlertViewModel viewModel;
    private RecyclerView recyclerView_list;
    private AlertListAdapter listAdapter;
    private List<AlertModel> models;
    private BroadcastReceiver mBatInfoReceiver;
    private boolean initSuccessfull = false;
    private TextToSpeech tts;
    private int rawLevel;

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
        models = new ArrayList<>();
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
    }

    @Override
    protected void setListeners() {
        //registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        startService(new Intent(this, BatteryService.class));
        mBatInfoReceiver = new BroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter("YourAction");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBatInfoReceiver, intentFilter);

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
        } else if (itemId == R.id.menu_add) {
            Intent addIntent = new Intent(MainActivity.this, AlertDetailsActivity.class);
            addIntent.putExtra("screen_type", 1);
            startActivity(addIntent);
            return true;
        } else if (itemId == R.id.menu_more) {
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBatInfoReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBatInfoReceiver);
        }
        mBatInfoReceiver = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getAll().observe(this, alertModels -> {
            models.clear();
            models.addAll(alertModels);
            listAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class BroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            rawLevel = intent.getIntExtra("batteryLevel", -1);
            int scale = intent.getIntExtra("scale", -1);
            int status = intent.getIntExtra("status", -1);
            log("rawLevel:" + rawLevel + " scale:" + scale + " status:" + status);
            //Get stored battery value and compare it on the current value
            int storedPreviousBatLevel = store.getInt(previousBatValueKey, -1);
            //If no stored battery value, then save the current battery level
            if (storedPreviousBatLevel == -1) {
                store.setInt(previousBatValueKey, rawLevel);
            }
            //Only play the TTS if the current battery level is not the same as previous battery level
            if (storedPreviousBatLevel != -1 //&& storedPreviousBatLevel != rawLevel
            ) {
                store.setInt(previousBatValueKey, rawLevel);
                checkRulesOnTheList();
            } else
                log("Previous battery level " + storedPreviousBatLevel + " is the same as current level " + rawLevel);
        }
    }

    private void checkRulesOnTheList() {
        boolean ttsWasPlayed = false;
        for (AlertModel a : models) {
            if (ttsWasPlayed)
                return;
            if (a.mode == 3 || a.mode == 4) {
                //Charging level
                if (a.mode == 3 && rawLevel >= a.modeValue) {
                    ttsWasPlayed = true;
                    playTTS(a.eventString);
                }
                //Dis-charging level
                if (a.mode == 4 && a.modeValue <= rawLevel) {
                    ttsWasPlayed = true;
                    playTTS(a.eventString);
                }
            }
        }
    }

    private void playTTS(String tell) {
        tts.setLanguage(Locale.US);
        tts.speak(tell, TextToSpeech.QUEUE_ADD, null);
        log("playTTS");
    }

}