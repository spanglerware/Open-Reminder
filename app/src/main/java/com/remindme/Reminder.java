package com.remindme;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Scott on 4/26/2015.
 */
public class Reminder implements Parcelable {
    private long rowId;
    private String reminder;
    private String frequency;
    private long timeFrom;
    private long timeTo;

    private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
    private ArrayList<Integer> messageDays;
    private boolean reminderUseType;  //true for recurring, false for single use
    private boolean notificationType;  //true for notification, false for alarm
    private int messageId;
    private MyAlarm myAlarm;
    private int alarmId;

    private boolean active;
    private Time counter;
    private static final String FORMAT = "%02d:%02d:%02d";
    private int intFrequency;

    LocalTime localTimeFrom;
    LocalTime localTimeTo;

    public Reminder (String rem, String freq, long id) {
        reminder = rem;
        frequency = freq;
        intFrequency = Integer.parseInt(frequency) * 60000;
        rowId = id;
        active = false;
        counter = new Time(0);
        messageDays = new ArrayList<Integer>();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(reminder);
        parcel.writeString(frequency);
        parcel.writeLong(rowId);
        parcel.writeInt(intFrequency);
        parcel.writeLong(timeFrom);
        parcel.writeLong(timeTo);
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
        parcel.writeInt(alarmId);
        parcel.writeList(messageDays);
        parcel.writeValue(localTimeFrom);
        parcel.writeValue(localTimeTo);

    }

    private Reminder(Parcel in) {
        reminder = in.readString();
        frequency = in.readString();
        rowId = in.readLong();
        intFrequency = in.readInt();
        timeFrom = in.readLong();
        timeTo = in.readLong();
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
        alarmId = in.readInt();
        messageDays = in.readArrayList(null);
        localTimeFrom = (LocalTime) in.readValue(null);
        localTimeTo = (LocalTime) in.readValue(null);
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

    public String getReminder() {
        return reminder;
    }

    public String getFrequency() { return frequency; }

    public int getIntFrequency() { return intFrequency; }

    public int getFrequencyMinutes() {
        return (intFrequency / 60000);
    }

    public long getTimeFrom() { return timeFrom; }

    public String getTimeFromAsString() {
        Time time = new Time(timeFrom);
        return (time.toString()).substring(0,5);
    }

    public long getTimeTo() { return timeTo; }

    public String getTimeToAsString() {
        Time time = new Time(timeTo);
        return (time.toString()).substring(0,5);
    }

    public boolean[] getDays() {
        boolean days[] = { monday, tuesday, wednesday, thursday, friday, saturday, sunday};
        return days;
    }

    public boolean getRecurring() { return reminderUseType; }

    public boolean getNotificationType() { return notificationType; }

    public int getMessageId() { return messageId; }

    public long getCounter() {
        return counter.getTime();
    }

    public boolean isActive() { return active; }


    public void setReminder(String stringReminder) {
        reminder = stringReminder;
    }

    public void setFrequency(String stringFrequency) {
        frequency = stringFrequency;
        intFrequency = Integer.parseInt(frequency) * 60000;
    }

    public void setLongFrequency(int intFreq) {
        intFrequency = intFreq;
        frequency = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(intFreq));
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

    public void setTimes(long from, long to) {
        timeFrom = from;
        timeTo = to;
        localTimeFrom = LocalTime.fromMillisOfDay(from);
        localTimeTo = LocalTime.fromMillisOfDay(to);
    }

    public void setTimes(String from, String to) {
        String stringFrom = from + ":00";
        String stringTo = to + ":00";

        Time time = Time.valueOf(stringFrom);
        localTimeFrom = LocalTime.parse(stringFrom);
        timeFrom = time.getTime();
        time = Time.valueOf(stringTo);
        localTimeTo = LocalTime.parse(stringTo);
        timeTo = time.getTime();
    }

    //todo may want to set inactive if sent to Edit, would also reset counter?
    public void setActive(boolean bActive) {
        active = bActive;
        if (active) {
            //todo this may reset the counter any time the class is sent to another activity
            if (counter != null) {
                counter.setTime(intFrequency);
            } else {
                counter = new Time(intFrequency);
            }
        }
    }

    public void setMisc(boolean bUsageType, boolean bNotificationType, int iMessageId) {
        reminderUseType = bUsageType;
        notificationType = bNotificationType;
        messageId = iMessageId;
    }

    public void setMessage(Context context, int listPosition) {
        alarmId = listPosition;
        myAlarm = new MyAlarm(context, alarmId, intFrequency, messageId, reminder);

        //todo set up myNotification here as well
    }

    public void startAlarm() {
        myAlarm.start();
        active = true;
    }

    public void cancelAlarm() {
        myAlarm.cancel();
        active = false;
    }

    public void startCounter() {
        counter.setTime(intFrequency);
    }

    public String getCounterAsString() {
        long time = counter.getTime();
        String strTimer;
        if (time == 0) {
            strTimer = "Complete";
        } else {
            //todo need to add days to strTimer?
            strTimer = ("" + String.format(FORMAT,
                    TimeUnit.MILLISECONDS.toHours(time),
                    TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                    TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))));
        }
/*        Log.v("reminder counter", strTimer); //todo firing 3 times per second? why??

        LocalTime localTime = LocalTime.now();
        long millis = localTime.getMillisOfDay();

        Log.v("Local Time now millis", String.valueOf(millis));
        Log.v("Local Time To millis", String.valueOf(localTimeTo.getMillisOfDay()));
        */

        return (strTimer);
    }

    //reduce counter method is used to decrement the count down variable used to display the reminder time
    //todo if alarm dialog is clicked too soon the next alarm will not be scheduled
    //todo countdown timers can get delayed based on when dialog is clicked, may need to refresh counters every so often
    public boolean reduceCounter(long increment, Context context) {
        long time = counter.getTime();
        if (active) {
            if (time <= 0) {
                time = 0;
                if (reminderUseType) {  //for a repeating reminder, set up next alarm
                    int newTime = scheduleNextAlarm(context);  //need return value for updating counter, if 0 can set active to false
                    if (newTime == 0) { active = false; }
                    time = newTime;
                } else {  //this is the case for a single use reminder
                    active = false;
                }
            } else {
                time = time - increment;
            }
            counter.setTime(time);
        }
        return (time == 0);  //this indicates whether or not the timer has completed
    }

    //need an update method for the counter for when the app wakes from the background
    public void updateCounter() {
        Calendar calendar = Calendar.getInstance();
        if (active) {
            long alarmTime = myAlarm.getAlarmTime();
            long currentTime = calendar.getTimeInMillis();
            if (alarmTime > currentTime) {
                counter.setTime(alarmTime - currentTime);
            } else {
                counter.setTime(0);
                active = false;
            }
        }
        //todo check this line to see if needed: reduceCounter(0);  //run this method to check for timers that have expired
    }

    //set up next alarm based on the class settings
    private int scheduleNextAlarm(Context alarmContext) {
        //todo first calculate next time for today, then check if it is in time range, then check for next days
        long currentTime = LocalTime.now().getMillisOfDay();
        long alarmTime = currentTime + intFrequency;
        long longTimeTo = localTimeTo.getMillisOfDay();
        long longTimeFrom = localTimeFrom.getMillisOfDay();

        int tomorrow = LocalDate.now().getDayOfWeek() + 1;
        if (tomorrow > 7) { tomorrow = 1; }
        boolean found = false;
        int daysFromToday = 0;
        int nextTime = 0;

        if (!messageDays.isEmpty() && alarmTime <= longTimeTo && alarmTime >= longTimeFrom) { //check to see if next alarm is within time range
            myAlarm = new MyAlarm(alarmContext, alarmId, intFrequency, messageId, reminder);
            myAlarm.start();
            nextTime = intFrequency;
        } else if (!messageDays.isEmpty()) {  //check for next day to run alarm
            for (int i = tomorrow; i < 8; i++) {
                if (messageDays.contains(i)) { found = true; daysFromToday = i - tomorrow + 1;  break; }
            }
            if (!found && tomorrow > 1) {  //if tomorrow = 1 then it has already been covered by loop above
                for (int i = 1; i < tomorrow; i++) {
                    if (messageDays.contains(i)) { found = true; daysFromToday = 7 - tomorrow + i + 1;  break; }
                }
            }
        }

        if (found) {
            LocalDateTime time = LocalDateTime.now();
            int currentMillis = time.getMillisOfDay();

            nextTime = (daysFromToday * 86400000) + localTimeFrom.getMillisOfDay() + intFrequency - currentMillis;
            myAlarm = new MyAlarm(alarmContext, alarmId, nextTime, messageId, reminder);
            myAlarm.start();
        }
        //else, leave empty, will not schedule a next alarm
        return nextTime;
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


    //end of Reminder class
}
