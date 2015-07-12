package com.remindme;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Scott on 4/26/2015.
 */
public class Reminder {
    public static boolean mOpened = false;

    private long rowId;  //id of reminder in the database
    private int reminderId; //id of reminder that corresponds with position in dataArray
    private String reminder;
    private String frequency;
    private float floatFrequency;
    private float timeFrom;
    private float timeTo;

    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
    private ArrayList<Integer> messageDays;
    private boolean reminderUseType;  //true for recurring, false for single use
    private boolean notificationType;  //true for notification, false for alarm
    private int messageId; //this is the position of the alarm or notification sound setting
    private long alarmTime;  //this time is when the next alarm is set for, value is in calendar millis

    private boolean active;
    public boolean bSelected;
    private long counter;  //the counter is the current value of the countdown timer in milliseconds
    private static final String FORMAT = "%01dd %01d:%01d:%02d";
    public int visibility = View.GONE;

    //todo save to db every time new reminder or something changed

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
            String format = "";
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
        Log.v("reduceCounter start", "active: " + active + ", counter: " + counter);
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
        Log.v("reduceCounter end", "counter: " + counter + ", next alarm: " + nextAlarm);
        return nextAlarm;  //this indicates whether or not the timer has completed
    }

    //need an update method for the counter for when the app wakes from the background
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
            active = false;
            alarmTime = 0;
            return 0;
        }

        nextTime -= currentTime;
        alarmTime = TimeUtil.FloatTimeToMilliseconds(nextTime) + currentSystemTime;

        dataUpdate();
        return TimeUtil.FloatTimeToMilliseconds(nextTime);
    }


    public String getDaysAsString() {
        //returns a string containing abbreviations of the days for filling a TextView
        //can return a collection of days, Monday-Friday, Weekend, All Week, etc
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

    public void editUpdate(boolean editedReminder, int position){
        if (!editedReminder) {
            SingletonDataArray.getInstance().addReminder(this);
        } else {
            bSelected = true;
        }
    }

    public void dataUpdate() {
        DatabaseUtil db = new DatabaseUtil();
        db.open();
        db.updateRow(this);
        db.close();
        Log.d("reminder dataUpdate", "updating database");
    }

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
