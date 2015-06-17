package com.remindme;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Scott on 5/5/2015.
 */

public class AlarmService extends IntentService {
    private NotificationManager alarmNotificationManager;
    private static final int NOTIFICATION_ID = 1;
    private PendingIntent pendingIntent;


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
        alarmNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String reminder = intent.getStringExtra("reminder");
        int reminderId = intent.getIntExtra("reminderId", 1);
        int messageId = intent.getIntExtra("messageId", 1);
//        String msg = "Wake Up! Wake Up!";

        //displayToast(reminder);

        //set up Alarm or Notification activity screen
        Intent alarmIntent = new Intent(this, MainActivity.class);
//        alarmIntent.putExtra("reminder", reminder);
//        alarmIntent.putExtra("messageId", reminderId);
//        alarmIntent.putExtra("messageId", messageId);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, reminderId, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
        this).setContentTitle("Remind Me")
            .setSmallIcon(R.drawable.pencil)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.r_logo))
            .setStyle(new NotificationCompat.BigTextStyle().bigText(reminder))
            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
            .setLights(Color.RED, 3000, 3000)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setTicker(reminder)
            .setContentText(reminder);

        alarmNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alarmNotificationBuilder.build());

        //startActivity(alarmIntent);
        Log.d("AlarmService", "Notification sent.");

//        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void displayToast(String reminder){
        String message = "Reminder: " + reminder;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }



    //end of AlarmService class
}