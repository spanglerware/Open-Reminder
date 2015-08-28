package com.openreminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.openreminder.R;

import java.util.ArrayList;

/**
 * Created by Scott on 6/27/2015.
 */

//AlarmReceiver listens for alarm broadcasts, triggers notification, and schedules following alarms
public class AlarmReceiver extends BroadcastReceiver {

    private float floatFrequency;
    private float timeFrom;
    private float timeTo;
    private ArrayList<Integer> messageDays;
    private boolean reminderUseType;
    private static Context mContext;

    @Override
    public void onReceive(final Context context, Intent intent) {
        ArrayList<Reminder> dataArray = SingletonDataArray.getInstance().getDataArray();

        //now sending application context to this receiver instead of activity context to prevent memory leaks
        if (mContext == null) mContext = context;
        DatabaseUtil db = new DatabaseUtil(mContext);
        db.open();

        String message = intent.getStringExtra("reminder");
        int rowId = intent.getIntExtra("rowId", -1);

        floatFrequency = intent.getFloatExtra("frequency", -1);
        reminderUseType = intent.getBooleanExtra("useType", false);
        timeFrom = intent.getFloatExtra("timeFrom", -1);
        timeTo = intent.getFloatExtra("timeTo", -1);
        messageDays = intent.getIntegerArrayListExtra("days");

//        if (mContext != null) {
//            Log.d("alarm receiver", "context not null");
//        } else {
//            Log.d("alarm receiver", "context null");
//        }

        if (rowId < 0) {
//            Log.d("alarm receiver", "received default value from intent extra, exiting ...");
            return;
//        } else {
//            Log.d("alarm receiver", "intent extra rowId: " + rowId);
        }

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire();

        Intent intentService = new Intent(mContext, AlarmService.class);
        intentService.putExtra("reminder", message);
        intentService.putExtra("rowId", rowId);

        mContext.startService(intentService);
        setResultCode(Activity.RESULT_OK);

        showNotification(mContext, message);

        //catch default values before calling functions
        if (floatFrequency >= 0 && timeFrom >= 0 && timeTo >= 0 && rowId >= 0) {
            long nextAlarm = TimeUtil.scheduleAlarm(reminderUseType, floatFrequency, messageDays,
                    timeFrom, timeTo);
            if (nextAlarm > 0) {
                nextAlarm += System.currentTimeMillis();
                startReminder(mContext, nextAlarm, rowId, message);

                if (!dataArray.isEmpty()) {
                    Reminder reminder = SingletonDataArray.getInstance().getReminderWithId(rowId);
                    reminder.setAlarmTime(nextAlarm);
                }
                //update database alarmTime here
                db.updateSelectRow(DatabaseUtil.FIELD_ALARM_TIME, rowId, String.valueOf(nextAlarm));
            } else {
                if (!dataArray.isEmpty()) {
                    Reminder reminder = SingletonDataArray.getInstance().getReminderWithId(rowId);
                    reminder.setActive(false);
                }
                //update database active to false here
                db.updateSelectRow(DatabaseUtil.FIELD_ACTIVE, rowId, String.valueOf(false));
            }
//            Log.d("Alarm Receiver", "next alarm: " + nextAlarm);
        }

        db.close();
        wakeLock.release();
    }


    private void showNotification(Context context, String message) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        View myView = mInflater.inflate(R.layout.notification_popup, null);

        TextView text = (TextView) myView.findViewById(R.id.notification_text);
        text.setTextSize(24);
        text.setText("Reminder: \n" + message);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(myView);
        toast.show();
    }

    private void startReminder(Context context, long alarmTime, int rowId, String reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        alarmIntent.putExtra("reminder", reminder);
        alarmIntent.putExtra("rowId", rowId);
        alarmIntent.putExtra("useType", reminderUseType);
        alarmIntent.putExtra("frequency", floatFrequency);
        alarmIntent.putExtra("days", messageDays);
        alarmIntent.putExtra("timeFrom", timeFrom);
        alarmIntent.putExtra("timeTo", timeTo);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rowId,
                alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }


    //end of AlarmReceiver class
}
