package com.remindme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by Scott on 4/18/2015.
 */

public class EditActivity extends Activity implements NumberPicker.OnValueChangeListener {
    private EditText etReminder;
    private TextView textViewFrequency;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    private boolean useType;  //false for single time, true for repeating

    private Button buttonMonday;
    private Button buttonTuesday;
    private Button buttonWednesday;
    private Button buttonThursday;
    private Button buttonFriday;
    private Button buttonSaturday;
    private Button buttonSunday;
    private RangeSeekBar<Float> rangeBar;
    private TextView rangebarLabel;
    private RadioButton radioButtonSingle;
    private RadioButton radioButtonRepeat;

    private int npHour;
    private int npMinute;
    private int npSecond;
    private float minTime;
    private float maxTime;

    private boolean editReminder;  //false means new reminder, true means edited reminder
    private Reminder mReminder;
    private int mListPosition;

    private final String EDIT_KEY = "editing";
    private final String POSITION_KEY = "listPosition";

    //todo handle screen rotation during edit, currently saves and adds new
    //todo      screen rotate keeps reminder text but not frequency

    //todo need to update values on selection instead of at onPause?

    //todo DELETE blank reminders

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_edit_new);

        initDayButtons();
        assignObjects();

        if (bundle != null) {
            //load saved instance state
            mListPosition = bundle.getInt(POSITION_KEY);
            editReminder = bundle.getBoolean(EDIT_KEY);
        } else {
            //load interface with data in case of an edit
            Intent intent = getIntent();
            editReminder = intent.getBooleanExtra("editType", false);
            mListPosition = intent.getIntExtra("arrayId", -1);
        }

        if (mListPosition < 0) { finish(); }

        if (editReminder) {
            mReminder = SingletonDataArray.getInstance().getDataArray().get(mListPosition);
            loadEditData();
        } else {
            createNewReminder(mListPosition);
        }

        minTime = mReminder.getTimeFrom();
        maxTime = mReminder.getTimeTo();

        //todo move to assignObjects method, use stored values
        rangeBar = new RangeSeekBar<Float>(this);
        rangeBar.setRangeValues(0.0f, 24.0f);
        rangeBar.setSelectedMinValue(9.0f);
        rangeBar.setSelectedMaxValue(17.0f);
        rangeBar.setPadding(10, 0, 10, 0);

        rangeBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Float>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Float minValue, Float maxValue) {
                minTime = minValue;
                maxTime = maxValue;
            }
        });

        ViewGroup container = (ViewGroup) findViewById(R.id.edit_container);
        container.addView(rangeBar);

        rangeBar.setSelectedMinValue(minTime);
        rangeBar.setSelectedMaxValue(maxTime);

        setUse();
        textViewFrequency.requestFocus();

    }  //end of 0nCreate

    @Override
    public void onPause() {
        super.onPause();
        mReminder.updateValues(etReminder.getText().toString(), minTime, maxTime, monday, tuesday, wednesday, thursday,
                friday, saturday, sunday, useType, false, 0, false, 0);
        mReminder.editUpdate(editReminder, mListPosition);

        //todo if reminder blank may either assign "blank reminder" value or show dialog asking to save or delete
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //set edit flag to true to prevent creating more entries in database
        editReminder = true;

        savedInstanceState.putInt(POSITION_KEY, mListPosition);
        savedInstanceState.putBoolean(EDIT_KEY, editReminder);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void assignObjects() {
        etReminder = (EditText) findViewById(R.id.edittext_reminder);
        textViewFrequency = (TextView) findViewById(R.id.edittext_frequency);
        rangebarLabel = (TextView) findViewById(R.id.rangebar_label);
        radioButtonSingle = (RadioButton) findViewById(R.id.radioButton_single_time);
        radioButtonRepeat = (RadioButton) findViewById(R.id.radioButton_repeat);
    }

    //load in data to edit from the Reminder sent to this activity
    private void loadEditData() {
        int frequency;
        boolean[] days = mReminder.getDays();

        etReminder.setText(mReminder.getReminder());
        useType = mReminder.getRecurring();

        String label;
        if (useType) {
            label = "Interval: ";
            radioButtonRepeat.setChecked(true);
        } else {
            label = "Time: ";
            radioButtonSingle.setChecked(true);
        }
        textViewFrequency.setText(label + mReminder.getFormattedFrequency());

        frequency = TimeUtil.FloatTimeToMilliseconds(mReminder.getFloatFrequency());
        npHour = frequency / (60 * 60 * 1000);
        if (npHour > 0) { frequency -= npHour * 60 * 60 * 1000; }
        npMinute = frequency / (60 * 1000);
        if (npMinute > 0) { frequency -= npMinute * 60 * 1000; }
        npSecond = frequency / 1000;
        Log.v("Edit Activity", "Hour: " + npHour + ", Minute: " + npMinute + ", Second: " + npSecond);

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

    } //end of loadEditData

    private void createNewReminder(int arrayId) {
        //create a new record in the database with default values
        //todo may want to change default values for reminder, frequency, and days
        DatabaseUtil db = new DatabaseUtil();
        db.open();
        long rowId = db.insertRow("", "0", 9.0f, 17.0f, false, false, false, false,
                false, false, false, false, false, 0, false, 0);
        mReminder = new Reminder(arrayId, "", "0", rowId, 9.0f, 17.0f, false, false, false,
                false, false, false, false, false, false, 0, false, 0);
        radioButtonSingle.setChecked(true);

        db.close();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        //update reminder and textview with new values
        switch (numberPicker.getId()) {
            case R.id.number_picker_hours:
                npHour = newValue;
                break;
            case R.id.number_picker_minutes:
                npMinute = newValue;
                break;
            case R.id.number_picker_seconds:
                npSecond = newValue;
                break;
        }
    }

    public void goSelectFrequency(View view) {
        showNumberPickerDialog();
    }

    private void showNumberPickerDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_select_time, null);
        String label;
        if (useType) {
            label = "Select Time Interval: ";
        } else {
            label = "Select Time: ";
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(EditActivity.this);
        dialog.setTitle(label);
        final NumberPicker numberPickerHours = (NumberPicker) dialogView.findViewById(
                R.id.number_picker_hours);
        final NumberPicker numberPickerMinutes = (NumberPicker) dialogView.findViewById(
                R.id.number_picker_minutes);
        final NumberPicker numberPickerSeconds = (NumberPicker) dialogView.findViewById(
                R.id.number_picker_seconds);
        numberPickerHours.setMaxValue(23);
        numberPickerMinutes.setMaxValue(59);
        numberPickerSeconds.setMaxValue(59);
        numberPickerHours.setWrapSelectorWheel(true);
        numberPickerMinutes.setWrapSelectorWheel(true);
        numberPickerSeconds.setWrapSelectorWheel(true);
        numberPickerHours.setOnValueChangedListener(this);
        numberPickerMinutes.setOnValueChangedListener(this);
        numberPickerSeconds.setOnValueChangedListener(this);
        numberPickerHours.setValue(npHour);
        numberPickerMinutes.setValue(npMinute);
        numberPickerSeconds.setValue(npSecond);

        dialog.setView(dialogView);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String hours = "";
                String label;
                if (useType) {
                    label = "Interval: ";
                } else {
                    label = "Time: ";
                }
                int frequency = (npHour * 60 * 60 + npMinute * 60 + npSecond) * 1000;
                mReminder.setFloatFrequency(TimeUtil.MillisecondsToFloatTime(frequency));
                if (npHour != 0) { hours = String.valueOf(npHour) + ":"; }
                textViewFrequency.setText(label + hours + String.format("%02d:%02d",
                        npMinute, npSecond));
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
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
        hideSoftKeyboard();
    }

    public void goToggleTuesday(View view) {
        if (tuesday) {
            buttonTuesday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonTuesday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        tuesday = !tuesday;
        hideSoftKeyboard();
    }

    public void goToggleWednesday(View view) {
        if (wednesday) {
            buttonWednesday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonWednesday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        wednesday = !wednesday;
        hideSoftKeyboard();
    }

    public void goToggleThursday(View view) {
        if (thursday) {
            buttonThursday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonThursday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        thursday = !thursday;
        hideSoftKeyboard();
    }

    public void goToggleFriday(View view) {
        if (friday) {
            buttonFriday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonFriday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        friday = !friday;
        hideSoftKeyboard();
    }

    public void goToggleSaturday(View view) {
        if (saturday) {
            buttonSaturday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonSaturday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        saturday = !saturday;
        hideSoftKeyboard();
    }

    public void goToggleSunday(View view) {
        if (sunday) {
            buttonSunday.setBackgroundResource(R.drawable.border_style);
        } else {
            buttonSunday.setBackgroundResource(R.drawable.border_style_button_on);
        }
        sunday = !sunday;
        hideSoftKeyboard();
    }

    public void goSelectUse(View view) {
        textViewFrequency.setText("");
        switch (view.getId()) {
            case R.id.radioButton_single_time:
                useType = false;
                break;
            case R.id.radioButton_repeat:
                useType = true;
                break;
        }
        setUse();
        hideSoftKeyboard();
    }

    public void setUse() {
        if (useType) {
            textViewFrequency.setHint("Select Time Interval");
            rangebarLabel.setVisibility(View.VISIBLE);
            rangeBar.setVisibility(View.VISIBLE);
            radioButtonRepeat.setChecked(true);
        } else {
            textViewFrequency.setHint("Select Time");
            rangebarLabel.setVisibility(View.GONE);
            rangeBar.setVisibility(View.GONE);
            radioButtonSingle.setChecked(true);
        }
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etReminder.getWindowToken(), 0);
        }
    }


    //end of Edit Activity
}
