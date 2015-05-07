package com.remindme;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Scott on 4/18/2015.
 */


public class EditActivity extends Activity implements NumberPicker.OnValueChangeListener {
    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;

    private EditText etReminder;
    private TextView textViewFrequency;
    private TextView textViewFrequencyHeader;
    private TextView textViewSelectTimesHeader;
    private TextView textViewCustomTimeFrom;
    private TextView textViewCustomTimeTo;
    private TextView textViewCustomDays;

    private RadioButton radio9to5;
    private RadioButton radioCustomTimes;
    private RadioButton radioMF;
    private RadioButton radioCustomDays;
    private RadioButton radioSingleUse;
    private RadioButton radioRecurring;
    private RadioButton radioNotification;
    private RadioButton radioAlarm;
    private Spinner spinnerSelectAlarm;

    private ArrayList<String> alarms;
    private ArrayList<Integer> alarmsResId;
    private ArrayList<String> notifications;
    private ArrayAdapter listAdapter;

    private int tpdHourFrom;
    private int tpdMinuteFrom;
    private int tpdHourTo;
    private int tpdMinuteTo;
    private int npMinute;
    private boolean booleanTimeFrom;
    private int dialogFlag;

    private long editId;
    private boolean editReminder;
    private Reminder reminder;


    //todo need to fix layout, user does not see buttons unless they scroll

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_edit);

        //set local variables to interface objects
        assignObjects();

        //load interface with data in case of an edit
        Intent intent = getIntent();
        reminder = intent.getParcelableExtra("reminder");

        if (reminder != null) { //todo this should now always be true
            editReminder = true;
            loadEditData(intent);
        } else {
            editReminder = false;
        }

        loadNotificationList();
        loadAlarmList();

        if (radioAlarm.isChecked()) {
            listAdapter = new ArrayAdapter(getBaseContext(), R.layout.alarm_layout, R.id.textViewMessage, alarms);
        } else {
            listAdapter = new ArrayAdapter(getBaseContext(), R.layout.alarm_layout, R.id.textViewMessage, notifications);
        }
        spinnerSelectAlarm.setAdapter(listAdapter);
        spinnerSelectAlarm.setSelection(reminder.getMessageId());
        registerSpinnerOnItemSelectedEvent();
        setUsage();
        dialogFlag = 0;
    }

    private void loadAlarmList() {
        alarms = new ArrayList<String>();
        alarmsResId = new ArrayList<Integer>();
        alarms.add("Alarm 1"); alarmsResId.add(R.raw.alarm1);
        alarms.add("Alarm 2"); alarmsResId.add(R.raw.alarm2);
        alarms.add("Alarm 3"); alarmsResId.add(R.raw.alarm3);
        alarms.add("Alarm 4"); alarmsResId.add(R.raw.alarm4);
        alarms.add("Alarm 5"); alarmsResId.add(R.raw.alarm5);
        alarms.add("Alarm 6"); alarmsResId.add(R.raw.alarm6);
        alarms.add("Alarm 7"); alarmsResId.add(R.raw.alarm7);
        alarms.add("Alarm 8"); alarmsResId.add(R.raw.alarm8);
    }

    private void loadNotificationList() {
        notifications = new ArrayList<String>();
        notifications.add("Toast short");
        notifications.add("Toast long");
        notifications.add("Notification");
    }

    private void registerSpinnerOnItemSelectedEvent() {
        spinnerSelectAlarm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), alarmsResId.get(position));
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //no updates needed
            }

        });
    }

    //todo register mediaplayer onCompletion listener then release the mediaplayer

    //todo redo the double playing time picker, not working correctly and can be confusing
    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeFrom, timeTo;
            dialogFlag++;
            if (dialogFlag == 1) {
                tpdHourFrom = hourOfDay;
                tpdMinuteFrom = minute;
            } else {
                tpdHourTo = hourOfDay;
                tpdMinuteTo = minute;
                dialogFlag = 0;
            }

            timeFrom = ((tpdHourFrom > 9) ? "" + tpdHourFrom : "0" + tpdHourFrom) + ":" +
                    ((tpdMinuteFrom > 9) ? "" + tpdMinuteFrom : "0" + tpdMinuteFrom);
            timeTo = ((tpdHourTo > 9) ? "" + tpdHourTo : "0" + tpdHourTo) + ":" +
                    ((tpdMinuteTo > 9) ? "" + tpdMinuteTo : "0" + tpdMinuteTo);
            updateTimes(timeFrom, timeTo);
        }
    };

    private TimePickerDialog showTimePickerDialog(int hour, int minutes, boolean militaryTime, TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minutes, militaryTime);
        timePickerDialog.show();
        return timePickerDialog;
    }

    public void goChangeFrequency(View view) {
        if (radioRecurring.isChecked()) {
            showNumberPickerDialog();
        } else {
            //todo showTimePickerDialog(12,0,true,timeSetListener);
        }
    }

    private void updateTimes(String timeFrom, String timeTo) {
        if (timeFrom.equals("09:00") && timeTo.equals("17:00")) {
            radio9to5.setChecked(true);
        } else {
            radioCustomTimes.setChecked(true);
        }
        textViewCustomTimeFrom.setText(timeFrom);
        textViewCustomTimeTo.setText(timeTo);
        reminder.setTimes(timeFrom, timeTo);
    }

    private void updateDays(String days) {
        if (days.equals("M-F")) {
            radioMF.setChecked(true);
            textViewCustomDays.setText("Monday - Friday");
        } else {
            radioCustomDays.setChecked(true);
            textViewCustomDays.setText(days);
        }
    }

    public void assignObjects() {
        etReminder = (EditText) findViewById(R.id.editTextReminder);
        textViewFrequency = (TextView) findViewById(R.id.editTextFrequency);
        textViewFrequencyHeader = (TextView) findViewById(R.id.textViewFrequencyHeader);
        textViewCustomDays = (TextView) findViewById(R.id.textViewCustomDays);
        textViewSelectTimesHeader = (TextView) findViewById(R.id.textViewSelectTimesHeader);
        textViewCustomTimeFrom = (TextView) findViewById(R.id.textViewCustomTimeFrom);
        textViewCustomTimeTo = (TextView) findViewById(R.id.textViewCustomTimeTo);
        radio9to5 = (RadioButton) findViewById(R.id.radio9To5);
        radioCustomTimes = (RadioButton) findViewById(R.id.radioCustomTimes);
        radioCustomDays = (RadioButton) findViewById(R.id.radioCustomDays);
        radioMF = (RadioButton) findViewById(R.id.radioMF);
        radioRecurring = (RadioButton) findViewById(R.id.radioRecurring);
        radioSingleUse = (RadioButton) findViewById(R.id.radioSingleUse);
        radioAlarm = (RadioButton) findViewById(R.id.radioAlarm);
        radioNotification = (RadioButton) findViewById(R.id.radioNotification);
        spinnerSelectAlarm = (Spinner) findViewById(R.id.spinnerSelectAlarm);
    }

    //load in data to edit from the Reminder sent to this activity
    private void loadEditData(Intent intent) {
        String days;
        editId = reminder.getRowId();

        etReminder.setText(reminder.getReminder());
        textViewFrequency.setText(reminder.getFrequency());
        textViewFrequency.requestFocus();
        npMinute = reminder.getFrequencyMinutes();

        days = reminder.getDaysAsString();
        updateDays(days);

        if (reminder.getRecurring()) {
            radioRecurring.setChecked(true);
            //setTimeRangeVisibility(true);
        } else {
            radioSingleUse.setChecked(true);
            //setTimeRangeVisibility(false);
        }
        updateTimes(reminder.getTimeFromAsString(), reminder.getTimeToAsString());

        if (reminder.getNotificationType()) {
            radioNotification.setChecked(true);
        } else {
            radioAlarm.setChecked(true);
        }

        //end of loadEditData
    }

    //return Cancelled code if cancel button selected
    public void goCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    //save information to database if save button selected
    public void goSaveReminder(View view) {
        reminder.setReminder(etReminder.getText().toString());
        reminder.setFrequency(textViewFrequency.getText().toString());
        //times are updated in updateTimes method
        //days are updated EditDaysActivity or in goSelectDays
        reminder.setMisc(radioRecurring.isChecked(),radioNotification.isChecked(),
                spinnerSelectAlarm.getSelectedItemPosition());

        Intent data = getIntent();
        data.putExtra("reminder",reminder);
        setResult(RESULT_OK, data);
        finish();

        //end of goSaveReminder
    }

    //todo allow entry of hours, minutes, seconds in frequency field, can set up three textbox layout, also could use timepicker widget


    public void goSelectTimes(View view) {
        switch (view.getId()) {
            case R.id.radioCustomTimes:

                //todo do something to show difference between dialogs, popup text or title change?
                //todo set up formatting for am/pm
                //todo enhance design of from and to in layout
                //todo set time picker into custom dialog?
                //todo validate time from not being later than time to
                showTimePickerDialog(tpdHourFrom, tpdMinuteFrom, true, timeSetListener);
                showTimePickerDialog(tpdHourTo, tpdMinuteTo, true, timeSetListener);
                break;

            case R.id.radio9To5:
                tpdHourFrom = 9;
                tpdMinuteFrom = 0;
                tpdHourTo = 17;
                tpdMinuteTo = 0;

                updateTimes("09:00", "17:00");
                break;
        }
    }

    public void goSelectDays(View view) {
        switch (view.getId()) {
            case R.id.radioMF:
                updateDays("M-F");
                reminder.setDays(true, true, true, true, true, false, false);
                break;
            case R.id.radioCustomDays:
                Intent intent = new Intent(this, EditDaysActivity.class);
                intent.putExtra("reminder", reminder);
                startActivityForResult(intent, REQUEST_EDIT);
                //showDaysDialog();
                break;
        }
    }

    private void showDaysDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Select Days");
        dialog.setContentView(R.layout.activity_select_days);
        Button buttonOK = (Button) dialog.findViewById(R.id.buttonDaysOK);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonDaysCancel);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        //update reminder and textview with new values
        switch (numberPicker.getId()) {
            case R.id.numberPickerMinute:
                npMinute = newValue;
                break;
        }
    }

    public void goSelectFrequency(View view) {
        //todo set up case for selecting single time or repeating frequency
        //todo could use timepicker for single and number picker for recurring
        //depends on Usage selection, if single use open up a time selector, if recurring enter frequency
        showNumberPickerDialog();
    }

    private void showNumberPickerDialog() {
        final Dialog dialog = new Dialog(EditActivity.this);
        dialog.setTitle("Select Time");
        dialog.setContentView(R.layout.dialog_frequency_layout);
        Button buttonOK = (Button) dialog.findViewById(R.id.buttonFreqOK);
        Button buttonCancel = (Button) dialog.findViewById(R.id.buttonFreqCancel);
        final NumberPicker numberPickerMinute = (NumberPicker) dialog.findViewById(R.id.numberPickerMinute);
        numberPickerMinute.setMaxValue(720);
        numberPickerMinute.setWrapSelectorWheel(false);
        numberPickerMinute.setOnValueChangedListener(this);
        numberPickerMinute.setValue(npMinute);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String freq = String.valueOf(npMinute);
/*                if (npMinute == 1) {
                    freq = freq + " minute";
                } else {
                    freq = freq + " minutes"; //todo if freq is large changes to multiple lines
                } */
                textViewFrequency.setText(freq);
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void goSelectUsage(View view) {
        setUsage();
    }

    private void setUsage() {
        if (radioSingleUse.isChecked()) {
            //setTimeRangeVisibility(false);
            textViewFrequencyHeader.setText("Notification Time");
        } else {
            //setTimeRangeVisibility(true);
            textViewFrequencyHeader.setText("Notification Frequency");
        }

    }

    public void goSelectMsgType(View view) {
        listAdapter.clear();
        switch (view.getId()) {
            //populate spinner with Notification types list
            case R.id.radioNotification:
                listAdapter.addAll(notifications);
                break;
            //populate spinner with Alarm sounds list
            case R.id.radioAlarm:
                listAdapter.addAll(alarms);
                break;
        }
        listAdapter.notifyDataSetChanged();
    }


    private void setTimeRangeVisibility(boolean visible) {
        //todo try again to dynamically set margins
        //ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) textViewSelectDaysHeader.getLayoutParams();

        int visibility;
        if (visible) {
            visibility = View.VISIBLE;
            //marginLayoutParams.topMargin = 220;
        } else {
            visibility = View.GONE;
            //marginLayoutParams.topMargin = 100;
        }
        //textViewSelectDaysHeader.setLayoutParams(marginLayoutParams);

        textViewSelectTimesHeader.setVisibility(visibility);
        textViewCustomTimeFrom.setVisibility(visibility);
        textViewCustomTimeTo.setVisibility(visibility);
        radio9to5.setVisibility(visibility);
        radioCustomTimes.setVisibility(visibility);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,this.getIntent());

        if (resultCode == Activity.RESULT_OK) {
            reminder = data.getParcelableExtra("reminder");
            updateDays(reminder.getDaysAsString());
        }
    }




    //end of Edit Activity
}
