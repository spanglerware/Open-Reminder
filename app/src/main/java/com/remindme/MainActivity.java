package com.remindme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;

    private DatabaseUtil myDb;

    private boolean bStarted;
    private Button btnStart;
    private Spinner spinner;

    ArrayList<Reminder> dataArray;
    MyAdapter myAdapter;
    Runnable timerRunnable;
    float xDown, xUp, yDown, yUp;

    private String spinnerReminder;
    private long spinnerDbId;
    private int spinnerRow;

    private ArrayList<MyAlarm> alarmArray;
    private int alarmCounter;

    //TODO display an alarm/notification screen/dialog if message fires while app is in background, currently stays in background

    //todo fix spinner list position for main screen

    //todo for different screen sizes try using center_horizontal layout for central widgets

    //todo add motion to spinner, right to delete, hold to edit, left to insert new; add animate for fx?

    //todo clean up and comment code in main

    //todo study android calendar set reminders and utilize style and layout

    //todo for a simple timer allow the user to tap frequency timer and select a new time

    //todo can use Timer object to run notifications or alarms

    //todo change main textview to a listview of all active reminders

    //todo add random reminders using Notification CATEGORY_RECOMMENDATION

    //todo be able to select specific time for alarm on selected days

    //todo future version may implement expandable listview for grouping of reminders

    //todo redesign database with more tables to make more oo?

    //todo set layouts to scrollable or change property so landscape is disabled

    //todo add onTouch to main, swipe right/left for new/edit

    //todo add voice support

    //todo remove cancel buttons and save any changes made immediately? currently more common to do so


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main_alt);

        openDB();

        bStarted = false;
        btnStart = (Button) findViewById(R.id.buttonStart);

        fillDataArray();
        addItemsToSpinner();
        registerSpinnerSelectionEvent();
        registerSpinnerOnTouchEvent();
        callHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //todo need to store time values?
        Log.v("onPause", "Main fired onPause");
        //from documentation: When an activity's onPause() method is called, it should commit to the backing content provider or file any changes the user has made.
    }

    protected void onStop() {
        super.onStop();
        //todo this method called when activity no longer visible, may need to store data on this call
        Log.v("onStop", "Main fired onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //todo sync active timers with pendingIntents?
        Log.v("onResume", "Main fired onResume");

        updateReminders();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //todo restart kills all the active timers?
        Log.v("onRestart", "Main fired onRestart");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //todo ensure all PendingIntents are canceled?
        closeDb();
        Log.v("onDestroy", "Main fired onDestroy");
    }

    //this handler sets up a countdown timer for the reminders in the spinner
    private void callHandler() {
        final Handler timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                myAdapter.notifyDataSetChanged();
                timerHandler.postDelayed(this, 1000); //run every second
                if (myAdapter.reduceCounters(1000)) {
                    setButtonImage(true);
                }
            }
        };
        timerRunnable.run();
    }

    private void openDB() {
        myDb = new DatabaseUtil(this);
        myDb.open();
    }

    private void closeDb() {
        myDb.close();
    }

    //this method writes the reminder information from the database to the Reminder class array
    private void fillDataArray() {
        long rowId;
        String frequency;
        String reminder;
        long timeFrom;
        long timeTo;
        boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
        boolean reminderUseType;
        boolean notificationType;
        int messageId;
        Time time;

        Cursor cursor = myDb.getAllRows();
        int count = cursor.getCount();
        dataArray = new ArrayList<Reminder>();

        //set up last selected row
        spinnerDbId = myDb.getCurrentRowId();

        //transfer data from the cursor adapter to the custom adapter
        if (count > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < count; i++) {
                rowId = cursor.getLong(DatabaseUtil.COLUMN_ROWID);
                frequency = cursor.getString(DatabaseUtil.COLUMN_FREQUENCY);
                reminder = cursor.getString(DatabaseUtil.COLUMN_REMINDER);
                if (rowId == spinnerDbId) { spinnerRow = i + 1; spinnerReminder = reminder; }

                time = Time.valueOf(cursor.getString(DatabaseUtil.COLUMN_TIME_FROM));
                timeFrom = time.getTime();
                time = Time.valueOf(cursor.getString(DatabaseUtil.COLUMN_TIME_TO));
                timeTo = time.getTime();

                monday = cursor.getInt(DatabaseUtil.COLUMN_MONDAY) > 0;
                tuesday = cursor.getInt(DatabaseUtil.COLUMN_TUESDAY) > 0;
                wednesday = cursor.getInt(DatabaseUtil.COLUMN_WEDNESDAY) > 0;
                thursday = cursor.getInt(DatabaseUtil.COLUMN_THURSDAY) > 0;
                friday = cursor.getInt(DatabaseUtil.COLUMN_FRIDAY) > 0;
                saturday = cursor.getInt(DatabaseUtil.COLUMN_SATURDAY) > 0;
                sunday = cursor.getInt(DatabaseUtil.COLUMN_SUNDAY) > 0;

                reminderUseType = cursor.getInt(DatabaseUtil.COLUMN_RECURRING) > 0;
                notificationType = cursor.getInt(DatabaseUtil.COLUMN_NOTIFICATION_TYPE) > 0;
                messageId = cursor.getInt(DatabaseUtil.COLUMN_MESSAGE);

                Reminder item = new Reminder(reminder, frequency, rowId);
                item.setDays(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
                item.setTimes(timeFrom, timeTo);
                item.setMisc(reminderUseType, notificationType, messageId);
                item.setMessage(getBaseContext(), i);
                dataArray.add(i, item);

                cursor.moveToNext();
            }
        }
        //set up the custom adapter
        myAdapter = new MyAdapter(this,dataArray);
    }

    private void addItemsToSpinner() {
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(myAdapter);
        spinner.setSelection(spinnerRow);
    }

    private void updateReminders() {
        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                reminder.updateCounter();
                myAdapter.reduceCounters(0);
                if (!reminder.isActive()) {
                    setButtonImage(true);
                } else {
                    setButtonImage(false);
                }
            }
        }
    }

    private void displayToast(long id){
        Cursor cursor = myDb.getRow(id);
        if (cursor.moveToFirst()){
            long idDb = cursor.getLong(DatabaseUtil.COLUMN_ROWID);
            String reminder = cursor.getString(DatabaseUtil.COLUMN_REMINDER);
            String frequency = cursor.getString(DatabaseUtil.COLUMN_FREQUENCY);

            String message = "ID: " + idDb + "\n" + "Reminder: " + reminder + "\n" + "Frequency: " + frequency;
            Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
        }
        cursor.close();
    }

    //send a Reminder to the Edit Activity
    public void goEditReminder(View view) {
        //todo may need to stop an active reminder before sending to edit
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("reminder",dataArray.get(spinnerRow));
        startActivityForResult(intent, REQUEST_EDIT);
    }

    //create a new blank reminder and send it to Edit Activity
    public void goNewReminder(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        long rowId = myDb.createNewEntry();

        Reminder reminder = new Reminder("","0",rowId);
        reminder.setDays(false,false,false,false,false,false,false);
        reminder.setTimes("09:00", "17:00");
        reminder.setMisc(true, true, 0);

        intent.putExtra("reminder", reminder);
        startActivityForResult(intent, REQUEST_NEW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,this.getIntent());

        if (resultCode == Activity.RESULT_OK) {
            Reminder reminder = data.getParcelableExtra("reminder");
            switch (requestCode) {
                case (REQUEST_NEW): {
                        //result is from successful insert of new reminder
                        reminder.setMessage(getBaseContext(), dataArray.size() + 1);
                        dataArray.add(reminder);
                    break;
                }
                case (REQUEST_EDIT): {
                        //result is from successful edit
                        reminder.setMessage(getBaseContext(), spinnerRow);
                        dataArray.set(spinnerRow, reminder);
                    break;
                }
            }
            myDb.updateRow(reminder);
        }
    }

    public void goStartReminder(View view) {
        int position = spinner.getSelectedItemPosition();
        Reminder reminder = dataArray.get(position);

        if (bStarted) {
            bStarted = false;
            reminder.setActive(false);
            reminder.cancelAlarm();
        } else {
            bStarted = true;
            reminder.setActive(true);
            //startAlarm((int) item.getLongFrequency());
            reminder.startAlarm();
        }
        setButtonImage(!bStarted);
        myAdapter.notifyDataSetChanged();

        //todo wait does not work, need to capture current time, store it, cancel button, then restart with stored value
    }

    private void setButtonImage(boolean toStart) {
        if (toStart) {
            btnStart.setText("Start");
            btnStart.setTextColor(Color.parseColor("#009400"));
            btnStart.setBackgroundResource(R.drawable.button_start);
        } else {
            btnStart.setText("Stop");
            btnStart.setTextColor(Color.RED);
            btnStart.setBackgroundResource(R.drawable.button_stop);
        }
    }

    private void registerSpinnerSelectionEvent() {
        xDown = 0;
        xUp = 0;
        yDown = 0;
        yUp = 0;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View viewSelected, int position, long idInDb) {
                Reminder item = dataArray.get(position);
                spinnerReminder = item.getReminder();
                spinnerDbId = item.getRowId();
                spinnerRow = position;

                if (item.isActive()){
                    bStarted = true;
                    setButtonImage(false);
                } else {
                    bStarted = false;
                    setButtonImage(true);
                }

                //todo still need current field? if so need to update as below
                myDb.changeCurrent(spinnerDbId);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //no updates needed
            }
        });

    }
    //todo set up spinner onTouchEvent method (left for delete and right for insert) and longClick for edit

    //todo separate onTouch and onItemClick events, right now both are firing, override onClick, may need to set up custom spinner and override forwarding listener

    private void registerSpinnerOnTouchEvent() {
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                String msg;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    {
                        xDown = event.getX();
                        yDown = event.getY();
                    }
                    case MotionEvent.ACTION_UP: {
                        xUp = event.getX();
                        yUp = event.getY();

                        //use a minimum swipe distance before activating to distinguish between taps and swipes
                        if (xUp - xDown > 100) {
                            //msg = "right";
                            //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                            goEditReminder(view);
                        } else if (xDown - xUp > 100) {
                            //msg = "left";
                            //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                            createDeleteDialog();
                        }
                        break;
                    }
                }
                return false;
            }
        });
    }

    private void registerSpinnerOnLongClickEvent() {
        spinner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //call edit activity, send intent data with db rowId, activityforresult
                return true;
            }
        });
    }

    private void goDelete() {
        TextView textView = (TextView) findViewById(R.id.textViewItemNumber);
        long id = Long.parseLong(textView.getText().toString());
        //todo set up alertdialog
        myDb.deleteRow(id);
        myAdapter.removeItem(spinner.getSelectedItemPosition());
        myAdapter.notifyDataSetChanged();
    }

    private void createDeleteDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Confirm Delete")
                .setMessage("Would you like to delete the reminder: " + spinnerReminder)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        goDelete();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //end of Main
}
