package com.remindme;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
        String msg = "Wake Up! Wake Up!";
        Log.d("AlarmService", "Preparing to send notification...: " + msg);
        alarmNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String reminder = intent.getStringExtra("reminder");
        int reminderId = intent.getIntExtra("reminderId", 1);
        int messageId = intent.getIntExtra("messageId", 1);

        //set up Alarm or Notification activity screen
        Intent alarmIntent = new Intent(this, MyAlarmActivity.class);
        alarmIntent.putExtra("reminder", reminder);
        alarmIntent.putExtra("messageId", reminderId);
        alarmIntent.putExtra("messageId", messageId);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, reminderId, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(
        this).setContentTitle("Alarm").setSmallIcon(R.drawable.abc_ic_voice_search_api_mtrl_alpha)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 3000, 3000)
        .setContentText(msg);

        alarmNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alarmNotificationBuilder.build());

        //startActivity(alarmIntent);
        Log.d("AlarmService", "Notification sent.");

//        AlarmReceiver.completeWakefulIntent(intent);
    }





    //end of AlarmService class
}