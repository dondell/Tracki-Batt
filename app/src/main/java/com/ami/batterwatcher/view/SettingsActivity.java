package com.ami.batterwatcher.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.databinding.DataBindingUtil;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.databinding.ActivitySettingsBinding;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.ami.batterwatcher.service.BatteryService.DEFAULT_CHECK_BATTERY_INTERVAL;

public class SettingsActivity extends BaseActivity {
    private ActivitySettingsBinding viewDataBinding;
    private boolean initTTSSuccessfull = false;
    private TextToSpeech tts;
    private Voice defaultTTSVoice;

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
        viewDataBinding.radioButtonMale.setChecked(store.getInt(ttsVoiceType, 2) == 1);
        viewDataBinding.radioButtonFemale.setChecked(store.getInt(ttsVoiceType, 2) == 2);
    }

    @Override
    protected void setListeners() {
        viewDataBinding.linearLayoutStartTime.setOnClickListener(view -> {
            showTimePicker(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker timePicker, int hourOfDay,
                                          int selectedMinute) {
                    viewDataBinding.textViewStartTime.
                            setText(convertTimePickerTime(hourOfDay, selectedMinute));
                    store.setInt(startTimeHr, hourOfDay);
                    store.setInt(startTimeMn, selectedMinute);
                }
            });
        });

        viewDataBinding.linearLayoutEndTime.setOnClickListener(view -> {
            /*Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);*/
            showTimePicker(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker timePicker, int hourOfDay,
                                          int selectedMinute) {
                    viewDataBinding.textViewEndTime.
                            setText(convertTimePickerTime(hourOfDay, selectedMinute));
                    store.setInt(stopTimeHr, hourOfDay);
                    store.setInt(stopTimeMn, selectedMinute);
                }
            });
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

        viewDataBinding.radioButtonMale.setOnCheckedChangeListener(((compoundButton, b) -> {
            store.setInt(ttsVoiceType, b ? 1 : 2);
            if (b)
                speakTest();
        }));
        viewDataBinding.radioButtonFemale.setOnCheckedChangeListener(((compoundButton, b) -> {
            store.setInt(ttsVoiceType, b ? 2 : 1);
            if (b)
                speakTest();
        }));

        initializeTTS();
    }

    @Override
    protected void setData() {

    }

    private void initializeTTS() {
        initTTSSuccessfull = false;
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    initTTSSuccessfull = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        defaultTTSVoice = tts.getDefaultVoice();
                    }
                }
            }
        });
    }

    private void speakTest() {
        tts.setLanguage(Locale.US);
        Set<String> a = new HashSet<>();
        a.add("male");
        Voice v = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            v = new Voice(store.getInt(ttsVoiceType, 2) == 1 ? "en-us-x-sfg#male_2-local" : "es-us-x-sfb#female_1-local",
                    new Locale("en", "US"),
                    400, 200, true, a);
            int result = tts.setVoice(v);
            tts.setSpeechRate(0.8f);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                log("This Language is not supported");
                if (null != defaultTTSVoice)
                    tts.setVoice(defaultTTSVoice);
            }
        }
        tts.speak("Testing voice", TextToSpeech.QUEUE_ADD, null);
        log("playTTS");
    }
}
