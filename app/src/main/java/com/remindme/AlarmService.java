package com.remindme;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Scott on 5/5/2015.
 */

public class AlarmService extends IntentService {
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION = "com.remindme.AlarmService";

    public AlarmService() {
        super("Alarm Service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onHandleIntent(Intent intent) {
        Log.d("AlarmService", "Preparing to send notification...");
        NotificationManager alarmNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String reminder = intent.getStringExtra("reminder");
        int rowId = intent.getIntExtra("rowId", 1);

        //set up Alarm or Notification activity screen
        Intent alarmIntent = new Intent(this, MainActivity.class);

        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, rowId, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
        this).setContentTitle("Remind Me")
            .setSmallIcon(R.drawable.notification_logo)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.r_logo))
            .setStyle(new NotificationCompat.BigTextStyle().bigText(reminder))
            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
            .setLights(Color.RED, 3000, 3000)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setTicker(reminder)
            .setContentText(reminder);

        alarmNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(NOTIFICATION_ID, alarmNotificationBuilder.build());

        Log.d("AlarmService", "Notification sent.");

    }


    //end of AlarmService class
}