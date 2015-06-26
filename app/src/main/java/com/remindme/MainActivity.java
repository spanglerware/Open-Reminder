package com.remindme;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity implements ReminderCallbacks {

    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;

    private DatabaseUtil myDb;
    private ReminderCallbacks reminderCallbacks;

    private boolean bSelected;

    private Button btnStart;
    public ListView spinner;

    //ArrayList<Reminder> dataArray;
    MyAdapter myAdapter;
    Runnable timerRunnable;
    Handler timerHandler;
    float xDown, xUp, yDown, yUp;

    private String spinnerReminder;
    private long spinnerDbId;
    private int spinnerRow;

    private static MainActivity inst;

    //todo BUG visibility behavior after delete not working correctly?

    //todo BUG counter reduction to 0 calls next alarm, should be event from service

    //todo BUG active flag not loading, counters not starting

    //todo BUG start and cancel buttons showing at different areas due to content wrap

    //todo BUG start stop buttons not showing correctly, or counter not working correctly

    //todo save active flag in db upon start/stop reminder?

    //todo need to implement onSaveInstanceState

    //todo add color to UI

    //todo remove log statements for release

    //todo need smaller icons for app and notification

    //todo clean up and comment code in main


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        Log.v("onPause", "Main fired onCreate");

        openDB();

        bSelected = false;

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

        //end of MainActivity onCreate
    }

    private void setViewVisibilities(int position) {
        int selected = MyAdapter.selectedId;
        spinnerRow = position;
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        Reminder reminder = dataArray.get(position);
        if (reminder.getOpened()) {
            if (selected == position) {
                reminder.toggleVisibility();
            } else if (selected == -1) {
                closeAllReminders();
            } else {
                Reminder previous = dataArray.get(selected);
                previous.toggleVisibility();
                reminder.toggleVisibility();
                MyAdapter.selectedId = position;
            }
        } else {
            reminder.toggleVisibility();
            MyAdapter.selectedId = position;
        }

        myAdapter.notifyDataSetChanged();
        spinner.setSelection(position);
    }

    @Override
    protected void onPause() {
        //wakeLock.release();
        super.onPause();
        Log.v("onPause", "Main fired onPause");
        //from documentation: When an activity's onPause() method is called, it should commit to the backing content provider or file any changes the user has made.
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("onStop", "Main fired onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("onResume", "Main fired onResume");
        //dataArray = SingletonDataArray.getInstance().getDataArray();
        updateReminders();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            //todo could add getSize method to singleton
            ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("arrayId", dataArray.size());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void startReminderCallBack(int position) {
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
                int interval = 1000;
                boolean reduced = false;
                timerHandler.postDelayed(this, interval); //run every second
                ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();

                int arraySize = dataArray.size();
                for (int i = 0; i < arraySize; i++) {
                    Reminder reminder = dataArray.get(i);
                    if (reminder.isActive()) {
                        Log.v("active reminder counter", reminder.getCounterAsString());
                        reduced = reminder.reduceCounter(interval);
                        SingletonDataArray.getInstance().updateReminder(reminder, i);
                        if (reduced) {
                            Log.v("reduced counter", reminder.getCounterAsString());
                            startReminder(i);
                        }
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
                if (rowId == spinnerDbId) { spinnerRow = i + 1; spinnerReminder = reminder; }

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

                Reminder item = new Reminder(reminder, frequency, rowId);
                item.setDays(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
                item.setTimes(timeFrom, timeTo);
                item.setMisc(reminderUseType, notificationType, messageId);
                dataArray.add(i, item);

                cursor.moveToNext();
            }
        }
    }

    private void addItemsToSpinner() {
        spinner.setAdapter(myAdapter);
        spinner.setSelection(spinnerRow);
    }

    private void updateReminders() {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                reminder.updateCounter();
                myAdapter.reduceCounters(0);
            }
        }
    }

    private void displayToast(String text){
        String message = "Reminder: " + text;
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, this.getIntent());

        if (resultCode == Activity.RESULT_OK) {
            ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
            Reminder reminder = data.getParcelableExtra("reminder");
            switch (requestCode) {
                case (REQUEST_NEW): {
                        //result is from successful insert of new reminder
                        dataArray.add(reminder);
                    break;
                }
                case (REQUEST_EDIT): {
                        //result is from successful edit
                        dataArray.set(spinnerRow, reminder);
                    break;
                }
            }
            myDb.updateRow(reminder);
        }
    }


    private void startReminder(int position) {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        Reminder reminder = dataArray.get(position);
        reminder.setActive(true);
        SingletonDataArray.getInstance().updateReminder(reminder, position);
        myDb.updateRow(reminder);

        int reminderId = (int) reminder.getRowId();
        long alarmTime = reminder.getAlarmTime();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("reminder", reminder.getReminder());
        alarmIntent.putExtra("reminderId", reminderId);
        alarmIntent.putExtra("messageId", reminder.getMessageId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId,
                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    private void cancelReminder(int position) {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        Reminder reminder = dataArray.get(position);
        reminder.setActive(false);
        SingletonDataArray.getInstance().updateReminder(reminder, position);
        myDb.updateRow(reminder);

        int reminderId = (int) reminder.getRowId();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        Log.v("cancelReminder", "Cancelling ...");
    }

    private void registerSpinnerSelectionEvent() {
        xDown = 0;
        xUp = 0;
        yDown = 0;
        yUp = 0;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View viewSelected, int position, long idInDb) {
                ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
                Reminder item = dataArray.get(position);
                spinnerReminder = item.getReminder();
                spinnerDbId = item.getRowId();
                spinnerRow = position;

                MyAdapter.TestViewHolder viewHolder = (MyAdapter.TestViewHolder) viewSelected.getTag();

                //set visibility of child views to expand or contract selection
                if (!bSelected) {
//                    viewHolder.tvHolderNotType.setVisibility(View.VISIBLE);
                    viewHolder.tvHolderTimes.setVisibility(View.VISIBLE);
                    viewHolder.tvHolderDays.setVisibility(View.VISIBLE);
                    viewHolder.tvHolderFrequency.setVisibility(View.VISIBLE);

                } else {
//                    viewHolder.tvHolderNotType.setVisibility(View.GONE);
                    viewHolder.tvHolderTimes.setVisibility(View.GONE);
                    viewHolder.tvHolderDays.setVisibility(View.GONE);
                    viewHolder.tvHolderFrequency.setVisibility(View.GONE);

                }
                bSelected = !bSelected;
                myDb.changeCurrent(spinnerDbId);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //no updates needed
            }
        });

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
                            //set an adapter flag in order to close expanded list item when list scrolled

                            //todo set up another condition if item is last in the list view
                            //if (spinnerRow != spinner.getCount() - 1) {
                            myAdapter.listMoved = true;
                            closeAllReminders();
                            myAdapter.notifyDataSetChanged();
                            //}
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

    private void goEdit(int position) {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        Intent intent = new Intent(this, EditActivity.class);
        Reminder reminder = dataArray.get(position);

        if (reminder.isActive()) {
            reminder.setActive(false);
            cancelReminder(position);
        }

        intent.putExtra("reminder", reminder);
        intent.putExtra("position", position);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goDelete(int position) {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        Reminder reminder = dataArray.get(position);
        long reminderId = reminder.getRowId();

        if (reminder.isActive()) { cancelReminder(position); }
        dataArray.remove(position);
        myAdapter.notifyDataSetChanged();

        //remove from db
        myDb.deleteRow(reminderId);
    }


    private void createDeleteDialog(final int position) {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Confirm Delete")
                .setMessage("Would you like to delete the reminder: " +
                        dataArray.get(position).getReminder())
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
