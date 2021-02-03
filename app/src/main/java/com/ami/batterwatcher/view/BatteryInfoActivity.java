package com.ami.batterwatcher.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.BatteryInfoActivityBinding;
import com.ami.batterwatcher.service.BatteryService;
import com.ami.batterwatcher.util.BatteryUtil;

import java.util.Date;
import java.util.Locale;

public class BatteryInfoActivity extends BaseActivity {

    private BatteryInfoActivityBinding viewDataBinding;
    private BatteryManager mBatteryManager;
    private BatteryUtil batteryUtil;
    private BroadcastReceiver2 mBatInfoReceiver;
    private BatteryService batteryService;
    private boolean mBounded;
    private Handler handler = new Handler();
    private Runnable runnable;


    @Override
    protected int setLayout() {
        return R.layout.battery_info_activity;
    }

    @Override
    protected View setLayoutBinding() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewDataBinding = DataBindingUtil.inflate(inflater, setLayout(), null, false);
        viewDataBinding.executePendingBindings();
        return viewDataBinding.getRoot();
    }

    @Override
    protected void setViews() {
        showBackButton(true);
        setActivityTitle("Battery Details");
    }

    @Override
    protected void setListeners() {
        batteryUtil = new BatteryUtil();
        mBatteryManager = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);

        viewDataBinding.buttonAppList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent batteryInfoIntent = new Intent(mContext, AppListActivity.class);
                startActivity(batteryInfoIntent);
            }
        });

        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                setInfo();
                handler.postDelayed(runnable, 1000);
            }
        }, 0);
    }

    private void startBatteryService() {
        mBatInfoReceiver = new BroadcastReceiver2();
        final IntentFilter intentFilter = new IntentFilter("YourAction");
        registerReceiver(mBatInfoReceiver, intentFilter);
    }

    private void stopBatteryService() {
        if (mBatInfoReceiver != null) {
            unregisterReceiver(mBatInfoReceiver);
        }
        mBatInfoReceiver = null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBatteryService();
        handler.removeCallbacks(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBatteryService();
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
    }

    @Override
    protected void setData() {

    }

    private class BroadcastReceiver2 extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            logStatic("xxx BatteryInfoActivity onReceive " + intent.getAction());
            if (intent.getAction().equalsIgnoreCase("YourAction")) {
                setInfo();
            }
        }
    }

    private void setInfo() {
        int percent = (store.getInt(bat_level) * 100) / store.getInt(bat_scale);
        viewDataBinding.textViewTechnology.setText("Technology: " + store.getString(bat_technology));
        viewDataBinding.textViewVoltage.setText("Voltage: " + (Double.valueOf(store.getInt(bat_voltage)) / 1000.0) + " V");
        viewDataBinding.textViewTemperature.setText("Temperature: " + (Double.valueOf(store.getInt(bat_temperature)) / 10.0) + " C");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            viewDataBinding.textViewCurrentAvg.setText("Average Current: " + store.getInt(bat_current_avg) + " mA");

            viewDataBinding.textViewCurrent.setText("Current: " + store.getInt(bat_current) + " mA");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                viewDataBinding.textViewStatus.setText(String.format(Locale.US,
                        "%s speed: %s (%s mA)",
                        BaseActivity.isCharging(mContext) ? "Charging" : "Discharging",
                        batteryUtil.getChargingAndDischargingSpeed(store.getInt(bat_current)), store.getInt(bat_current)));
            }
        }
        viewDataBinding.textViewHealth.setText("Health: " +
                batteryUtil.getHealthString(store.getInt(bat_health)));
        viewDataBinding.textViewCharging.setText("Charging: " +
                batteryUtil.getStatusString(store.getInt(bat_status)) +
                "(" + batteryUtil.getPlugTypeString(store.getInt(bat_plugged)) + ")");
        viewDataBinding.textViewBatteryPercent.setText("Level " + percent + "%");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            long time = mBatteryManager.computeChargeTimeRemaining();
            Date d = new Date(time * 1000);
            viewDataBinding.textViewChargingTimeRemaining.setText("" + d.toString());
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
