package com.remindme;

import android.util.Log;
import android.view.View;
import org.joda.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Scott on 4/26/2015.
 */

//Reminder class provides a structure for organizing the data and methods of each reminder
public class Reminder {
    public static boolean mOpened = false;

    private long rowId;  //id of reminder in the database
    private int reminderId; //id of reminder that corresponds with position in dataArray
    private String reminder; //name of the reminder
    private String frequency; //represents either the time of day or the countdown timer
    private float floatFrequency; //the frequency in decimal hours
    private float timeFrom; //start time of a daily reminder's time range
    private float timeTo; //end time of a daily reminder's time range

    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
    private ArrayList<Integer> messageDays; //array containing boolean representations of selected days
    private boolean reminderUseType;  //true for recurring, false for single use
    private boolean notificationType;  //true for notification, false for alarm
    private int messageId; //this is the position of the alarm or notification sound setting
    private long alarmTime;  //this time is when the next alarm is set for, value is in calendar millis

    private boolean active; //active reminders have been started and are counting down
    public boolean bSelected; //selected indicates a reminder currently selected in the list view
    private long counter;  //the counter is the current value of the countdown timer in milliseconds

    private static final String FORMAT = "%01dd %01d:%01d:%02d";
    public int visibility = View.GONE; //visibility used to indicate which reminder is expanded in list view

    //public constructor used to set up a new reminder or load one from the database
    public Reminder (int remId, String rem, String freq, long id, float fTimeFrom, float fTimeTo, boolean bMonday, boolean bTuesday, boolean bWednesday,
                     boolean bThursday, boolean bFriday, boolean bSaturday, boolean bSunday,
                     boolean bUseType, boolean bNotType, int iMsgId, boolean bActive, long lAlarmTime) {
        reminderId = remId;
        reminder = rem;
        frequency = freq;
        floatFrequency = Float.parseFloat(frequency);
        rowId = id;
        timeFrom = fTimeFrom;
        timeTo = fTimeTo;

        messageDays = new ArrayList<Integer>();
        monday = bMonday;if (monday) { messageDays.add(1); }
        tuesday = bTuesday; if (tuesday) { messageDays.add(2); }
        wednesday = bWednesday;if (wednesday) { messageDays.add(3); }
        thursday = bThursday; if (thursday) { messageDays.add(4); }
        friday = bFriday; if (friday) { messageDays.add(5); }
        saturday = bSaturday; if (saturday) { messageDays.add(6); }
        sunday = bSunday; if (sunday) { messageDays.add(7); }

        reminderUseType = bUseType;
        notificationType = bNotType;
        messageId = iMsgId;
        active = bActive;
        alarmTime = lAlarmTime;

        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
        bSelected = false;
    }

    public long getRowId() { return rowId; }

    public String getReminder() { return reminder; }

    public String getFrequency() { return frequency; }

    public boolean getOpened() { return mOpened; }

    public String getFormattedFrequency() {
        return TimeUtil.FloatTimeToStringHMS(floatFrequency);
    }

    public float getFloatFrequency() { return floatFrequency; }

    public float getTimeFrom() { return timeFrom; }

    public String getTimeFromAsString() {
        return TimeUtil.FloatTimeToString(timeFrom);
    }

    public float getTimeTo() { return timeTo; }

    public String getTimeToAsString() {
        return TimeUtil.FloatTimeToString(timeTo);
    }

    public boolean[] getDays() {
        boolean days[] = { monday, tuesday, wednesday, thursday, friday, saturday, sunday};
        return days;
    }

    public ArrayList<Integer> getMessageDays() { return messageDays; }

    public boolean getRecurring() { return reminderUseType; }

    public boolean getNotificationType() { return notificationType; }

    public int getMessageId() { return messageId; }

    public long getAlarmTime() { return alarmTime; }

    public boolean getUseType() {return reminderUseType; }

    public int getIndexId() { return reminderId; }

    public boolean isActive() { return active; }

    public void setIndexId(int position) {
        reminderId = position;
    }

    public void setFloatFrequency(float floatFreq) {
        floatFrequency = floatFreq;
        frequency = String.valueOf(floatFreq);

        resetCounter();
        dataUpdate();
    }

    public void setOpened(boolean opened) {
        mOpened = opened;
    }

    public void setTimes(float from, float to) {
        timeFrom = from;
        timeTo = to;
        dataUpdate();
    }

    public void setAlarmTime(long time) {
        alarmTime = time;
    }

    //the active flag indicates if the reminder currently is counting down
    public void setActive(boolean bActive) {
        active = bActive;
        long systemTime = System.currentTimeMillis();
        LocalTime localTime = LocalTime.now();
        float currentTime = TimeUtil.MillisecondsToFloatTime(localTime.getMillisOfDay());

        if (reminderUseType) {
            counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
        } else {
            if (floatFrequency >= currentTime) {
                counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency - currentTime);
            } else {
                counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency - currentTime + 24);
            }
        }
        alarmTime = systemTime + counter;
        dataUpdate();
    }

    //resets the countdown timer
    public void resetCounter() {
        LocalTime localTime = LocalTime.now();
        float currentTime = TimeUtil.MillisecondsToFloatTime(localTime.getMillisOfDay());

        if (reminderUseType) {
            counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
        } else {
            if (floatFrequency >= currentTime) {
                counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency - currentTime);
            } else {
                counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency - currentTime + 24);
            }
        }
    }

    //formats the countdown timer for display
    public String getCounterAsString() {
        long time = counter;
        String strTimer;
        if (time == 0) {
            strTimer = "";
        } else if (!reminderUseType && !active) {
            strTimer = TimeUtil.FloatTimeToStringExact(floatFrequency);
        } else {
            long days = TimeUnit.MILLISECONDS.toDays(time);
            long hours = TimeUnit.MILLISECONDS.toHours(time) -
                    TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(time));
            long minutes = TimeUnit.MILLISECONDS.toMinutes(time) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time));
            long seconds = TimeUnit.MILLISECONDS.toSeconds(time) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

            String format;
            if (days > 0) {
                format = "%01dd %02d:%02d:%02d";
                strTimer = ("" + String.format(format, days, hours, minutes, seconds));
            } else if (hours > 0) {
                format = "%01d:%02d:%02d";
                strTimer = ("" + String.format(format, hours, minutes, seconds));
            } else if (minutes >= 10) {
                format = "%02d:%02d";
                strTimer = ("" + String.format(format, minutes, seconds));
            } else {
                format = "%01d:%02d";
                strTimer = ("" + String.format(format, minutes, seconds));
            }
        }

        return (strTimer);
    }

    //reduce counter method is used to decrement the count down variable used to display the reminder time
    public boolean reduceCounter(long increment) {
        long time = counter;
        boolean nextAlarm = false;
//        Log.v("reduceCounter start", "active: " + active + ", counter: " + counter);
        if (active) {
            if (time <= 0) {
                long newTime = scheduleNextAlarm();  //need return value for updating counter, if 0 can set active to false
                if (newTime == 0) {
                    active = false;
                    dataUpdate();
                } else {
                    nextAlarm = true;
                }
                time = newTime;
            } else {
                time = time - increment;
            }
            counter = time;
        }
//        Log.v("reduceCounter end", "counter: " + counter + ", next alarm: " + nextAlarm);
        return nextAlarm;  //this indicates whether or not the timer has completed
    }

    //an update method for the counter for when the app wakes from the background
    public boolean updateCounter() {
        boolean changed = false;
        if (active) {
            long currentTime = System.currentTimeMillis();
            if (alarmTime > currentTime) {
                counter = alarmTime - currentTime;
            } else {
                changed = true;
            }
        }
        return changed;
    }

    //set up next alarm based on the class settings
    public long scheduleNextAlarm() {

        long nextTime = TimeUtil.scheduleAlarm(reminderUseType, floatFrequency, messageDays,
                timeFrom, timeTo);

        if (nextTime == 0) {
            active = false;
            alarmTime = 0;
        } else {
            alarmTime = nextTime;
        }

        dataUpdate();
        return alarmTime;
    }

    //returns a string containing abbreviations of the days for filling a TextView
    //can return a collection of days, Monday-Friday, Weekend, All Week, etc
    public String getDaysAsString() {
        String days[] = new String[7];
        String stringDays = "";
        int firstDay = -1;

        int weekdayCounter = 0;
        int weekendCounter = 0;

        if (monday) {days[0] = "M"; weekdayCounter++;} else {days[0] = "";}
        if (tuesday) {days[1] = "Tu"; weekdayCounter++;} else {days[1] = "";}
        if (wednesday) {days[2] = "W"; weekdayCounter++;} else {days[2] = "";}
        if (thursday) {days[3] = "Th"; weekdayCounter++;} else {days[3] = "";}
        if (friday) {days[4] = "F"; weekdayCounter++;} else {days[4] = "";}
        if (saturday) {days[5] = "Sa"; weekendCounter++;} else {days[5] = "";}
        if (sunday) {days[6] = "Su"; weekendCounter++;} else {days[6] = "";}

        if (weekdayCounter + weekendCounter == 7) {stringDays = "All Week";
        } else if (weekdayCounter == 5 && weekendCounter == 0) {stringDays = "M-F";
        } else if (weekdayCounter == 0 && weekendCounter == 2) {stringDays = "Weekend";
        } else {
            for (int i = 0; i < 7; i++) {if (!days[i].isEmpty()) {firstDay = i; break;}}
            if (firstDay != -1) {
                for (int i = firstDay; i < 7; i++) {
                    if (i == firstDay) {
                        stringDays = days[i];
                    } else if (!days[i].isEmpty()) {
                        stringDays = stringDays + "," + days[i];
                    }
                }
            }
        }
        return stringDays;
    }

    //updates the singleton after an edit
    public void editUpdate(boolean editedReminder, int position){
        if (!editedReminder) {
            SingletonDataArray.getInstance().addReminder(this);
        } else {
            bSelected = true;
        }
    }

    //updates the database after a change
    public void dataUpdate() {
        DatabaseUtil db = new DatabaseUtil();
        db.open();
        db.updateRow(this);
        db.close();
//        Log.d("reminder dataUpdate", "updating database");
    }

    //updates values within this reminder
    public void updateValues(String rem, float fTimeFrom, float fTimeTo, boolean bMonday, boolean bTuesday, boolean bWednesday,
                             boolean bThursday, boolean bFriday, boolean bSaturday, boolean bSunday,
                             boolean bUseType, boolean bNotType, int iMsgId, boolean bActive, long lAlarmTime) {
        reminder = rem;
        timeFrom = fTimeFrom;
        timeTo = fTimeTo;

        messageDays = new ArrayList<Integer>();
        monday = bMonday; if (monday) { messageDays.add(1); }
        tuesday = bTuesday; if (tuesday) { messageDays.add(2); }
        wednesday = bWednesday; if (wednesday) { messageDays.add(3); }
        thursday = bThursday; if (thursday) { messageDays.add(4); }
        friday = bFriday; if (friday) { messageDays.add(5); }
        saturday = bSaturday; if (saturday) { messageDays.add(6); }
        sunday = bSunday; if (sunday) { messageDays.add(7); }

        reminderUseType = bUseType;
        notificationType = bNotType;
        messageId = iMsgId;
        active = bActive;
        alarmTime = lAlarmTime;
        dataUpdate();

        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
    }

    //changes visibility of the reminder
    public void toggleVisibility()
    {
        if(visibility==View.GONE) {
            visibility = View.VISIBLE;
            mOpened = true;
        } else {
            visibility = View.GONE;
            mOpened = false;
        }
    }


}   //end of Reminder class
