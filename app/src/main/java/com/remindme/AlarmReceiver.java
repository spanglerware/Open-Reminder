package com.remindme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Scott on 5/5/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        //this will update the UI with message
        //AlarmActivity inst = AlarmActivity.instance();
        //inst.setAlarmText("Alarm! Wake up! Wake up!");

        String reminder = intent.getStringExtra("reminder");
        int reminderId = intent.getIntExtra("reminderId", 1);
        int messageId = intent.getIntExtra("messageId", 1);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire();

        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)
/*
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
*/
        //this will send a notification message
//        ComponentName comp = new ComponentName(context.getPackageName(), AlarmService.class.getName());
//        startWakefulService(context, (intent.setComponent(comp)));
        Intent intentService = new Intent(context, AlarmService.class);
        intentService.putExtra("reminder", reminder);
        intentService.putExtra("reminderId", reminderId);
        intentService.putExtra("messageId", messageId);

        context.startService(intentService);
        setResultCode(Activity.RESULT_OK);
        wakeLock.release();
    }


    //end of AlarmReceiver class
}