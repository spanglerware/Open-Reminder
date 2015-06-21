package com.remindme;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
public class Reminder implements Parcelable {  //implements parcelable so the data can be sent between activities
    public static boolean mSelected;

    private long rowId;  //id of reminder in the database
//    private int reminderId; //id of reminder that corresponds with position in dataArray
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


    public Reminder (String rem, String freq, long id) {
        reminder = rem;
        frequency = freq;
        floatFrequency = Float.parseFloat(frequency);
        rowId = id;
        active = false;
        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
        messageDays = new ArrayList<Integer>();
        alarmTime = 0;
        bSelected = false;
        reminderUseType = true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(reminder);
        parcel.writeString(frequency);
        parcel.writeLong(rowId);
        parcel.writeFloat(floatFrequency);
        parcel.writeFloat(timeFrom);
        parcel.writeFloat(timeTo);
        parcel.writeLong(counter);
        parcel.writeByte((byte) (monday ? 1 : 0));
        parcel.writeByte((byte) (tuesday ? 1 : 0));
        parcel.writeByte((byte) (wednesday ? 1 : 0));
        parcel.writeByte((byte) (thursday ? 1 : 0));
        parcel.writeByte((byte) (friday ? 1 : 0));
        parcel.writeByte((byte) (saturday ? 1 : 0));
        parcel.writeByte((byte) (sunday ? 1 : 0));
        parcel.writeByte((byte) (reminderUseType ? 1 : 0));
        parcel.writeByte((byte) (notificationType ? 1 : 0));
        parcel.writeInt(messageId);
        parcel.writeByte((byte) (active ? 1 : 0));
//        parcel.writeInt(reminderId);
        parcel.writeList(messageDays);
        parcel.writeLong(alarmTime);
    }

    private Reminder(Parcel in) {
        reminder = in.readString();
        frequency = in.readString();
        rowId = in.readLong();
        floatFrequency = in.readFloat();
        timeFrom = in.readFloat();
        timeTo = in.readFloat();
        counter = in.readLong();
        monday = in.readByte() != 0;
        tuesday = in.readByte() != 0;
        wednesday = in.readByte() != 0;
        thursday = in.readByte() != 0;
        friday = in.readByte() != 0;
        saturday = in.readByte() != 0;
        sunday = in.readByte() != 0;
        reminderUseType = in.readByte() != 0;
        notificationType = in.readByte() != 0;
        messageId = in.readInt();
        active = in.readByte() != 0;
//        reminderId = in.readInt();
        messageDays = in.readArrayList(null);
        alarmTime = in.readLong();
    }

    public static final Parcelable.Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    public long getRowId() {
        return rowId;
    }

    public String getReminder() { return reminder; }

    public String getFrequency() { return frequency; }

//    public String getFormattedFrequency() {
//        int hours, minutes, seconds;
//        hours = intFrequency / (60 * 60 * 1000);
//        minutes = (intFrequency % (60 * 60 * 1000)) / (60 * 1000);
//        seconds = ((intFrequency % (60 * 60 * 1000)) % (60 * 1000) / 1000);
//
//        String formattedFreq = "";
//        if (hours != 0) { formattedFreq = String.valueOf(hours) + ":"; }
//        formattedFreq += String.format("%02d:%02d",
//                minutes, seconds);
//
//        return formattedFreq;
//    }

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

    public boolean getRecurring() { return reminderUseType; }

    public boolean getNotificationType() { return notificationType; }

    public int getMessageId() { return messageId; }

    public long getCounter() { return counter; }

    public long getAlarmTime() { return alarmTime; }

//    public int getReminderId() { return reminderId; }

    public boolean isActive() { return active; }


    public void setReminder(String stringReminder) { reminder = stringReminder; }

    public void setFrequency(String stringFrequency) {
        frequency = stringFrequency;
        floatFrequency = Float.parseFloat(stringFrequency);
        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
    }

    public void setFloatFrequency(float floatFreq) {
        floatFrequency = floatFreq;
        frequency = String.valueOf(floatFreq);
        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
    }

    public void setDays(boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun) {
        if (messageDays == null) { messageDays = new ArrayList<Integer>(); }
        if (!messageDays.isEmpty()) { messageDays.clear(); }

        monday = mon; if (monday) { messageDays.add(1); }
        tuesday = tue; if (tuesday) { messageDays.add(2); }
        wednesday = wed; if (wednesday) { messageDays.add(3); }
        thursday = thu; if (thursday) { messageDays.add(4); }
        friday = fri; if (friday) { messageDays.add(5); }
        saturday = sat; if (saturday) { messageDays.add(6); }
        sunday = sun; if (sunday) { messageDays.add(7); }
    }

    public void setTimes(float from, float to) {
        timeFrom = from;
        timeTo = to;
    }

    //the active flag indicates if the reminder currently is counting down
    public void setActive(boolean bActive) {
        active = bActive;
//        if (active) {
//            if (counter != null) {
//                counter.setTime(TimeUtil.FloatTimeToMilliseconds(floatFrequency));
//            } else {
//                counter = new Time(intFrequency);
//            }
//        } else {
//            // reset counter
//            counter.setTime(intFrequency);
//        }
        //counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
    }

    public void setMisc(boolean bUsageType, boolean bNotificationType, int iMessageId) {
        reminderUseType = true; //todo may remove Misc data or comment out for now
        notificationType = bNotificationType;
        messageId = iMessageId;
    }

//    public void setReminderId(int listPosition) {
//        reminderId = listPosition;
//    }

    public void setAlarmTime (long time) { alarmTime = time; }

    public void startCounter() {
        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
    }

    public String getCounterAsString() {
        long time = counter;
        String strTimer;
        if (time == 0) {
            strTimer = "";
        } else {
            //todo will need to add days to strTimer
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
//            } else if (hours >= 10) {
//                format = "%02d:%02d:%02d";
//                strTimer = ("" + String.format(format, hours, minutes, seconds));
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
        //Log.v("reminder counter", strTimer);
        return (strTimer);
    }

    //reduce counter method is used to decrement the count down variable used to display the reminder time
    //todo countdown timers can get delayed based on when dialog is clicked, may need to refresh counters more often
    public boolean reduceCounter(long increment) {
        long time = counter;
        boolean nextAlarm = false;
        if (active) {
            if (time <= 0) {
                time = 0;
                if (reminderUseType) {  //for a repeating reminder, set up next alarm
                    long newTime = scheduleNextAlarm();  //need return value for updating counter, if 0 can set active to false
                    if (newTime == 0) {
                        active = false;
                    } else {
                        nextAlarm = true;
                    }
                    time = newTime;
                } else {  //this is the case for a single use reminder
                    active = false;
                }
            } else {
                time = time - increment;
            }
            counter = time;
        }
        return (nextAlarm);  //this indicates whether or not the timer has completed
    }

    //need an update method for the counter for when the app wakes from the background
    public boolean updateCounter() {
        boolean changed = false;
        Calendar calendar = Calendar.getInstance();
        if (active) {
            long currentTime = calendar.getTimeInMillis();
            if (alarmTime > currentTime) {
                counter = alarmTime - currentTime;
            } else {
                changed = true;
            }
        }
        return changed;
    }


    //set up next alarm based on the class settings
    private long scheduleNextAlarm() {
        //todo re-write to use float time for better tracking
        //first calculate next time for today, then check if it is in time range, then check for next days
        LocalTime localTime = LocalTime.now();
        LocalDate localDate = LocalDate.now();

        //todo not working correctly, need to fix
        float currentTime = TimeUtil.MillisecondsToFloatTime(localTime.getMillisOfDay());
        float alarmNextTime = currentTime + floatFrequency;

        Calendar calendar = Calendar.getInstance();
        long currentCalendarTime = calendar.getTimeInMillis();

        int today = localDate.getDayOfWeek();
        int tomorrow = localDate.getDayOfWeek() + 1;
        if (tomorrow > 7) { tomorrow = 1; }
        boolean found = false;
        int daysFromToday = 0;
        float nextTime = 0;  //this value will be the next alarm time in float time

        //check if frequency fits within timefrom - timeto interval, if not then there will be no repeat
        //also check if no days were selected for repeating
        if (floatFrequency > (timeTo - timeFrom) || messageDays.isEmpty()) {
            active = false;
            alarmTime = 0;
            return 0;
        }

        //first check for today
        if (messageDays.contains(today) && alarmNextTime < timeTo)  {
            if (alarmNextTime <= timeFrom) {
                nextTime = timeFrom + floatFrequency;
            } else {
                //schedule alarm normally
                nextTime = alarmNextTime;
            }
        } else {  //check for next day to run alarm
            for (int i = tomorrow; i < 8; i++) {
                if (messageDays.contains(i)) { found = true; daysFromToday = i - tomorrow + 1;  break; }
            }
            if (!found && tomorrow > 1) {  //if tomorrow = 1 then it has already been covered by loop above
                for (int i = 1; i < tomorrow; i++) {
                    if (messageDays.contains(i)) { found = true; daysFromToday = 7 - tomorrow + i + 1;  break; }
                }
            }
            if (found) {
                nextTime = (daysFromToday * 24) + timeFrom + floatFrequency;
            } else {
                active = false;
                alarmTime = 0;
                return 0;
            }
        }

        nextTime -= currentTime;
        alarmTime = TimeUtil.FloatTimeToMilliseconds(nextTime) + currentCalendarTime;

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
        //todo what happens if nothing selected? could return just today
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

    public void update(Context context, boolean editedReminder, int position){
        DatabaseUtil db = new DatabaseUtil(context);
        db.open();
        db.updateRow(this);
        db.close();
        counter = TimeUtil.FloatTimeToMilliseconds(floatFrequency);
        if (editedReminder) {
            SingletonDataArray.getInstance().updateReminder(this, position);
        } else {
            SingletonDataArray.getInstance().addReminder(this);
        }
    }

    public void reload(Context context) {
        DatabaseUtil db = new DatabaseUtil(context);
        db.open();
        Cursor cursor = db.getRow(rowId);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
        } else {
            return;
        }

        frequency = cursor.getString(DatabaseUtil.COLUMN_FREQUENCY);
        reminder = cursor.getString(DatabaseUtil.COLUMN_REMINDER);

        monday = cursor.getInt(DatabaseUtil.COLUMN_MONDAY) > 0;
        tuesday = cursor.getInt(DatabaseUtil.COLUMN_TUESDAY) > 0;
        wednesday = cursor.getInt(DatabaseUtil.COLUMN_WEDNESDAY) > 0;
        thursday = cursor.getInt(DatabaseUtil.COLUMN_THURSDAY) > 0;
        friday = cursor.getInt(DatabaseUtil.COLUMN_FRIDAY) > 0;
        saturday = cursor.getInt(DatabaseUtil.COLUMN_SATURDAY) > 0;
        sunday = cursor.getInt(DatabaseUtil.COLUMN_SUNDAY) > 0;

        timeFrom = cursor.getFloat(DatabaseUtil.COLUMN_TIME_FROM);
        timeTo = cursor.getFloat(DatabaseUtil.COLUMN_TIME_TO);

        reminderUseType = cursor.getInt(DatabaseUtil.COLUMN_RECURRING) > 0;
        notificationType = cursor.getInt(DatabaseUtil.COLUMN_NOTIFICATION_TYPE) > 0;
        messageId = cursor.getInt(DatabaseUtil.COLUMN_MESSAGE);

        db.close();
    }



}   //end of Reminder class
