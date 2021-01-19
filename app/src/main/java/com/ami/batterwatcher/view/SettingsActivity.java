package com.ami.batterwatcher.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.databinding.DataBindingUtil;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.ActivitySettingsBinding;

import java.util.Calendar;

public class SettingsActivity extends BaseActivity {
    private ActivitySettingsBinding viewDataBinding;

    @Override
    protected int setLayout() {
        return R.layout.activity_settings;
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
        setActivityTitle("Settings");
    }

    @Override
    protected void setListeners() {
        viewDataBinding.linearLayoutStartTime.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(mContext,
                    (timePicker, selectedHour, selectedMinute) ->
                            viewDataBinding.textViewStartTime.
                                    setText(selectedHour + ":" + selectedMinute),
                    hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        viewDataBinding.linearLayoutEndTime.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(mContext,
                    (timePicker, selectedHour, selectedMinute) ->
                            viewDataBinding.textViewEndTime.
                                    setText(selectedHour + ":" + selectedMinute),
                    hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });
    }

    @Override
    protected void setData() {

    }
}
