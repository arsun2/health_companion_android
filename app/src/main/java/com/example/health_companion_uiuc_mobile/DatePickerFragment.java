/**
 * Source: https://android--examples.blogspot.com/2015/05/how-to-use-datepickerdialog-in-android.html
 */

package com.example.health_companion_uiuc_mobile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    /**
     * index to specify which date picker is called
     * 0 is the date picker from the stats fragment
     * 1 is the date picker from the plan fragment
     */
    private int index = 0;

    public DatePickerFragment() {
    }

    @SuppressLint("ValidFragment")
    public DatePickerFragment(int index) {
        this();
        this.index = index;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // Use the current date as the default date in the date picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new DatePickerDialog instance and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (index == 1) {
            PlanFragment.getInstance().updateDate(year, month + 1, day);
        }
        if (index == 0) {
            StatsFragment.getInstance().updateDate(year, month + 1, day);
        }
    }
}
