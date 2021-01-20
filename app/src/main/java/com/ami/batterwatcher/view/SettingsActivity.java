package com.ami.batterwatcher.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.ActivitySettingsBinding;

import java.util.Calendar;
import java.util.Locale;

import static com.ami.batterwatcher.service.BatteryService.DEFAULT_CHECK_BATTERY_INTERVAL;

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
        viewDataBinding.editTextInterval.setText(
                String.format(Locale.US, "%d",
                        store.getInt(checkIntervalOnBatteryServiceLevelChecker,
                                DEFAULT_CHECK_BATTERY_INTERVAL / 1000)));
        viewDataBinding.checkboxEnableNotificationSoundRepetition.setChecked(
                store.getBoolean(enableRepeatedAlertForPercentage, true));
        viewDataBinding.checkboxAudioProfile.setChecked(
                store.getBoolean(ignoreSystemAudioProfile, true));
        viewDataBinding.checkboxPlayMaxVolume.setChecked(
                store.getBoolean(playSoundWithMaxVolume, false));
        viewDataBinding.checkboxDisableDuringCall.setChecked(
                store.getBoolean(disableAlertDuringCall, true));
        viewDataBinding.textViewStartTime.setText(
                convertTimePickerTime(store.getInt(startTimeHr), store.getInt(startTimeMn)));
        viewDataBinding.textViewEndTime.setText(
                convertTimePickerTime(store.getInt(stopTimeHr), store.getInt(stopTimeMn)));
    }

    @Override
    protected void setListeners() {
        viewDataBinding.linearLayoutStartTime.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(mContext,
                    (timePicker, hourOfDay, selectedMinute) -> {

                        viewDataBinding.textViewStartTime.
                                setText(convertTimePickerTime(hourOfDay, selectedMinute));
                        store.setInt(startTimeHr, hourOfDay);
                        store.setInt(startTimeMn, selectedMinute);
                    },
                    hour, minute, false);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        viewDataBinding.linearLayoutEndTime.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(mContext,
                    (timePicker, hourOfDay, selectedMinute) -> {
                        viewDataBinding.textViewEndTime.
                                setText(convertTimePickerTime(hourOfDay, selectedMinute));
                        store.setInt(stopTimeHr, hourOfDay);
                        store.setInt(stopTimeMn, selectedMinute);
                    },
                    hour, minute, false);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        viewDataBinding.checkboxEnableNotificationSoundRepetition.setOnCheckedChangeListener((compoundButton, b) -> {
            store.setBoolean(enableRepeatedAlertForPercentage, b);
        });
        viewDataBinding.checkboxAudioProfile.setOnCheckedChangeListener((compoundButton, b) -> {
            if (checkModifyMaxVolumePermission()) {
                store.setBoolean(ignoreSystemAudioProfile, b);
            }
        });
        viewDataBinding.checkboxPlayMaxVolume.setOnCheckedChangeListener((compoundButton, b) -> {
            store.setBoolean(playSoundWithMaxVolume, b);
        });
        viewDataBinding.checkboxDisableDuringCall.setOnCheckedChangeListener((compoundButton, b) -> {
            store.setBoolean(disableAlertDuringCall, b);
        });

        viewDataBinding.buttonInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(viewDataBinding.editTextInterval.getText().toString())) {
                    store.setInt(checkIntervalOnBatteryServiceLevelChecker,
                            Integer.parseInt(viewDataBinding.editTextInterval.getText().toString()));
                    showDialogOkButton("Interval set successfully!");
                }
            }
        });
    }

    @Override
    protected void setData() {

    }
}
