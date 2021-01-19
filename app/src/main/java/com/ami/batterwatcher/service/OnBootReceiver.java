package com.ami.batterwatcher.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            Intent serviceLauncher = new Intent(context, BatteryService.class);

            context.startService(serviceLauncher);
        }
    }
}
