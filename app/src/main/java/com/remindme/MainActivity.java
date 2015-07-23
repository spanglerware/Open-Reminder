package com.remindme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements ReminderCallbacks {

    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;
    private static final int RUNNABLE_INTERVAL = 1000;  //runnable set to 1 second delay

    private DatabaseUtil myDb;

    private boolean bSelected;
    private boolean edited = false;

    public ListView spinner;
    private MyAdapter myAdapter;
    private Runnable timerRunnable;
    private Handler timerHandler;
    float xDown, xUp, yDown, yUp;

    private long spinnerDbId;
    private int spinnerRow;

    private static MainActivity instance;


    //todo BUG alarmTime not writing correctly to db?

    //todo BUG if device turned off, countdown timers continue but alarm was cancelled
        //todo solution? overwrite all active timers with another alarm?

    //todo BUG float rounding causing time display to be sometimes off a second

    //todo REMOVE initial entries?

    //todo need to implement onSaveInstanceState

    //todo add color to UI

    //todo remove log statements for release

    //todo clean up and comment code in main


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        Log.v("onPause", "Main fired onCreate");

        openDB();

        bSelected = false;
        spinnerRow = -1;

        fillDataArray();
        addItemsToSpinner();
        registerSpinnerOnTouchEvent();
        callHandler();

        spinner.setItemsCanFocus(true);
        spinner.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewVisibilities(position);
            }
        });

        IntentFilter filter = new IntentFilter(AlarmService.ACTION);
        restartAlarms();

        //end of MainActivity onCreate
    }

    private void setViewVisibilities(int position) {
        int selected = spinnerRow;
        Reminder reminder = myAdapter.getItem(position);
        if (reminder.getOpened()) {
            if (selected == -1) {
                closeAllReminders();
            } else if (selected != position) {
                Reminder previous = myAdapter.getItem(selected);
                previous.toggleVisibility();
            }
        }
        reminder.toggleVisibility();
        spinnerRow = position;

        myAdapter.notifyDataSetChanged();
        spinner.setSelection(position);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("onPause", "Main fired onPause");
        //from documentation: When an activity's onPause() method is called, it should commit to the backing content provider or file any changes the user has made.

        //todo write data from singleton to database here?
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("onStop", "Main fired onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("onResume", "Main fired onResume");
        //dataArray = SingletonDataArray.getInstance().getDataArray();
        updateReminders();
        if (spinnerRow >= 0 && edited) {
            Reminder reminder = myAdapter.getItem(spinnerRow);
            reminder.visibility = View.VISIBLE;
            spinner.setSelection(spinnerRow);
            edited = false;
        }
        myAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v("onRestart", "Main fired onRestart");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        closeDb();
        timerHandler.removeCallbacks(timerRunnable);
        Log.v("onDestroy", "Main fired onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("editType", false);
            intent.putExtra("arrayId", myAdapter.getCount());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startReminderCallBack(int position) {
        Reminder reminder = myAdapter.getItem(position);
        reminder.setActive(true);
        startReminder(position);
    }

    @Override
    public void deleteReminderCallBack(int position) {
        createDeleteDialog(position);
    }

    @Override
    public void editReminderCallBack(int position) {
        goEdit(position);
    }

    @Override
    public void cancelReminderCallBack(int position) {
        cancelReminder(position);
    }

    @Override
    public void notificationCallBack(String reminderText) {
        displayToast(reminderText);
    }

    //this handler sets up a countdown timer for the reminders in the spinner
    private void callHandler() {
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int interval = RUNNABLE_INTERVAL;
                timerHandler.postDelayed(this, interval); //run every second

                int arraySize = myAdapter.getCount();
                for (int i = 0; i < arraySize; i++) {
                    Reminder reminder = myAdapter.getItem(i);
                    if (reminder.isActive()) {
                        reminder.reduceCounter(interval);
                        myAdapter.notifyDataSetChanged();
                    }
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
        float timeFrom;
        float timeTo;
        boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
        boolean reminderUseType;
        boolean notificationType;
        int messageId;
        boolean active;
        long alarmTime;

        //set up the custom adapter
        spinner = (ListView) findViewById(R.id.listview_reminder);
        myAdapter = new MyAdapter(getApplicationContext(), spinner, this);

        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        if (!dataArray.isEmpty()) {
            return;
        }

        Cursor cursor = myDb.getAllRows();
        int count = cursor.getCount();

        //set up last selected row
        spinnerDbId = myDb.getCurrentRowId();

        //transfer data from the cursor adapter to the custom adapter
        if (count > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < count; i++) {
                rowId = cursor.getLong(DatabaseUtil.COLUMN_ROWID);
                frequency = cursor.getString(DatabaseUtil.COLUMN_FREQUENCY);
                reminder = cursor.getString(DatabaseUtil.COLUMN_REMINDER);
                if (rowId == spinnerDbId) { spinnerRow = i + 1; }

                timeFrom = cursor.getFloat(DatabaseUtil.COLUMN_TIME_FROM);
                timeTo = cursor.getFloat(DatabaseUtil.COLUMN_TIME_TO);

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
                active = cursor.getInt(DatabaseUtil.COLUMN_ACTIVE) > 0;
                alarmTime = cursor.getLong(DatabaseUtil.COLUMN_ALARM_TIME);

                Reminder item = new Reminder(i, reminder, frequency, rowId, timeFrom, timeTo, monday, tuesday, wednesday, thursday,
                        friday, saturday, sunday, reminderUseType, notificationType, messageId, active, alarmTime);
                dataArray.add(i, item);

                if (active) Log.d("Main fillDataArray", "next alarm: " + alarmTime);

                cursor.moveToNext();
            }
        }
        updateReminders();
    }

    private void addItemsToSpinner() {
        spinner.setAdapter(myAdapter);
    }

    //todo need to examine this update for refactoring
    private void updateReminders() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                Log.d("Main updateReminders", "next alarm: " + reminder.getAlarmTime());
                reminder.updateCounter();
            }
        }
        myAdapter.reduceCounters(0);
    }

    private void restartAlarms() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                startReminder(reminder.getIndexId());
            }
        }
    }

    private void displayToast(String text){
        String message = "Reminder: " + text;
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void startReminder(int position) {
        Reminder reminder = myAdapter.getItem(position);
        //reminder.setActive(true);

        int rowId = (int) reminder.getRowId();
        long alarmTime = reminder.getAlarmTime();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        //alarmIntent.setAction("com.remindme.action.ALARM_INDEF");
        alarmIntent.putExtra("reminder", reminder.getReminder());
        alarmIntent.putExtra("rowId", rowId);
        alarmIntent.putExtra("useType", reminder.getUseType());
        alarmIntent.putExtra("frequency", reminder.getFloatFrequency());
        alarmIntent.putExtra("days", reminder.getMessageDays());
        alarmIntent.putExtra("timeFrom", reminder.getTimeFrom());
        alarmIntent.putExtra("timeTo", reminder.getTimeTo());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), rowId,
                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    private void cancelReminder(int position) {
        Reminder reminder = myAdapter.getItem(position);
        reminder.setActive(false);

        int reminderId = (int) reminder.getRowId();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        Log.v("cancelReminder", "Cancelling ...");
    }

    private void registerSpinnerOnTouchEvent() {
        xDown = 0;
        xUp = 0;
        yDown = 0;
        yUp = 0;

        spinner.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                String msg;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
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
                        } else if (xDown - xUp > 100) {
                            //msg = "left";
                            //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
                            //createDeleteDialog();
                        }

                        if (Math.abs(yDown - yUp) > 50) {
                            closeAllReminders();
                            myAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
                return false;
            }
        });
    }

    private void goEdit(int position) {
        Intent intent = new Intent(this, EditActivity.class);
        Reminder reminder = myAdapter.getItem(position);

        if (reminder.isActive()) {
            reminder.setActive(false);
            cancelReminder(position);
        }
        reminder.visibility = View.GONE;

        intent.putExtra("editType", true);
        intent.putExtra("arrayId", position);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        edited = true;
    }

    private void goDelete(int position) {
        Reminder reminder = myAdapter.getItem(position);
        long reminderId = reminder.getRowId();

        if (reminder.isActive()) { cancelReminder(position); }
        myAdapter.removeItem(position);
        spinnerRow = -1;
        closeAllReminders();
        myAdapter.notifyDataSetChanged();

        //remove from db
        myDb.deleteRow(reminderId);

        //re-index reminders in dataArray
        reIndexArray();
    }

    private void reIndexArray() {
        int size = myAdapter.getCount();
        Reminder reminder;
        for (int i = 0; i < size; i++) {
            reminder = myAdapter.getItem(i);
            reminder.setIndexId(i);
        }
    }

    private void createDeleteDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Confirm Delete")
                .setMessage("Would you like to delete the reminder: " +
                        myAdapter.getItem(position).getReminder())
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        goDelete(position);
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

    private void closeAllReminders() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            reminder.visibility = View.GONE;
            reminder.setOpened(false);
        }
    }


    //end of Main
}
