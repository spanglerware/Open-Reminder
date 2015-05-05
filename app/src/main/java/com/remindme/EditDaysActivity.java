package com.remindme;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;

/**
 * Created by Scott on 4/18/2015.
 */


public class EditDaysActivity extends Activity {

    private CheckBox checkBoxMon;
    private CheckBox checkBoxTue;
    private CheckBox checkBoxWed;
    private CheckBox checkBoxThu;
    private CheckBox checkBoxFri;
    private CheckBox checkBoxSat;
    private CheckBox checkBoxSun;
    private CheckBox checkBoxMF;
    private CheckBox checkBoxSS;
    private CheckBox checkBoxAll;

    private boolean editReminder;
    private long editId;
    private Reminder reminder;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_select_days);

        //set local variables to interface objects
        assignObjects();

        //load interface with data in case of an edit
        Intent intent = getIntent();
        reminder = intent.getParcelableExtra("reminder");

        if (reminder != null) {
            editReminder = true;
            loadEditData();
        }
    }


    public void assignObjects() {
        checkBoxMon = (CheckBox) findViewById(R.id.checkBoxMonday);
        checkBoxTue = (CheckBox) findViewById(R.id.checkBoxTuesday);
        checkBoxWed = (CheckBox) findViewById(R.id.checkBoxWednesday);
        checkBoxThu = (CheckBox) findViewById(R.id.checkBoxThursday);
        checkBoxFri = (CheckBox) findViewById(R.id.checkBoxFriday);
        checkBoxSat = (CheckBox) findViewById(R.id.checkBoxSaturday);
        checkBoxSun = (CheckBox) findViewById(R.id.checkBoxSunday);
        checkBoxMF = (CheckBox) findViewById(R.id.checkBoxWeekdays);
        checkBoxSS = (CheckBox) findViewById(R.id.checkBoxWeekend);
        checkBoxAll = (CheckBox) findViewById(R.id.checkBoxAllWeek);
    }


    private void loadEditData() {
        boolean days[] = reminder.getDays();

        checkBoxMon.setChecked(days[0]);
        checkBoxTue.setChecked(days[1]);
        checkBoxWed.setChecked(days[2]);
        checkBoxThu.setChecked(days[3]);
        checkBoxFri.setChecked(days[4]);
        checkBoxSat.setChecked(days[5]);
        checkBoxSun.setChecked(days[6]);
    }

    //return Cancelled code if cancel button selected
    public void goCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }


    //save information to database if save button selected
    public void goSave(View view) {
        reminder.setDays(checkBoxMon.isChecked(), checkBoxTue.isChecked(), checkBoxWed.isChecked(),
                checkBoxThu.isChecked(), checkBoxFri.isChecked(), checkBoxSat.isChecked(), checkBoxSun.isChecked());

        Intent data = getIntent();
        data.putExtra("reminder", reminder);
        setResult(RESULT_OK, data);
        finish();
    }


    public void goSelectMF(View view) {
        if (checkBoxMF.isChecked()) {
            updateDays(true, true, true, true, true, false, false);
            checkBoxSS.setChecked(false);
            checkBoxAll.setChecked(false);
        } else {
            updateDays(false, false, false, false, false, false, false);
        }
    }


    public void goSelectWeekend(View view) {
        if (checkBoxSS.isChecked()) {
            checkBoxAll.setChecked(false);
            checkBoxMF.setChecked(false);
            updateDays(false, false, false, false, false, true, true);
        } else {
            updateDays(false,false,false,false,false,false,false);
        }
    }


    public void goSelectAllWeek(View view) {
        if (checkBoxAll.isChecked()) {
            checkBoxSS.setChecked(false);
            checkBoxMF.setChecked(false);
            updateDays(true, true, true, true, true, true, true);
        } else {
            updateDays(false,false,false,false,false,false,false);
        }
    }


    public void updateDays(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun) {
        checkBoxMon.setChecked(mon);
        checkBoxTue.setChecked(tue);
        checkBoxWed.setChecked(wed);
        checkBoxThu.setChecked(thu);
        checkBoxFri.setChecked(fri);
        checkBoxSat.setChecked(sat);
        checkBoxSun.setChecked(sun);
    }


    //end of EditDaysActivity
}
