package com.remindme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.joda.time.LocalTime;

/**
 * Created by Scott on 5/5/2015.
 */
public class AlarmReceiverAlt extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("reminder");
        int noteId = intent.getIntExtra("noteId", 0);

        Intent noteIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, noteId, noteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        Notification notification = new Notification.Builder(context)
                .setContentIntent(contentIntent)
                .setContentText(message)
                .setContentTitle("Reminder")
                .setAutoCancel(true)
                .setWhen(LocalTime.now().getMillisOfDay())
                .build();

        notificationManagerCompat.notify(noteId, notification);
    }


    //end of AlarmReceiver class
}
