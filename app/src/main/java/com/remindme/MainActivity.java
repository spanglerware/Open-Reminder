package com.remindme;

import android.animation.LayoutTransition;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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


public class MainActivity extends ActionBarActivity implements ReminderCallbacks {

    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;

    private DatabaseUtil myDb;
    private ReminderCallbacks reminderCallbacks;

    private boolean bStarted;
    private boolean bSelected;

    private Button btnStart;
    public ListView spinner;

    ArrayList<Reminder> dataArray;
    MyAdapter myAdapter;
    Runnable timerRunnable;
    float xDown, xUp, yDown, yUp;

    private String spinnerReminder;
    private long spinnerDbId;
    private int spinnerRow;

    private static MainActivity inst;


    //TODO display an alarm/notification screen/dialog if message fires while app is in background, currently stays in background

    //todo need icons for app and notification

    //todo clean up and comment code in main

    //todo add random reminders using Notification CATEGORY_RECOMMENDATION

    //todo set layouts to scrollable or change property so landscape is disabled

    //todo simplify app to focus on primary function


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        openDB();

        bStarted = false;
        bSelected = false;
        //btnStart = (Button) findViewById(R.id.buttonStart);


        fillDataArray();
        addItemsToSpinner();
        //registerSpinnerOnTouchEvent();
        callHandler();

        spinner.setItemsCanFocus(true);
        spinner.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyAdapter.TestViewHolder viewHolder = (MyAdapter.TestViewHolder) view.getTag();
                int selected = MyAdapter.selectedId;

                //set visibility of child views to expand or contract selection
                if (selected == position) {
                    viewHolder.llSecondary.setVisibility(View.GONE);
                    viewHolder.llAll.setBackgroundResource(R.color.transparent);
//                    viewHolder.llAll.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                    MyAdapter.selectedId = -1;
                } else if (selected == -1) {
                    viewHolder.llSecondary.setVisibility(View.VISIBLE);
                    viewHolder.llAll.setBackgroundResource(R.color.light_grey);
//                    viewHolder.llAll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                    MyAdapter.selectedId = position;
                } else {
                    //close previous selection then open new one
                    View viewPrevious = parent.getChildAt(selected);
                    if (viewPrevious != null) {
                        MyAdapter.TestViewHolder vhPrevious = (MyAdapter.TestViewHolder) viewPrevious.getTag();
                        vhPrevious.llSecondary.setVisibility(View.GONE);
                        vhPrevious.llAll.setBackgroundResource(R.color.transparent);
                    }
//                    vhPrevious.llAll.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                    viewHolder.llSecondary.setVisibility(View.VISIBLE);
                    viewHolder.llAll.setBackgroundResource(R.color.light_grey);
//                    viewHolder.llAll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                    MyAdapter.selectedId = position;
                }

            }
        });


        //end of MainActivity onCreate
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
        updateReminders();
        dataArray = SingletonDataArray.getInstance().getDataArray();
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
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("arrayId", dataArray.size());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void startReminderCallBack(Reminder reminder) {
        startReminder(reminder);
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
    public void cancelReminderCallBack(Reminder reminder) {
        cancelReminder(reminder);
    }

    @Override
    public void notificationCallBack(String reminderText) {
        displayToast(reminderText);
    }

    //this handler sets up a countdown timer for the reminders in the spinner
    private void callHandler() {
        final Handler timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int interval = 1000;
                timerHandler.postDelayed(this, interval); //run every second
                for (Reminder reminder : dataArray) {
                    if (reminder.isActive()) {
                        Log.v("active reminder counter", reminder.getCounterAsString());
                        if (reminder.reduceCounter(interval)) {
                            Log.v("reduced counter", reminder.getCounterAsString());
                            //startReminder(reminder);
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
        Time time;

        Cursor cursor = myDb.getAllRows();
        int count = cursor.getCount();
        dataArray = SingletonDataArray.getInstance().getDataArray();

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
        //set up the custom adapter
        spinner = (ListView) findViewById(R.id.listview_reminder);
        myAdapter = new MyAdapter(getApplicationContext(), spinner, this);
    }

    private void addItemsToSpinner() {
        spinner.setAdapter(myAdapter);
        spinner.setSelection(spinnerRow);
    }

    private void updateReminders() {
        //todo controls are now in MyAdapter, need to update there

        for (Reminder reminder : dataArray) {
            if (reminder.isActive()) {
                if (reminder.updateCounter()) {

                }
                //myAdapter.reduceCounters(0);
//                if (!reminder.isActive()) {
//                } else {
//                }
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


    private void startReminder(Reminder reminder) {
        int reminderId = (int) reminder.getRowId();
        long alarmTime = reminder.getAlarmTime();
        reminder.setActive(true);
        bStarted = true;
        //setButtonImage(false);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("reminder", reminder.getReminder());
        alarmIntent.putExtra("reminderId", reminderId);
        alarmIntent.putExtra("messageId", reminder.getMessageId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reminderId,
                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    private void cancelReminder(Reminder reminder) {
        int reminderId = (int) reminder.getRowId();
        reminder.setActive(false);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, reminderId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.cancel();
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
                } else {
                    bStarted = false;
                }

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
        spinner.setOnTouchListener(new View.OnTouchListener() {
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
        Intent intent = new Intent(this, EditActivity.class);
        Reminder reminder = dataArray.get(position);
        reminder.setActive(false);
        intent.putExtra("reminder", reminder);
        intent.putExtra("position", position);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goDelete(int position) {
        //remove reminder from mItems
        long reminderId = dataArray.get(position).getRowId();
        dataArray.remove(position);
        //update reminder instance
        SingletonDataArray.getInstance().removeReminder(position);
        myAdapter.notifyDataSetChanged();

        //remove from db
        myDb.deleteRow(reminderId);

        //refresh adapter
        myAdapter.refresh();
    }

    private void createDeleteDialog(final int position) {
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


    //end of Main
}
