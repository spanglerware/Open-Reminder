package com.remindme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Scott on 5/1/2015.
 */
public class MyAlarm {
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private Calendar calendar;
    private int alarm_id;
    private int alarmTime;
    private long alarmSetTime;
    private final static long ALARM_ACTIVATION_INTERVAL = 5000;

    public MyAlarm(Context context, int alarmId, int time, int messageId, String reminder) {
        alarm_id = alarmId;
        alarmTime = time;

        Intent intent = new Intent();
        intent.setClass(context, MyAlarmActivity.class);
        intent.putExtra("messageId", messageId);
        intent.putExtra("reminder", reminder);

        pendingIntent = PendingIntent.getActivity(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
    }

    public void start() {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, alarmTime);
        alarmSetTime = calendar.getTimeInMillis();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmSetTime, pendingIntent);
    }

    public void cancel() {
        alarmManager.cancel(pendingIntent);
    }

    public long getAlarmTime() { return alarmSetTime; }

    public PendingIntent getPendingIntent() { return pendingIntent; }

/*
    private void registerOnFinishedEvent() {
        PendingIntent.OnFinished onFinished = new PendingIntent().OnFinished() {
            public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
                gotReceive(0, intent);
            }
        };
    }
*/

    //end of MyAlarm class
}
