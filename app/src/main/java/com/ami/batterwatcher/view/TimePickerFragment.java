package com.ami.batterwatcher.view;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Time t = new Time();
        t.setToNow();

        // Use the current time as the default values for the picker
        /*Bundle args = getArguments();
        int hour = args.getInt(GLarmStrings.FRAGMENT_ARGS_TIME_PICKER_HOUR, t.hour);
        int minute = args.getInt(GLarmStrings.FRAGMENT_ARGS_TIME_PICKER_MINUTE, t.minute);*/

        // Create a new instance of TimePickerDialog and return it
        //return new TimePickerDialog(getActivity(), this, hour, minute, GLarmApp.is24HourEnabled(getActivity()));
        return null;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // TODO Auto-generated method stub
    }
}