package com.openreminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.openreminder.R;

import java.util.ArrayList;

//MainActivity loads the data and sets up the list view containing the reminders
public class MainActivity extends ActionBarActivity implements ReminderCallbacks {

    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;
    private static final int RUNNABLE_INTERVAL = 1000;  //runnable set to 1 second delay
    public static final String EDIT_TYPE_KEY = "editType";
    public static final String EDIT_ARRAY_ID_KEY = "arrayId";

    private DatabaseUtil myDb;

    private boolean bSelected;
    private boolean edited = false;
    private static int editPosition = 0;

    public ListView spinner;
    private MyAdapter myAdapter;
    private Runnable timerRunnable;
    private Handler timerHandler;
    float xDown, xUp, yDown, yUp;

    private long spinnerDbId;
    private int spinnerRow;
    private int syncCounter = 0;
    private static MainActivity instance;


    //onCreate method loads data into list view and registers event handlers
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        //Log.v("onPause", "Main fired onCreate");

        //open database connection
        openDB();

        bSelected = false;
        spinnerRow = -1;

        //load data into list view
        fillDataArray();
        addItemsToSpinner();
        registerSpinnerOnTouchEvent();

        //set up internal timer which operates the countdown timers of the running reminders
        callHandler();

        //set up list view click listener
        spinner.setItemsCanFocus(true);
        spinner.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewVisibilities(position);
            }
        });
        IntentFilter filter = new IntentFilter(AlarmService.ACTION);

        //restart any alarms that may have been turned off upon device reboot
        restartAlarms();

        //synchronize countdown timers with system clock
        updateReminders();

        //end of MainActivity onCreate
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Log.v("onPause", "Main fired onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.v("onStop", "Main fired onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    //synchronize timers any time this activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        //Log.v("onResume", "Main fired onResume");
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
        //Log.v("onRestart", "Main fired onRestart");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        closeDb();
        timerHandler.removeCallbacks(timerRunnable);
        //Log.v("onDestroy", "Main fired onDestroy");
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
            editPosition = myAdapter.getCount();
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EDIT_TYPE_KEY, false);
            intent.putExtra(EDIT_ARRAY_ID_KEY, editPosition);
            startActivityForResult(intent, REQUEST_NEW);
            return true;
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    //callback from MyAdapter when start icon of a reminder is tapped
    @Override
    public void startReminderCallBack(int position) {
        Reminder reminder = myAdapter.getItem(position);
        reminder.setActive(true);
        if (reminder.getAlarmTime() > 0) startReminder(position);
    }

    //callback from MyAdapter when delete icon of a reminder is tapped
    @Override
    public void deleteReminderCallBack(int position) {
        createDeleteDialog(position);
    }

    //callback from MyAdapter when edit icon of a reminder is tapped
    @Override
    public void editReminderCallBack(int position) {
        goEdit(position);
    }

    //callback from MyAdapter when stop icon of a reminder is tapped
    @Override
    public void cancelReminderCallBack(int position) {
        cancelReminder(position);
    }

    //this callback no currently being implemented
    @Override
    public void notificationCallBack(String reminderText) {
        displayToast(reminderText);
    }

    //this handler sets up a countdown timer for the reminders in the list view
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
                        //reminder.reduceCounter(interval);
                    }
                }

                syncCounter++;
                if (syncCounter >= 10) {
                    //run sync every 10 seconds to ensure counter is synchronized with alarm
                    updateReminders();
                    //reset counter
                    syncCounter = 0;
                }

                myAdapter.notifyDataSetChanged();
            }
        };
        timerRunnable.run();
    }

    //opens connection to database
    private void openDB() {
        myDb = new DatabaseUtil(this);
        myDb.open();
    }

    //closes database connection
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

                //if (active) Log.d("Main fillDataArray", "next alarm: " + alarmTime);

                cursor.moveToNext();
            }
        }
    }

    //method sets the adapter for the list view
    private void addItemsToSpinner() {
        spinner.setAdapter(myAdapter);
    }

    //updateReminders synchronizes the countdown timers of the running reminders to the system clock
    private void updateReminders() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                //Log.d("Main updateReminders", "next alarm: " + reminder.getAlarmTime());
                reminder.updateCounter();
            }
        }
        myAdapter.reduceCounters(0);
    }

    //restartAlarms used to restart any alarms that may have been turned off by device reboot
    private void restartAlarms() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                startReminder(reminder.getIndexId());
            }
        }
    }

    //this method sets a previously selected reminder as expanded and closes all others
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

    private void displayToast(String text){
        String message = "Reminder: " + text;
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    //startReminder creates a system alarm that will be broadcast at the specified time
    private void startReminder(int position) {
        Reminder reminder = myAdapter.getItem(position);
        //reminder.setActive(true);

        //database row id of the reminder is used as the unique identifier for the alarm
        int rowId = (int) reminder.getRowId();
        long alarmTime = reminder.getAlarmTime();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        //alarmIntent.setAction("com.openreminder.action.ALARM_INDEF");
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

    //cancelReminder cancels a system alarm that had been previously scheduled
    private void cancelReminder(int position) {
        Reminder reminder = myAdapter.getItem(position);
        reminder.setActive(false);

        int reminderId = (int) reminder.getRowId();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        //Log.v("cancelReminder", "Cancelling ...");
    }

    //OnTouchEvent initially set up to act on swipe events, now just closes any expanded reminders
    private void registerSpinnerOnTouchEvent() {
        xDown = 0;
        xUp = 0;
        yDown = 0;
        yUp = 0;

        spinner.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

//                String msg;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        xDown = event.getX();
                        yDown = event.getY();
                    }

                    case MotionEvent.ACTION_UP: {
                        xUp = event.getX();
                        yUp = event.getY();

                        //use a minimum swipe distance before activating to distinguish between taps and swipes
//                        if (xUp - xDown > 100) {
//                            //msg = "right";
//                            //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
//                        } else if (xDown - xUp > 100) {
//                            //msg = "left";
//                            //Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
//                            //createDeleteDialog();
//                        }

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

    //onActivityResult used to update list based on what occurs in EditActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW) {
            //check for blank reminder, delete if found
            Reminder reminder = myAdapter.getItem(editPosition);
            if (reminder.getReminder().equals("")) {
                //remove from db
                myDb.deleteRow(reminder.getRowId());

                //remove from singleton
                myAdapter.removeItem(editPosition);
                myAdapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //goEdit opens the edit activity and sends data on selected reminder
    private void goEdit(int position) {
        Intent intent = new Intent(this, EditActivity.class);
        Reminder reminder = myAdapter.getItem(position);

        if (reminder.isActive()) {
            reminder.setActive(false);
            cancelReminder(position);
        }
        reminder.visibility = View.GONE;

        intent.putExtra(EDIT_TYPE_KEY, true);
        intent.putExtra(EDIT_ARRAY_ID_KEY, position);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_EDIT);

        edited = true;
    }

    //goDelete removes selected reminder from list and database
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

    //reIndexArray synchronizes data array with indexes after a delete
    private void reIndexArray() {
        int size = myAdapter.getCount();
        Reminder reminder;
        for (int i = 0; i < size; i++) {
            reminder = myAdapter.getItem(i);
            reminder.setIndexId(i);
        }
    }

    //delete dialog used to confirm the user would like to remove a reminder
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

    //this method collapses any reminders that were expanded
    private void closeAllReminders() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            reminder.visibility = View.GONE;
            reminder.setOpened(false);
        }
    }


    //end of MainActivity
}
