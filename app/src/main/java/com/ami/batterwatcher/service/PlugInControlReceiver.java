package com.ami.batterwatcher.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.util.PrefStore;

import static com.ami.batterwatcher.base.BaseActivity.isCharging;

public class PlugInControlReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        PrefStore store = new PrefStore(context);

        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            BaseActivity.logStatic("ACTION_POWER_CONNECTED");
            store.setBoolean(isCharging, true);
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            BaseActivity.logStatic("ACTION_POWER_DISCONNECTED");
            store.setBoolean(isCharging, false);
        }
    }
}
