package com.ami.batterwatcher.view;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.ViewModelProvider;

import com.ami.batterwatcher.R;
import com.ami.batterwatcher.base.BaseActivity;
import com.ami.batterwatcher.data.AlertViewModel;
import com.ami.batterwatcher.databinding.ActivityItemDetailsBinding;
import com.ami.batterwatcher.viewmodels.AlertModel;

public class AlertDetailsActivity extends BaseActivity {

    private ActivityItemDetailsBinding viewDataBinding;
    private AlertViewModel viewModel;
    private RadioGroup modeRadioGroup;
    private int screen_type = 0;
    private int menu_add = 1;
    private int menu_update = 2;
    private AlertModel alertModel;

    @Override
    protected int setLayout() {
        return R.layout.activity_item_details;
    }

    @Override
    protected View setLayoutBinding() {
        alertModel = new AlertModel();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewDataBinding = DataBindingUtil.inflate(inflater, setLayout(), null, false);
        if (getIntent() != null) {
            screen_type = getIntent().getIntExtra("screen_type", 0);
            if (screen_type == menu_update) {
                alertModel = getIntent().getParcelableExtra("data");
                viewDataBinding.setVariable(BR.alert, alertModel);
            }
        }
        viewDataBinding.executePendingBindings();
        return viewDataBinding.getRoot();
    }

    @Override
    protected void setData() {
        viewModel = new ViewModelProvider(this).get(AlertViewModel.class);
    }

    @Override
    protected void setViews() {
        showBackButton(true);
        if (screen_type == menu_add) {
            setActivityTitle("New Alert");
        } else if (screen_type == menu_update) {
            setActivityTitle("Edit Alert");
        }
        modeRadioGroup = findViewById(R.id.radioGroup_1);
    }

    @Override
    protected void setListeners() {
        modeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioButton_plugged) {
                    alertModel.mode = 1;
                    viewDataBinding.linearLayoutChargingLevel.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutDischargingLevel.setVisibility(View.GONE);
                    viewDataBinding.editTextChargingValue.setText("");
                    viewDataBinding.editTextDischargingValue.setText("");
                } else if (i == R.id.radioButton_unplugged) {
                    alertModel.mode = 2;
                    viewDataBinding.linearLayoutChargingLevel.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutDischargingLevel.setVisibility(View.GONE);
                    viewDataBinding.editTextChargingValue.setText("");
                    viewDataBinding.editTextDischargingValue.setText("");
                } else if (i == R.id.radioButton_chargingLevel) {
                    alertModel.mode = 3;
                    viewDataBinding.linearLayoutChargingLevel.setVisibility(View.VISIBLE);
                    viewDataBinding.linearLayoutDischargingLevel.setVisibility(View.GONE);
                    viewDataBinding.editTextDischargingValue.setText("");
                } else if (i == R.id.radioButton_dischargingLevel) {
                    alertModel.mode = 4;
                    viewDataBinding.linearLayoutChargingLevel.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutDischargingLevel.setVisibility(View.VISIBLE);
                    viewDataBinding.editTextChargingValue.setText("");
                }
            }
        });
        viewDataBinding.radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioButton_media) {
                    alertModel.event = 1;
                    viewDataBinding.linearLayoutMedia.setVisibility(View.VISIBLE);
                    viewDataBinding.linearLayoutRingtone.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutTextToSpeech.setVisibility(View.GONE);
                    viewDataBinding.buttonRingtone.setText("select ringtone");
                    viewDataBinding.editTextTextToSpeechText.setText("");
                } else if (i == R.id.radioButton_ringtone) {
                    alertModel.event = 2;
                    viewDataBinding.linearLayoutMedia.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutRingtone.setVisibility(View.VISIBLE);
                    viewDataBinding.linearLayoutTextToSpeech.setVisibility(View.GONE);
                    viewDataBinding.editTextMedia.setText("");
                    viewDataBinding.editTextTextToSpeechText.setText("");
                } else if (i == R.id.radioButton_textToSpeech) {
                    alertModel.event = 3;
                    viewDataBinding.linearLayoutMedia.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutRingtone.setVisibility(View.GONE);
                    viewDataBinding.linearLayoutTextToSpeech.setVisibility(View.VISIBLE);
                    viewDataBinding.editTextMedia.setText("");
                    viewDataBinding.buttonRingtone.setText("select ringtone");
                }
            }
        });

        if (screen_type == menu_update && alertModel != null) {
            if (alertModel.mode == 1) {
                viewDataBinding.radioButtonPlugged.performClick();
            } else if (alertModel.mode == 2) {
                viewDataBinding.radioButtonUnplugged.setChecked(true);
            } else if (alertModel.mode == 3) {
                viewDataBinding.radioButtonChargingLevel.setChecked(true);
            } else if (alertModel.mode == 4) {
                viewDataBinding.radioButtonDischargingLevel.setChecked(true);
            }

            if (alertModel.event == 1) {
                viewDataBinding.radioButtonMedia.setChecked(true);
            } else if (alertModel.event == 2) {
                viewDataBinding.radioButtonRingtone.setChecked(true);
            } else if (alertModel.event == 3) {
                viewDataBinding.radioButtonTextToSpeech.setChecked(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_or_add_alert, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            alertModel.name = viewDataBinding.editText1.getText().toString();
            alertModel.description = viewDataBinding.editTextDescription.getText().toString();
            if (!TextUtils.isEmpty(viewDataBinding.editTextChargingValue.getText().toString())) {
                alertModel.modeValue = Integer.parseInt(viewDataBinding.editTextChargingValue.getText().toString());
            }
            if (!TextUtils.isEmpty(viewDataBinding.editTextDischargingValue.getText().toString())) {
                alertModel.modeValue = Integer.parseInt(viewDataBinding.editTextDischargingValue.getText().toString());
            }
            if (!TextUtils.isEmpty(viewDataBinding.editTextMedia.getText().toString())) {
                alertModel.eventString = viewDataBinding.editTextMedia.getText().toString();
            }
            if (viewDataBinding.buttonRingtone.getText().toString().equalsIgnoreCase("select ringtone")) {
                alertModel.eventString = viewDataBinding.buttonRingtone.getText().toString();
            }
            if (!TextUtils.isEmpty(viewDataBinding.editTextTextToSpeechText.getText().toString())) {
                alertModel.eventString = viewDataBinding.editTextTextToSpeechText.getText().toString();
            }

            //data check
            if (null != alertModel) {
                if (alertModel.mode == -1) {
                    showMissingField("Select mode");
                    return true;
                } else if (alertModel.mode == 3 && TextUtils.isEmpty(viewDataBinding.editTextChargingValue.getText().toString())) {
                    showMissingField("Enter charging level");
                    return true;
                } else if (alertModel.mode == 4 && TextUtils.isEmpty(viewDataBinding.editTextDischargingValue.getText().toString())) {
                    showMissingField("Enter discharging level");
                    return true;
                } else if (alertModel.event == -1) {
                    showMissingField("Select event");
                    return true;
                } else if (alertModel.event == 3 && TextUtils.isEmpty(viewDataBinding.editTextTextToSpeechText.getText().toString())) {
                    showMissingField("Enter text to speech text");
                    return true;
                } else if (TextUtils.isEmpty(viewDataBinding.editText1.getText().toString())) {
                    showMissingField("Enter alert name");
                    return true;
                } else if (TextUtils.isEmpty(viewDataBinding.editTextDescription.getText().toString())) {
                    showMissingField("Enter alert description");
                    return true;
                }

                if (screen_type == menu_add)
                    viewModel.insert(alertModel);
                else
                    viewModel.update(alertModel);

                onBackPressed();
            }
        }
        return (super.onOptionsItemSelected(item));
    }

    private void showMissingField(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


}