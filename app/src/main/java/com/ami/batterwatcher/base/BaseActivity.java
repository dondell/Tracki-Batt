package com.ami.batterwatcher.base;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.ami.batterwatcher.BuildConfig;
import com.ami.batterwatcher.util.PrefStore;

public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    private ActionBar actionBar;
    public LayoutInflater inflater;
    public PrefStore store;
    //SharedPreference keys
    public static String previousBatValueKey = "previousBatValueKey";//int
    public static String isSleepModeDisabledKey = "isSleepModeDisabledKey";//boolean
    public static String startTimeValueKey = "startTimeValueKey";//time
    public static String endTimeValueKey = "endTimeValueKey";//time

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the layout binding
        setContentView(setLayoutBinding());

        //or set via layout files directly
        //setContentView(setLayout());

        mContext = BaseActivity.this;
        store = new PrefStore(mContext);
        setData();
        setView();
        setViews();
        setListeners();
    }

    protected abstract int setLayout();

    protected abstract View setLayoutBinding();

    protected abstract void setViews();

    protected abstract void setListeners();

    protected abstract void setData();

    private void setView() {
        actionBar = getSupportActionBar();
    }

    public void showBackButton(boolean flag) {
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(flag);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void log(String logMessage) {
        if (BuildConfig.DEBUG) {
            Log.e("xxx", "xxx " + logMessage);
        }
    }

    public void setActivityTitle(String title) {
        setTitle(title);
    }
}
