package com.remindme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

/**
 * Created by Scott on 6/27/2015.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private float floatFrequency;
    private float timeFrom;
    private float timeTo;
    private ArrayList<Integer> messageDays;
    private boolean reminderUseType;

    @Override
    public void onReceive(final Context context, Intent intent) {

        String message = intent.getStringExtra("reminder");
        int rowId = intent.getIntExtra("rowId", -1);

        floatFrequency = intent.getFloatExtra("frequency", -1);
        reminderUseType = intent.getBooleanExtra("useType", false);
        timeFrom = intent.getFloatExtra("timeFrom", -1);
        timeTo = intent.getFloatExtra("timeTo", -1);
        messageDays = intent.getIntegerArrayListExtra("days");

        if (context != null) {
            Log.d("alarm receiver", "context not null");
        } else {
            Log.d("alarm receiver", "context null");
        }

        if (rowId < 0) {
            Log.d("alarm receiver", "received default value from intent extra, exiting ...");
            return;
        } else {
            Log.d("alarm receiver", "intent extra rowId: " + rowId);

        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire();

        Intent intentService = new Intent(context, AlarmService.class);
        intentService.putExtra("reminder", message);
        intentService.putExtra("rowId", rowId);

        context.startService(intentService);
        setResultCode(Activity.RESULT_OK);

        showNotification(context, message);

//        //TODO start next alarm not working well here, may need to launch another activity and start from there
//        //todo place next alarm scheduling here?
//        int dataArraySize = SingletonDataArray.getInstance().getSize();
//        if (dataArraySize == 0) SingletonDataArray.getInstance().loadFromDb(context);
//
//        Reminder reminder = SingletonDataArray.getInstance().getReminderWithId(rowId);
//        Log.d("alarm receiver", "dataArray size: " + dataArraySize);

        //todo send all needed info with intent then send again with next one
        //todo need use type, frequency, times, and days

//        if (reminder != null) {
//            //schedule next alarm
//            long nextAlarm = reminder.scheduleNextAlarm();
//
//
//            //if no next alarm, set to inactive
//            if (nextAlarm == 0) {
//                reminder.setActive(false);
//                Log.d("alarm receiver", "setting reminder to inactive");
//            } else {
//                startReminder(context, reminder);
//                Log.d("alarm receiver", "starting another reminder: " + reminder.getCounterAsString());
//                Log.d("alarm receiver", "stored frequency: " + reminder.getFloatFrequency());
//            }
//
//        } else {
//            Log.d("alarm receiver", "reminder is null");
//        }

        //todo catch default values before calling functions
        long nextAlarm = scheduleNextAlarm();
        startReminder(context, nextAlarm, rowId, message);

        //todo if not able to update database using this structure will have to check for reminder updates upon restart

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

    //todo refactor main start to include this one
    private void startReminder(Context context, long alarmTime, int rowId, String reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        //alarmIntent.setAction("com.remindme.action.ALARM_INDEF");
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


    public long scheduleNextAlarm() {
        //first calculate next time for today, then check if it is in time range, then check for next days
        LocalTime localTime = LocalTime.now();
        LocalDate localDate = LocalDate.now();

        float currentTime = TimeUtil.MillisecondsToFloatTime(localTime.getMillisOfDay());
        float alarmNextTime;
        long currentSystemTime = System.currentTimeMillis();

        int today = localDate.getDayOfWeek();
        int tomorrow = localDate.getDayOfWeek() + 1;
        if (tomorrow > 7) { tomorrow = 1; }
        boolean found = false;
        boolean exit = false;
        int daysFromToday = 0;
        float nextTime = 0;  //this value will be the next alarm time in float time

        if (!reminderUseType) {
            if (floatFrequency >= currentTime) {
                nextTime = floatFrequency;
            } else if (messageDays.isEmpty()) {
                exit = true;
            } else {
                for (int i = tomorrow; i < 8; i++) {
                    if (messageDays.contains(i)) { found = true; daysFromToday = i - tomorrow + 1;  break; }
                }
                if (!found && tomorrow > 1) {  //if tomorrow = 1 then it has already been covered by loop above
                    for (int i = 1; i < tomorrow; i++) {
                        if (messageDays.contains(i)) { found = true; daysFromToday = 7 - tomorrow + i + 1;  break; }
                    }
                }
                nextTime = (floatFrequency + (24 * daysFromToday));
            }
        } else {
            alarmNextTime = currentTime + floatFrequency;

            //check if frequency fits within timefrom - timeto interval, if not then there will be no repeat
            //also check if no days were selected for repeating
            if (floatFrequency > (timeTo - timeFrom) || messageDays.isEmpty()) {
                exit = true;
            } else {

                //first check for today
                if (messageDays.contains(today) && alarmNextTime < timeTo) {
                    if (alarmNextTime <= timeFrom) {
                        nextTime = timeFrom + floatFrequency;
                    } else {
                        //schedule alarm normally
                        nextTime = alarmNextTime;
                    }
                } else {  //check for next day to run alarm
                    for (int i = tomorrow; i < 8; i++) {
                        if (messageDays.contains(i)) {
                            found = true;
                            daysFromToday = i - tomorrow + 1;
                            break;
                        }
                    }
                    if (!found && tomorrow > 1) {  //if tomorrow = 1 then it has already been covered by loop above
                        for (int i = 1; i < tomorrow; i++) {
                            if (messageDays.contains(i)) {
                                found = true;
                                daysFromToday = 7 - tomorrow + i + 1;
                                break;
                            }
                        }
                    }
                    if (found) {
                        nextTime = (daysFromToday * 24) + timeFrom + floatFrequency;
                    }
                }
            }
        }

        if (exit) {
//            active = false;
//            alarmTime = 0;
            return 0;
        }

        nextTime -= currentTime;
//        alarmTime = TimeUtil.FloatTimeToMilliseconds(nextTime) + currentSystemTime;

//
        return TimeUtil.FloatTimeToMilliseconds(nextTime);
    }


    //end of AlarmReceiver class
}
