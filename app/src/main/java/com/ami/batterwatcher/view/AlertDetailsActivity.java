package com.ami.batterwatcher.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.ViewModelProvider;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.AlertViewModel;
import com.ami.batterwatcher.data.ChargeViewModel;
import com.ami.batterwatcher.data.PercentageViewModel;
import com.ami.batterwatcher.databinding.ActivityItemDetailsBinding;
import com.ami.batterwatcher.viewmodels.ChargeModel;
import com.ami.batterwatcher.viewmodels.PercentageModel;

import java.util.List;
import java.util.Locale;

import static com.ami.batterwatcher.service.BatteryService.DEFAULT_CHECK_BATTERY_INTERVAL;

public class AlertDetailsActivity extends BaseActivity {

    private ActivityItemDetailsBinding viewDataBinding;
    private AlertViewModel viewModel;
    private ChargeViewModel chargeViewModel;
    private PercentageViewModel percentageViewModel;
    private RadioGroup modeRadioGroup;
    private int screen_type = 0;
    private int menu_add = 1;
    private int menu_update = 2;
    private int menu_charging = 3;
    private int menu_discharging = 4;
    private int chargeModelId = 0;
    private ChargeModel chargeModel;

    @Override
    protected int setLayout() {
        return R.layout.activity_item_details;
    }

    @Override
    protected View setLayoutBinding() {
        chargeModel = new ChargeModel();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewDataBinding = DataBindingUtil.inflate(inflater, setLayout(), null, false);
        if (getIntent() != null) {
            screen_type = getIntent().getIntExtra("screen_type", 0);
            if (null != getIntent().getParcelableExtra("data")) {
                chargeModel = getIntent().getParcelableExtra("data");
                if (screen_type == 3) {
                    chargeModel.event = 3;
                } else if (screen_type == 4) {
                    chargeModel.event = 3;
                }
                viewDataBinding.setVariable(BR.chargeModel, chargeModel);
            }
        }
        viewDataBinding.executePendingBindings();
        return viewDataBinding.getRoot();
    }

    @Override
    protected void setData() {
        viewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        chargeViewModel = new ViewModelProvider(this).get(ChargeViewModel.class);
        percentageViewModel = new ViewModelProvider(this).get(PercentageViewModel.class);
    }

    @Override
    protected void setViews() {
        showBackButton(true);
        if (screen_type == menu_add) {
            setActivityTitle("New Alert");
        } else if (screen_type == menu_update) {
            setActivityTitle("Edit Alert");
        } else if (screen_type == 3) {
            setActivityTitle("Edit Charging");
            viewDataBinding.textViewMode.setText("Charging");
            chargeModelId = 1;
            percentageViewModel.getAllChargingItems().observe(this, this::setCheckBoxes);
        } else if (screen_type == 4) {
            setActivityTitle("Edit Discharging");
            viewDataBinding.textViewMode.setText("Discharging");
            chargeModelId = 2;
            percentageViewModel.getAllDischargingItems().observe(this, this::setCheckBoxes);
        }
        viewDataBinding.editTextInterval.setText(
                String.format(Locale.US, "%d",
                        store.getInt(screen_type == 3 ?
                                        checkIntervalOnBatteryServiceLevelCheckerForCharging :
                                        checkIntervalOnBatteryServiceLevelCheckerForDisCharging,
                                DEFAULT_CHECK_BATTERY_INTERVAL / 1000)));
        viewDataBinding.checkboxEnableNotificationSoundRepetition.setChecked(
                store.getBoolean(screen_type == 3 ?
                        enableRepeatedAlertForPercentageForCharging :
                        enableRepeatedAlertForPercentageForDisCharging, true));
    }

    private void setCheckBoxes(List<PercentageModel> list) {
        for (PercentageModel p : list) {
            if (p.percentage == 100)
                viewDataBinding.checkbox100.setChecked(p.selected);
            if (p.percentage == 95)
                viewDataBinding.checkbox95.setChecked(p.selected);
            if (p.percentage == 90)
                viewDataBinding.checkbox90.setChecked(p.selected);
            if (p.percentage == 80)
                viewDataBinding.checkbox80.setChecked(p.selected);
            if (p.percentage == 70)
                viewDataBinding.checkbox70.setChecked(p.selected);
            if (p.percentage == 60)
                viewDataBinding.checkbox60.setChecked(p.selected);
            if (p.percentage == 50)
                viewDataBinding.checkbox50.setChecked(p.selected);
            if (p.percentage == 40)
                viewDataBinding.checkbox40.setChecked(p.selected);
            if (p.percentage == 30)
                viewDataBinding.checkbox30.setChecked(p.selected);
            if (p.percentage == 20)
                viewDataBinding.checkbox20.setChecked(p.selected);
            if (p.percentage == 10)
                viewDataBinding.checkbox10.setChecked(p.selected);
            if (p.percentage == 7)
                viewDataBinding.checkbox7.setChecked(p.selected);
            if (p.percentage == 3)
                viewDataBinding.checkbox3.setChecked(p.selected);
        }
    }

    @Override
    protected void setListeners() {
        viewDataBinding.checkbox100.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 1 : 14, 100, chargeModelId, b)));
        viewDataBinding.checkbox95.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 2 : 15, 95, chargeModelId, b)));
        viewDataBinding.checkbox90.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 3 : 16, 90, chargeModelId, b)));
        viewDataBinding.checkbox80.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 4 : 17, 80, chargeModelId, b)));
        viewDataBinding.checkbox70.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 5 : 18, 70, chargeModelId, b)));
        viewDataBinding.checkbox60.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 6 : 19, 60, chargeModelId, b)));
        viewDataBinding.checkbox50.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 7 : 20, 50, chargeModelId, b)));
        viewDataBinding.checkbox40.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 8 : 21, 40, chargeModelId, b)));
        viewDataBinding.checkbox30.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 9 : 22, 30, chargeModelId, b)));
        viewDataBinding.checkbox20.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 10 : 23, 20, chargeModelId, b)));
        viewDataBinding.checkbox10.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 11 : 24, 10, chargeModelId, b)));
        viewDataBinding.checkbox7.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 12 : 25, 7, chargeModelId, b)));
        viewDataBinding.checkbox3.setOnCheckedChangeListener((compoundButton, b) ->
                percentageViewModel.insert(new PercentageModel(screen_type == 3 ? 13 : 26, 3, chargeModelId, b)));

        viewDataBinding.checkboxEnableNotificationSoundRepetition
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    store.setBoolean(screen_type == 3 ?
                            enableRepeatedAlertForPercentageForCharging :
                            enableRepeatedAlertForPercentageForDisCharging, b);
                });
        viewDataBinding.buttonInterval.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(viewDataBinding.editTextInterval.getText().toString())) {
                store.setInt(screen_type == 3 ?
                                checkIntervalOnBatteryServiceLevelCheckerForCharging :
                                checkIntervalOnBatteryServiceLevelCheckerForDisCharging,
                        Integer.parseInt(viewDataBinding.editTextInterval.getText().toString()));
                showDialogOkButton("Interval set successfully!");
            }
        });

        viewDataBinding.radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioButton_media) {
                    chargeModel.event = 1;
                    viewDataBinding.linearLayoutMedia.setVisibility(View.VISIBLE);
                    viewDataBinding.linearLayoutRingtone.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutTextToSpeech.setVisibility(View.GONE);
                    viewDataBinding.buttonRingtone.setText("select ringtone");
                    viewDataBinding.editTextTextToSpeechText.setText("");
                } else if (i == R.id.radioButton_ringtone) {
                    chargeModel.event = 2;
                    viewDataBinding.linearLayoutMedia.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutRingtone.setVisibility(View.VISIBLE);
                    viewDataBinding.linearLayoutTextToSpeech.setVisibility(View.GONE);
                    viewDataBinding.editTextMedia.setText("");
                    viewDataBinding.editTextTextToSpeechText.setText("");
                } else if (i == R.id.radioButton_textToSpeech) {
                    chargeModel.event = 3;
                    viewDataBinding.linearLayoutMedia.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutRingtone.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutTextToSpeech.setVisibility(View.VISIBLE);
                    viewDataBinding.editTextMedia.setText("");
                    viewDataBinding.buttonRingtone.setText("Select Ringtone");
                }
            }
        });

        if (chargeModel != null) {
            if (chargeModel.event == 1) {
                viewDataBinding.radioButtonMedia.setChecked(true);
            } else if (chargeModel.event == 2) {
                viewDataBinding.radioButtonRingtone.setChecked(true);
            } else if (chargeModel.event == 3) {
                viewDataBinding.radioButtonTextToSpeech.setChecked(true);
            }
        }

        viewDataBinding.buttonSave.setOnClickListener((View.OnClickListener) view -> {
            if (!TextUtils.isEmpty(viewDataBinding.editTextMedia.getText().toString())) {
                chargeModel.eventString = viewDataBinding.editTextMedia.getText().toString();
            }
            if (viewDataBinding.buttonRingtone.getText().toString().equalsIgnoreCase("select ringtone")) {
                chargeModel.eventString = viewDataBinding.buttonRingtone.getText().toString();
            }
            if (!TextUtils.isEmpty(viewDataBinding.editTextTextToSpeechText.getText().toString())) {
                chargeModel.eventString = viewDataBinding.editTextTextToSpeechText.getText().toString();
            }

            //data check
            if (chargeModel.event == -1) {
                showDialogOkButton("Select event");
                return;
            } else if (chargeModel.event == 3 && TextUtils.isEmpty(viewDataBinding.editTextTextToSpeechText.getText().toString())) {
                showDialogOkButton("Enter text to speech text");
                return;
            }

            chargeViewModel.insert(chargeModel);

            onBackPressed();
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_or_add_alert, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {

        }
        return (super.onOptionsItemSelected(item));
    }*/

}