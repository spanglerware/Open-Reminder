package com.remindme;

import android.content.Context;
import android.database.Cursor;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Scott on 6/8/2015.
 */
public class SingletonDataArray {
    private ArrayList<Reminder> dataArray;

    private static SingletonDataArray instance;

    private SingletonDataArray() {
        dataArray = new ArrayList<Reminder>();
    }

    public static SingletonDataArray getInstance() {
        if (instance == null) {
            instance = new SingletonDataArray();
        }
        return instance;
    }

    public ArrayList<Reminder> getDataArray() {
        return dataArray;
    }

    public void updateReminder(Reminder reminder, int position) {
        //todo getReminderId position does not update when the list changes
        dataArray.set(position, reminder);
    }

    public void addReminder(Reminder reminder) {
        dataArray.add(reminder);
    }

    public void removeReminder(int position) {
        dataArray.remove(position);
    }

    public Reminder getReminderWithId (long rowId) {
        for (Reminder reminder : dataArray) {
            if (reminder.getRowId() == rowId) { return reminder; }
        }
        return null;
    }

    public int getPositionWithId (long rowId) {
        for (int i = 0; i < dataArray.size(); i++) {
            if (dataArray.get(i).getRowId() == rowId) { return i; }
        }
        return -1;
    }

    public int getSize() {
        return dataArray.size();
    }

    public void loadFromDb(Context context) {
        dataArray.clear();
        DatabaseUtil db;

        //todo will not have a context to create new db util?
        if (DatabaseUtil.hasContext()) {
            db = new DatabaseUtil();
        } else {
            db = new DatabaseUtil(context);
        }

        long rowId;
        String frequency;
        String reminder;
        float timeFrom;
        float timeTo;
        boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
        boolean reminderUseType;
        boolean notificationType;
        int messageId;
        boolean active;
        long alarmTime;

        Cursor cursor = db.getAllRows();
        int count = cursor.getCount();

        //transfer data from the cursor adapter to the custom adapter
        if (count > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < count; i++) {
                rowId = cursor.getLong(DatabaseUtil.COLUMN_ROWID);
                frequency = cursor.getString(DatabaseUtil.COLUMN_FREQUENCY);
                reminder = cursor.getString(DatabaseUtil.COLUMN_REMINDER);

                timeFrom = cursor.getFloat(DatabaseUtil.COLUMN_TIME_FROM);
                timeTo = cursor.getFloat(DatabaseUtil.COLUMN_TIME_TO);

                monday = cursor.getInt(DatabaseUtil.COLUMN_MONDAY) > 0;
                tuesday = cursor.getInt(DatabaseUtil.COLUMN_TUESDAY) > 0;
                wednesday = cursor.getInt(DatabaseUtil.COLUMN_WEDNESDAY) > 0;
                thursday = cursor.getInt(DatabaseUtil.COLUMN_THURSDAY) > 0;
                friday = cursor.getInt(DatabaseUtil.COLUMN_FRIDAY) > 0;
                saturday = cursor.getInt(DatabaseUtil.COLUMN_SATURDAY) > 0;
                sunday = cursor.getInt(DatabaseUtil.COLUMN_SUNDAY) > 0;

                reminderUseType = cursor.getInt(DatabaseUtil.COLUMN_RECURRING) > 0;
                notificationType = cursor.getInt(DatabaseUtil.COLUMN_NOTIFICATION_TYPE) > 0;
                messageId = cursor.getInt(DatabaseUtil.COLUMN_MESSAGE);
                active = cursor.getInt(DatabaseUtil.COLUMN_ACTIVE) > 0;
                alarmTime = cursor.getLong(DatabaseUtil.COLUMN_ALARM_TIME);

                Reminder item = new Reminder(i, reminder, frequency, rowId, timeFrom, timeTo, monday, tuesday, wednesday, thursday,
                        friday, saturday, sunday, reminderUseType, notificationType, messageId, active, alarmTime);
                dataArray.add(i, item);

                cursor.moveToNext();
            }
        }
    }



}  //end of SingletonDataArray class
