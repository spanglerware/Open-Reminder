package com.remindme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Scott on 5/4/2015.
 */
public class MyNotification {

    private PendingIntent pendingIntent;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;
    private Calendar calendar;
    private int notification_id;
    private int noteTime;
    private long noteSetTime;
    private Notification notification;
    private String noteText;
    private Context myContext;

    public MyNotification(Context context, int noteId, int time, int messageId, String reminder) {
        notification_id = noteId + 1000000;  //this will separate note Ids from alarm Ids
        noteTime = time;
        noteText = reminder;
        myContext = context;

        Intent intent = new Intent();
        intent.setClass(context, AlarmReceiver.class);
        intent.putExtra("messageId", messageId);
        intent.putExtra("reminder", noteText);
        intent.putExtra("noteId", notification_id);

        //todo make sure notification id does not overlap alarm id for the pending intent
        //pendingIntent = PendingIntent.getActivity(context, notification_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent = PendingIntent.getBroadcast(myContext, notification_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

    }

    public void start() {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, noteTime);
        noteSetTime = calendar.getTimeInMillis();

/*
        notification = new Notification.Builder(myContext)
                .setContentIntent(pendingIntent)
                .setContentText(noteText)
                .setContentTitle("Reminder")
                .setAutoCancel(true)
                .setWhen(noteSetTime)
                .build();
*/
        //todo crashing here
        //notificationManager.notify();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, noteSetTime, pendingIntent);

    }

    public void cancel() { alarmManager.cancel(pendingIntent); }

    public long getNoteTime() { return noteSetTime; }

    public PendingIntent getPendingIntent() { return pendingIntent; }



    //end of MyNotification class
}
