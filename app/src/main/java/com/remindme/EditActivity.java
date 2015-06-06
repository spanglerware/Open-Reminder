package com.remindme;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private Spinner spinnerSelectAlarm;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    private Button buttonMonday;
    private Button buttonTuesday;
    private Button buttonWednesday;
    private Button buttonThursday;
    private Button buttonFriday;
    private Button buttonSaturday;
    private Button buttonSunday;
    private RangeSeekBar<Float> rangeBar;

    private ArrayList<String> alarms;
    private ArrayList<Integer> alarmsResId;
    private ArrayList<String> notifications;

    private int npMinute;
    private int dialogFlag;

    private long editId;
    private boolean editReminder;
    private Reminder mReminder;
    private TextView tvTimeMin;
    private TextView tvTimeMax;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_edit_new);

        initDayButtons();

        rangeBar = new RangeSeekBar<Float>(this);
        rangeBar.setRangeValues(0.0f, 24.0f);
        rangeBar.setSelectedMinValue(9.0f);
        rangeBar.setSelectedMaxValue(17.0f);
        rangeBar.setPadding(10,0,10,0);

        tvTimeMax = (TextView) findViewById(R.id.time_range_max);
        tvTimeMin = (TextView) findViewById(R.id.time_range_min);

        rangeBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Float>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Float minValue, Float maxValue) {
                int timeMinHours = (int) Math.floor(minValue);
                int timeMaxHours = (int) Math.floor(maxValue);
                int timeMinMinutes = (int) ((minValue - Math.floor(minValue)) * 60.0f);
                int timeMaxMinutes = (int) ((maxValue - Math.floor(maxValue)) * 60.0f);

                tvTimeMin.setText("From: " + String.valueOf(timeMinHours) + ":" + String.format("%02d", timeMinMinutes));
                tvTimeMax.setText("To: " + String.valueOf(timeMaxHours) + ":" + String.format("%02d", timeMaxMinutes));
                // handle changed range values
//                Log.i(TAG, "User selected new date range: MIN=" + new Date(minValue) + ", MAX=" + new Date(maxValue));
            }
        });

        ViewGroup container = (ViewGroup) findViewById(R.id.edit_container);
        container.addView(rangeBar);

        //set local variables to interface objects
        //assignObjects();

        //load interface with data in case of an edit
        Intent intent = getIntent();
        mReminder = intent.getParcelableExtra("reminder");

        if (mReminder != null) { //todo this should now always be true
            editReminder = true;
            loadEditData();
        } else {
            editReminder = false;
        }

        //loadNotificationList();
        //loadAlarmList();

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
//
//    private TimePickerDialog showTimePickerDialog(int hour, int minutes, boolean militaryTime, TimePickerDialog.OnTimeSetListener onTimeSetListener) {
//        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minutes, militaryTime);
//        timePickerDialog.show();
//        return timePickerDialog;
//    }


    public void assignObjects() {
        etReminder = (EditText) findViewById(R.id.edittext_reminder);
        textViewFrequency = (TextView) findViewById(R.id.edittext_frequency);
//        spinnerSelectAlarm = (Spinner) findViewById(R.id.spinnerSelectAlarm);
    }

    //load in data to edit from the Reminder sent to this activity
    private void loadEditData() {
        boolean[] days = mReminder.getDays();
        editId = mReminder.getRowId();

        etReminder.setText(mReminder.getReminder());
        textViewFrequency.setText(mReminder.getFrequency());
        textViewFrequency.requestFocus();
        npMinute = mReminder.getFrequencyMinutes();

        monday = days[0];
        if (monday) { buttonMonday.setBackgroundResource(R.drawable.border_style_button_on); }
        tuesday = days[1];
        if (tuesday) { buttonTuesday.setBackgroundResource(R.drawable.border_style_button_on); }
        wednesday = days[2];
        if (wednesday) { buttonWednesday.setBackgroundResource(R.drawable.border_style_button_on); }
        thursday = days[3];
        if (thursday) { buttonThursday.setBackgroundResource(R.drawable.border_style_button_on); }
        friday = days[4];
        if (friday) { buttonFriday.setBackgroundResource(R.drawable.border_style_button_on); }
        saturday = days[5];
        if (saturday) { buttonSaturday.setBackgroundResource(R.drawable.border_style_button_on); }
        sunday = days[6];
        if (sunday) { buttonSunday.setBackgroundResource(R.drawable.border_style_button_on); }

        if (mReminder.getRecurring()) {
            //setTimeRangeVisibility(true);
        } else {
            //setTimeRangeVisibility(false);
        }

        //todo add range bar min and max selections
        

        //end of loadEditData
    }

    //save information to database if save button selected
    public void goSaveReminder(View view) {
        mReminder.setReminder(etReminder.getText().toString());
        mReminder.setFrequency(textViewFrequency.getText().toString());
        //times are updated in updateTimes method
        //days are updated EditDaysActivity or in goSelectDays
        //reminder.setMisc(radioRecurring.isChecked(),radioNotification.isChecked(),
//        spinnerSelectAlarm.getSelectedItemPosition());

        Intent data = getIntent();
        data.putExtra("reminder", mReminder);
        setResult(RESULT_OK, data);
        finish();

        //end of goSaveReminder
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

    private void initDayButtons() {
        monday = false;
        tuesday = false;
        wednesday = false;
        thursday = false;
        friday = false;
        saturday = false;
        sunday = false;

        buttonMonday = (Button) findViewById(R.id.toggle_monday);
        buttonTuesday = (Button) findViewById(R.id.toggle_tuesday);
        buttonWednesday = (Button) findViewById(R.id.toggle_wednesday);
        buttonThursday = (Button) findViewById(R.id.toggle_thursday);
        buttonFriday = (Button) findViewById(R.id.toggle_friday);
        buttonSaturday = (Button) findViewById(R.id.toggle_saturday);
        buttonSunday = (Button) findViewById(R.id.toggle_sunday);
    }

    public void goToggleMonday(View view) {
        if (monday) {
            buttonMonday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonMonday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        monday = !monday;
    }

    public void goToggleTuesday(View view) {
        if (tuesday) {
            buttonTuesday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonTuesday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        tuesday = !tuesday;
    }

    public void goToggleWednesday(View view) {
        if (wednesday) {
            buttonWednesday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonWednesday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        wednesday = !wednesday;
    }

    public void goToggleThursday(View view) {
        if (thursday) {
            buttonThursday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonThursday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        thursday = !thursday;
    }

    public void goToggleFriday(View view) {
        if (friday) {
            buttonFriday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonFriday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        friday = !friday;
    }

    public void goToggleSaturday(View view) {
        if (saturday) {
            buttonSaturday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonSaturday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        saturday = !saturday;
    }

    public void goToggleSunday(View view) {
        if (sunday) {
            buttonSunday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonSunday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        sunday = !sunday;
    }


    //end of Edit Activity
}
