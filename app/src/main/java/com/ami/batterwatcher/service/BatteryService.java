package com.ami.batterwatcher.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.util.PrefStore;

public class BatteryService extends Service {

    public static final int DEFAULT_CHECK_BATTERY_INTERVAL = 10000;
    private static final int CHECK_BATTERY_INTERVAL = DEFAULT_CHECK_BATTERY_INTERVAL;

    private double batteryLevel;
    private Handler handler;
    private PrefStore store;

    private BroadcastReceiver batInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent batteryIntent) {
            int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (rawlevel >= 0 && scale > 0) {
                batteryLevel = (rawlevel * 100.0) / scale;
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
            Log.e("Battery status is", batteryLevel + "mm cached. Interval: " + 10000);

            Intent intent = new Intent("YourAction");
            Bundle bundle = new Bundle();
            bundle.putInt("batteryLevel", (int) batteryLevel);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    };

    @Override
    public void onCreate() {
        store = new PrefStore(this);
        handler = new Handler();
        handler.postDelayed(checkBatteryStatusRunnable, CHECK_BATTERY_INTERVAL);
        registerReceiver(batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batInfoReceiver);
        handler.removeCallbacks(checkBatteryStatusRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}