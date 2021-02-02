package com.ami.batterwatcher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.BatteryInfoActivityBinding;
import com.ami.batterwatcher.util.BatteryUtil;

import java.util.Date;
import java.util.Locale;

public class BatteryInfoActivity extends BaseActivity {

    private BatteryInfoActivityBinding viewDataBinding;
    private BatteryManager mBatteryManager;
    private BatteryUtil batteryUtil;

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
        this.registerReceiver(this.batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mBatteryManager = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);

        viewDataBinding.buttonAppList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent batteryInfoIntent = new Intent(mContext, AppListActivity.class);
                startActivity(batteryInfoIntent);
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryInfo);
    }

    @Override
    protected void setData() {

    }

    private BroadcastReceiver batteryInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            boolean isPresent = intent.getBooleanExtra("present", false);

            Bundle bundle = intent.getExtras();
            String str = bundle.toString();
            log("Battery Info " + str);

            if (isPresent) {
                int percent = (level * 100) / scale;
                viewDataBinding.textViewTechnology.setText("Technology: " + technology);
                viewDataBinding.textViewVoltage.setText("Voltage: " + (Double.valueOf(voltage) / 1000.0) + " V");
                viewDataBinding.textViewTemperature.setText("Temperature: " + (Double.valueOf(temperature) / 10.0) + " C");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int averageCurrent = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
                    viewDataBinding.textViewCurrentAvg.setText("Average Current: " + averageCurrent + " mA");

                    int current = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                    viewDataBinding.textViewCurrent.setText("Current: " + current + " mA");

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        viewDataBinding.textViewStatus.setText(String.format(Locale.US,
                                "%s speed: %s (%s mA)",
                                BaseActivity.isCharging(mContext) ? "Charging" : "Discharging",
                                batteryUtil.getChargingAndDischargingSpeed(current), current));
                    }
                }
                viewDataBinding.textViewHealth.setText("Health: " + batteryUtil.getHealthString(health));
                viewDataBinding.textViewCharging.setText("Charging: " + batteryUtil.getStatusString(status) + "(" + batteryUtil.getPlugTypeString(plugged) + ")");
                viewDataBinding.textViewBatteryPercent.setText("Level " + percent + "%");
            } else {
                viewDataBinding.textViewBatteryPercent.setText("Battery not present!!!");
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                long time = mBatteryManager.computeChargeTimeRemaining();
                Date d = new Date(time * 1000);
                viewDataBinding.textViewChargingTimeRemaining.setText("" + d.toString());
            }
        }
    };

}
